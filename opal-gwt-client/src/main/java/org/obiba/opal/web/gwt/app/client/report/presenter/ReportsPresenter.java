/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.presenter;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateModalPresenter.Mode;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ReportCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ReportsPresenter extends PresenterWidget<ReportsPresenter.Display> implements ReportsUiHandlers {

  ReportTemplateDetailsPresenter reportTemplateDetailsPresenter;

  ReportTemplateListPresenter reportTemplateListPresenter;

  ModalProvider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider;

  private ReportTemplateDto reportTemplate;

  private Runnable actionRequiringConfirmation;

  private String project;

  @Inject
  public ReportsPresenter(Display display, EventBus eventBus,
      ReportTemplateDetailsPresenter reportTemplateDetailsPresenter,
      ReportTemplateListPresenter reportTemplateListPresenter,
      ModalProvider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider) {
    super(eventBus, display);
    this.reportTemplateDetailsPresenter = reportTemplateDetailsPresenter;
    this.reportTemplateListPresenter = reportTemplateListPresenter;
    this.reportTemplateUpdateModalPresenterProvider = reportTemplateUpdateModalPresenterProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void onAdd() {
    ReportTemplateUpdateModalPresenter presenter = reportTemplateUpdateModalPresenterProvider.show();
    presenter.setProject(project);
    presenter.setReportTemplate(null);
  }

  @Override
  public void onEdit() {
    ReportTemplateUpdateModalPresenter presenter = reportTemplateUpdateModalPresenterProvider.show();
    presenter.setProject(project);
    presenter.setReportTemplate(reportTemplate);
  }

  @Override
  public void onDownload() {
    downloadFile(reportTemplate.getDesign());
  }

  @Override
  public void onExecute() {
    ResponseCodeCallback callbackHandler = new CommandResponseCallBack();
    ReportCommandOptionsDto reportCommandOptions = ReportCommandOptionsDto.create();
    reportCommandOptions.setName(reportTemplate.getName());
    ResourceRequestBuilderFactory.newBuilder().forResource("/shell/report").post()
        .withResourceBody(ReportCommandOptionsDto.stringify(reportCommandOptions))
        .withCallback(Response.SC_CREATED, callbackHandler).send();
  }

  @Override
  public void onDelete() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        String reportTemplateName = reportTemplate.getName();
        UriBuilder ub = UriBuilder.create().segment("report-template", reportTemplateName);
        ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).delete()
            .withCallback(Response.SC_OK, new RemoveReportTemplateResponseCallBack())
            .withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithKeys(actionRequiringConfirmation, "removeReportTemplate", "confirmDeleteReportTemplate"));
  }

  @Override
  protected void onBind() {
    super.onBind();
    for(SplitPaneWorkbenchPresenter.Slot slot : SplitPaneWorkbenchPresenter.Slot.values()) {
      setInSlot(slot, getDefaultPresenter(slot));
    }
    addRegisteredHandler(ReportTemplateSelectedEvent.getType(), new ReportTemplateSelectedEvent.Handler() {

      @Override
      public void onReportTemplateSelected(ReportTemplateSelectedEvent event) {
        reportTemplate = event.getReportTemplate();
        getView().setCurrentReportTemplateVisible(reportTemplate != null);
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if(event.getSource() == actionRequiringConfirmation && event.isConfirmed()) {
          actionRequiringConfirmation.run();
          actionRequiringConfirmation = null;
        }
      }
    });
  }

  public void showProject(String project) {
    this.project = project;
    reportTemplateListPresenter.showProject(project);

  }

  private class RemoveReportTemplateResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(new ReportTemplateDeletedEvent(reportTemplate));
    }

  }

  private class ReportTemplateNotFoundCallBack implements ResponseCodeCallback {

    private final String templateName;

    private ReportTemplateNotFoundCallBack(String reportTemplateName) {
      templateName = reportTemplateName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(NotificationEvent.newBuilder().error("ReportTemplateCannotBeFound").args(templateName).build());
    }
  }

  private class CommandResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_CREATED) {
        fireEvent(NotificationEvent.newBuilder().info("ReportJobStarted").build());
      }
    }
  }

  private void downloadFile(String filePath) {
    fireEvent(new FileDownloadRequestEvent("/files" + filePath));
  }

  protected PresenterWidget<?> getDefaultPresenter(SplitPaneWorkbenchPresenter.Slot slot) {
    switch(slot) {
      case LEFT:
        return reportTemplateListPresenter;
      case CENTER:
        return reportTemplateDetailsPresenter;
    }
    return null;
  }

  protected void authorize() {
    // create report templates
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").post()
        .authorize(getView().getAddReportTemplateAuthorizer()).send();

    // run report
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/report").post()
        .authorize(getView().getExecuteReportAuthorizer()).send();

    // download report design
    UriBuilder ub = UriBuilder.create().segment("files", reportTemplate.getDesign());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getDownloadReportDesignAuthorizer()).send();

    ub = UriBuilder.create().segment("report-template", reportTemplate.getName());
    // remove
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).delete()
        .authorize(getView().getRemoveReportTemplateAuthorizer()).send();

    // edit
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).put()
        .authorize(getView().getUpdateReportTemplateAuthorizer()).send();
  }

  public interface Display extends View, HasUiHandlers<ReportsUiHandlers> {
    HasAuthorization getAddReportTemplateAuthorizer();

    HasAuthorization getExecuteReportAuthorizer();

    HasAuthorization getDownloadReportDesignAuthorizer();

    HasAuthorization getRemoveReportTemplateAuthorizer();

    HasAuthorization getUpdateReportTemplateAuthorizer();

    void setCurrentReportTemplateVisible(boolean visible);
  }
}
