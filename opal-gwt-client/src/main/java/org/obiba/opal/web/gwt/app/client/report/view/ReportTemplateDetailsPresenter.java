/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.report.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.report.edit.ReportTemplateEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateCreatedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ReportCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.ReportDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class ReportTemplateDetailsPresenter extends PresenterWidget<ReportTemplateDetailsPresenter.Display>
    implements ReportTemplateDetailsUiHandlers {

  public static final String DOWNLOAD_ACTION = "Download";

  private Runnable actionRequiringConfirmation;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final ModalProvider<ReportTemplateEditModalPresenter> reportTemplateUpdateModalPresenterProvider;

  private ReportTemplateDto reportTemplate;

  private final TranslationMessages translationMessages;

  @Inject
  public ReportTemplateDetailsPresenter(Display display, EventBus eventBus,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
      ModalProvider<ReportTemplateEditModalPresenter> reportTemplateUpdateModalPresenterProvider,
      TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.reportTemplateUpdateModalPresenterProvider = reportTemplateUpdateModalPresenterProvider.setContainer(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    getView().setUiHandlers(this);
  }

  public void refresh() {
    if(reportTemplate != null) {
      refreshProducedReports(reportTemplate);
    }
  }

  @Override
  public void onReset() {
    super.onReset();
    refresh();
  }

  @Override
  protected void onBind() {
    super.onBind();
    initUiComponents();
    addHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    reportTemplate = null;
  }

  @Override
  public void onEdit() {
    ReportTemplateEditModalPresenter presenter = reportTemplateUpdateModalPresenterProvider.get();
    presenter.setReportTemplate(reportTemplate);
  }

  @Override
  public void onDownload() {
    downloadFile(reportTemplate.getDesign());
  }

  @Override
  public void onExecute() {
    ReportCommandOptionsDto reportCommandOptions = ReportCommandOptionsDto.create();
    reportCommandOptions.setName(reportTemplate.getName());
    reportCommandOptions.setProject(reportTemplate.getProject());
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource("/project/" + reportTemplate.getProject() + "/commands/_report") //
        .withResourceBody(ReportCommandOptionsDto.stringify(reportCommandOptions)) //
        .withCallback(Response.SC_CREATED, new CommandResponseCallBack()) //
        .post().send();
  }

  @Override
  public void onDelete() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        String reportTemplateName = reportTemplate.getName();
        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(
                UriBuilders.PROJECT_REPORT_TEMPLATE.create().build(reportTemplate.getProject(), reportTemplateName)) //
            .withCallback(SC_OK, new RemoveReportTemplateResponseCallBack()) //
            .withCallback(SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)) //
            .delete().send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeReportTemplate(),
            translationMessages.confirmDeleteReportTemplate()));
  }

  //
  // Private methods
  //

  @SuppressWarnings("unchecked")
  private void initUiComponents() {
    getView().setProducedReports((JsArray<ReportDto>) JsArray.createArray());
    getView().setReportTemplateDetails(null);
  }

  private void addHandlers() {
    getView().getActionColumn().setActionHandler(new ActionHandler<ReportDto>() {
      @Override
      public void doAction(ReportDto dto, String actionName) {
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });

    addRegisteredHandler(ReportTemplateSelectedEvent.getType(),
        new ReportTemplateSelectedEvent.ReportTemplateSelectedHandler() {
          @Override
          public void onReportTemplateSelected(ReportTemplateSelectedEvent event) {
            ReportTemplateDto template = event.getReportTemplate();
            if(template == null) {
              getView().setReportTemplateDetails(null);
            } else {
              refreshReportTemplateDetails(template);
            }
          }
        });

    registerHandler(getView().addReportDesignClickHandler(new ReportDesignClickHandler()));
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
    addRegisteredHandler(ReportTemplateCreatedEvent.getType(), new ReportTemplateCreatedUpdatedHandler());
    addRegisteredHandler(ReportTemplateUpdatedEvent.getType(), new ReportTemplateCreatedUpdatedHandler());
  }

  private void authorize() {
    // display reports

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilder.create()
            .segment("files", "meta", "reports", reportTemplate.getProject(), reportTemplate.getName()).build()) //
        .authorize(getView().getListReportsAuthorizer()) //
        .get().send();

    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();

    // run report
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource("/project/" + reportTemplate.getProject() + "/commands/_report") //
        .authorize(getView().getExecuteReportAuthorizer()) //
        .post().send();

    // download report design
    UriBuilder ub = UriBuilder.create().segment("files", reportTemplate.getDesign());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getDownloadReportDesignAuthorizer()).send();

    // remove
    String uri = UriBuilders.PROJECT_REPORT_TEMPLATE.create()
        .build(reportTemplate.getProject(), reportTemplate.getName());
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(uri) //
        .authorize(getView().getRemoveReportTemplateAuthorizer()) //
        .delete().send();

    // edit
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(uri) //
        .authorize(getView().getUpdateReportTemplateAuthorizer()) //
        .put().send();
  }

  private void authorizeDownloadReport(ReportDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(dto.getLink()).get().authorize(authorizer)
        .send();
  }

  private void authorizeDeleteReport(ReportDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(dto.getLink()).delete().authorize(authorizer)
        .send();
  }

  protected void doActionImpl(final ReportDto dto, String actionName) {
    if(actionName.equals(DOWNLOAD_ACTION)) {
      authorizeDownloadReport(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          fireEvent(new FileDownloadRequestEvent(dto.getLink()));
        }
      });
    } else if(actionName.equals(ActionsColumn.REMOVE_ACTION)) {
      authorizeDeleteReport(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          actionRequiringConfirmation = new Runnable() {
            @Override
            public void run() {
              ResourceRequestBuilderFactory.newBuilder() //
                  .forResource(dto.getLink()) //
                  .withCallback(SC_OK, new ResponseCodeCallback() {
                    @Override
                    public void onResponseCode(Request request, Response response) {
                      fireEvent(ConfirmationTerminatedEvent.create());
                      refreshProducedReports(reportTemplate);
                    }
                  }) //
                  .delete().send();
            }
          };
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(actionRequiringConfirmation, translationMessages.removeFile(),
                  translationMessages.confirmDeleteFile()));
        }
      });
    }
  }

  private void refreshProducedReports(ReportTemplateDto reportTemplateDto) {
    ResourceRequestBuilderFactory.<JsArray<ReportDto>>newBuilder() //
        .forResource(UriBuilders.PROJECT_REPORT_TEMPLATE_REPORTS.create()
            .build(reportTemplateDto.getProject(), reportTemplateDto.getName())) //
        .withCallback(new ProducedReportsResourceCallback()) //
        .withCallback(SC_NOT_FOUND, new NoProducedReportsResourceCallback()) //
        .get().send();
  }

  private void refreshReportTemplateDetails(ReportTemplateDto reportTemplateDto) {
    String reportTemplateName = reportTemplateDto.getName();
    ResourceRequestBuilderFactory.<ReportTemplateDto>newBuilder() //
        .forResource(
            UriBuilders.PROJECT_REPORT_TEMPLATE.create().build(reportTemplateDto.getProject(), reportTemplateName)) //
        .withCallback(new ReportTemplateFoundCallBack()) //
        .withCallback(SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)) //
        .get().send();
  }

  private class RemoveReportTemplateResponseCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(ConfirmationTerminatedEvent.create());
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
      fireEvent(ConfirmationTerminatedEvent.create());
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

  private class ReportTemplateCreatedUpdatedHandler implements ReportTemplateCreatedEvent.ReportTemplateCreatedHandler,
      ReportTemplateUpdatedEvent.ReportTemplateUpdatedHandler {
    @Override
    public void onReportTemplateCreated(ReportTemplateCreatedEvent event) {
      refreshReportTemplateDetails(event.getReportTemplate());
    }

    @Override
    public void onReportTemplateUpdated(ReportTemplateUpdatedEvent event) {
      refreshReportTemplateDetails(event.getReportTemplate());
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private class ReportDesignClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      downloadFile(getView().getReportTemplateDetails().getDesign());
    }

    private void downloadFile(String filePath) {
      getEventBus().fireEvent(new FileDownloadRequestEvent("/files" + filePath));
    }

  }

  private class NoProducedReportsResourceCallback implements ResponseCodeCallback {

    @SuppressWarnings("unchecked")
    @Override
    public void onResponseCode(Request request, Response response) {
      getView().setProducedReports((JsArray<ReportDto>) JsArray.createArray());
    }
  }

  private class ProducedReportsResourceCallback implements ResourceCallback<JsArray<ReportDto>> {

    @Override
    public void onResource(Response response, JsArray<ReportDto> reports) {
      getView().setProducedReports(JsArrays.toSafeArray(reports));
    }
  }

  private class ReportTemplateFoundCallBack implements ResourceCallback<ReportTemplateDto> {

    @Override
    public void onResource(Response response, ReportTemplateDto resource) {
      reportTemplate = resource;
      getView().setReportTemplateDetails(reportTemplate);
      refreshProducedReports(reportTemplate);
      authorize();
    }
  }

  private final class PermissionsUpdate implements HasAuthorization {

    private static final String PERMISSIONS_SLOT = "permissions";

    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(PERMISSIONS_SLOT);
    }

    @Override
    public void authorized() {
      getView().setVisiblePermissionsPanel(true);
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.REPORT_TEMPLATE,
          ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_REPORTTEMPLATE, reportTemplate.getProject(),
          reportTemplate.getName());
      setInSlot(PERMISSIONS_SLOT, resourcePermissionsPresenter);
    }
  }

  public interface Display extends View, HasUiHandlers<ReportTemplateDetailsUiHandlers> {

    void setProducedReports(JsArray<ReportDto> reports);

    HasActionHandler<ReportDto> getActionColumn();

    HandlerRegistration addReportDesignClickHandler(ClickHandler handler);

    void setReportTemplateDetails(@Nullable ReportTemplateDto reportTemplate);

    ReportTemplateDto getReportTemplateDetails();

    HasAuthorization getListReportsAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    HasAuthorization getExecuteReportAuthorizer();

    HasAuthorization getDownloadReportDesignAuthorizer();

    HasAuthorization getRemoveReportTemplateAuthorizer();

    HasAuthorization getUpdateReportTemplateAuthorizer();

    void setVisiblePermissionsPanel(boolean value);
  }

}
