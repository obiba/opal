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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateListReceivedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateModalPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.ReportCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.ReportDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ReportTemplateDetailsPresenter extends PresenterWidget<ReportTemplateDetailsPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String DOWNLOAD_ACTION = "Download";

  private Runnable actionRequiringConfirmation;

  private final Provider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider;

  private final Provider<AuthorizationPresenter> authorizationPresenter;

  private ReportTemplateDto reportTemplate;

  @Inject
  public ReportTemplateDetailsPresenter(Display display, EventBus eventBus,
      Provider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider,
      Provider<AuthorizationPresenter> authorizationPresenter) {
    super(eventBus, display);
    this.reportTemplateUpdateModalPresenterProvider = reportTemplateUpdateModalPresenterProvider;
    this.authorizationPresenter = authorizationPresenter;
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
    setCommands();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    reportTemplate = null;
  }

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

    registerHandler(
        getEventBus().addHandler(ReportTemplateSelectedEvent.getType(), new ReportTemplateSelectedEvent.Handler() {

          @Override
          public void onReportTemplateSelected(ReportTemplateSelectedEvent event) {
            ReportTemplateDto reportTemplate = event.getReportTemplate();
            if(reportTemplate == null) {
              getView().setReportTemplateDetails(null);
            } else {
              refreshReportTemplateDetails(reportTemplate);
            }
          }
        }));

    registerHandler(getView().addReportDesignClickHandler(new ReportDesignClickHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    registerHandler(getEventBus().addHandler(ReportTemplateUpdatedEvent.getType(), new ReportTemplateUpdatedHandler()));
    registerHandler(getEventBus()
        .addHandler(ReportTemplateListReceivedEvent.getType(), new ReportTemplateListReceivedEventHandler()));
  }

  private void setCommands() {
    getView().setRunReportCommand(new RunReportCommand());
    getView().setDownloadReportDesignCommand(new DownloadReportDesignCommand());
    getView().setRemoveReportTemplateCommand(new RemoveReportTemplateCommand());
    getView().setUpdateReportTemplateCommand(new EditReportTemplateCommand());
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  private void authorize() {
    // run report
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/report").post()
        .authorize(getView().getRunReportAuthorizer()).send();

    // download report design
    UriBuilder ub = UriBuilder.create().segment("files", getView().getReportTemplateDetails().getDesign());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getDownloadReportDesignAuthorizer()).send();

    ub = UriBuilder.create().segment("report-template", reportTemplate.getName());
    // remove
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).delete()
        .authorize(getView().getRemoveReportTemplateAuthorizer()).send();

    // edit
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).put()
        .authorize(getView().getUpdateReportTemplateAuthorizer()).send();

    // display reports
    ub = UriBuilder.create().segment("files", "meta", "reports", reportTemplate.getName());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getListReportsAuthorizer()).send();

    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
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
          downloadReportFile(dto);
        }
      });
    } else if(actionName.equals(DELETE_ACTION)) {
      authorizeDeleteReport(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          actionRequiringConfirmation = new Runnable() {
            @Override
            public void run() {
              deleteReportFile(dto);
            }
          };
          getEventBus().fireEvent(
              ConfirmationRequiredEvent.createWithKeys(actionRequiringConfirmation, "deleteFile", "confirmDeleteFile"));
        }
      });
    }
  }

  private void deleteReportFile(ReportDto report) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() != Response.SC_OK) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
        } else {
          refreshProducedReports(reportTemplate);
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource(report.getLink()).delete()
        .withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler)
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler)
        .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void downloadReportFile(ReportDto report) {
    getEventBus().fireEvent(new FileDownloadEvent(report.getLink()));
  }

  private void downloadFile(String filePath) {
    String url = "/files" + filePath;
    getEventBus().fireEvent(new FileDownloadEvent(url));
  }

  private void refreshProducedReports(ReportTemplateDto reportTemplate) {
    UriBuilder ub = UriBuilder.create().segment("report-template", reportTemplate.getName(), "reports");
    ResourceRequestBuilderFactory.<JsArray<ReportDto>>newBuilder().forResource(ub.build()).get()
        .withCallback(new ProducedReportsResourceCallback()).withCallback(404, new NoProducedReportsResourceCallback())
        .send();
  }

  private void refreshReportTemplateDetails(ReportTemplateDto reportTemplate) {
    String reportTemplateName = reportTemplate.getName();
    UriBuilder ub = UriBuilder.create().segment("report-template", reportTemplateName);
    ResourceRequestBuilderFactory.<ReportTemplateDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ReportTemplateFoundCallBack())
        .withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
  }

  private class RunReportCommand implements Command {

    @Override
    public void execute() {
      ResponseCodeCallback callbackHandler = new CommandResponseCallBack();
      ReportCommandOptionsDto reportCommandOptions = ReportCommandOptionsDto.create();
      reportCommandOptions.setName(getView().getReportTemplateDetails().getName());
      ResourceRequestBuilderFactory.newBuilder().forResource("/shell/report").post()
          .withResourceBody(ReportCommandOptionsDto.stringify(reportCommandOptions))
          .withCallback(Response.SC_CREATED, callbackHandler).send();
    }

  }

  private class DownloadReportDesignCommand implements Command {
    @Override
    public void execute() {
      downloadFile(getView().getReportTemplateDetails().getDesign());
    }
  }

  private class EditReportTemplateCommand implements Command {

    @Override
    public void execute() {
      ReportTemplateUpdateModalPresenter presenter = reportTemplateUpdateModalPresenterProvider.get();
      presenter.setDialogMode(Mode.UPDATE);
      presenter.setReportTemplate(getView().getReportTemplateDetails());
      addToPopupSlot(presenter);
    }

  }

  private class RemoveReportTemplateCommand implements Command {

    @Override
    public void execute() {
      actionRequiringConfirmation = new Runnable() {
        @Override
        public void run() {
          String reportTemplateName = getView().getReportTemplateDetails().getName();
          UriBuilder ub = UriBuilder.create().segment("report-template", reportTemplateName);
          ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).delete()
              .withCallback(Response.SC_OK, new RemoveReportTemplateResponseCallBack())
              .withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
        }
      };
      getEventBus().fireEvent(ConfirmationRequiredEvent
          .createWithKeys(actionRequiringConfirmation, "removeReportTemplate", "confirmDeleteReportTemplate"));
    }

  }

  private class ReportTemplateUpdatedHandler implements ReportTemplateUpdatedEvent.Handler {

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

  private class CommandResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().info("ReportJobStarted").build());
      }
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

  private class ReportTemplateNotFoundCallBack implements ResponseCodeCallback {

    private final String templateName;

    private ReportTemplateNotFoundCallBack(String reportTemplateName) {
      templateName = reportTemplateName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("ReportTemplateCannotBeFound").args(templateName).build());
    }
  }

  private class RemoveReportTemplateResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(new ReportTemplateDeletedEvent(getView().getReportTemplateDetails()));
    }

  }

  class ReportTemplateListReceivedEventHandler implements ReportTemplateListReceivedEvent.Handler {

    @Override
    public void onReportTemplateListReceived(ReportTemplateListReceivedEvent event) {
      getView().setReportTemplatesAvailable(event.getReportTemplates().length() != 0);
    }
  }

  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(null);
    }

    @Override
    public void authorized() {
      AuthorizationPresenter authz = authorizationPresenter.get();
      String node = UriBuilder.create().segment("report-template", reportTemplate.getName()).build();
      authz.setAclRequest("report-template", new AclRequest(AclAction.REPORT_TEMPLATE_READ, node), //
          new AclRequest(AclAction.REPORT_TEMPLATE_ALL, node));
      setInSlot(null, authz);
    }
  }

  public interface Display extends View {

    void setReportTemplatesAvailable(boolean available);

    void setProducedReports(JsArray<ReportDto> reports);

    HasActionHandler<ReportDto> getActionColumn();

    HandlerRegistration addReportDesignClickHandler(ClickHandler handler);

    void setReportTemplateDetails(@Nullable ReportTemplateDto reportTemplate);

    ReportTemplateDto getReportTemplateDetails();

    void setRemoveReportTemplateCommand(Command command);

    void setRunReportCommand(Command command);

    void setDownloadReportDesignCommand(Command command);

    void setUpdateReportTemplateCommand(Command command);

    HasAuthorization getRemoveReportTemplateAuthorizer();

    HasAuthorization getRunReportAuthorizer();

    HasAuthorization getUpdateReportTemplateAuthorizer();

    HasAuthorization getListReportsAuthorizer();

    HasAuthorization getDownloadReportDesignAuthorizer();

    HasAuthorization getPermissionsAuthorizer();
  }

}
