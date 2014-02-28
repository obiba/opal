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

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
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

import com.google.gwt.core.client.GWT;
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

public class ReportTemplateDetailsPresenter extends PresenterWidget<ReportTemplateDetailsPresenter.Display>
    implements ReportTemplateDetailsUiHandlers {

  public static final String DOWNLOAD_ACTION = "Download";

  private Runnable actionRequiringConfirmation;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final ModalProvider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider;

  private ReportTemplateDto reportTemplate;

  private TranslationMessages translationMessages;

  @Inject
  public ReportTemplateDetailsPresenter(Display display, EventBus eventBus,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
      ModalProvider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider,
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
    ReportTemplateUpdateModalPresenter presenter = reportTemplateUpdateModalPresenterProvider.get();
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
    String uri = "/shell/report";
    if(reportTemplate.hasProject()) {
      uri = "/project/" + reportTemplate.getProject() + "/commands/_report";
    }
    ResourceRequestBuilderFactory.newBuilder().forResource(uri).post()
        .withResourceBody(ReportCommandOptionsDto.stringify(reportCommandOptions))
        .withCallback(Response.SC_CREATED, callbackHandler).send();
  }

  @Override
  public void onDelete() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        String reportTemplateName = reportTemplate.getName();
        String uri = UriBuilder.create().segment("report-template", reportTemplateName).build();
        if(reportTemplate.hasProject()) {
          uri = UriBuilders.PROJECT_REPORT_TEMPLATE.create().build(reportTemplate.getProject(), reportTemplateName);
        }
        ResourceRequestBuilderFactory.newBuilder().forResource(uri).delete()
            .withCallback(Response.SC_OK, new RemoveReportTemplateResponseCallBack())
            .withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
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

    addRegisteredHandler(ReportTemplateSelectedEvent.getType(), new ReportTemplateSelectedEvent.Handler() {

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
    String uri;
    uri = reportTemplate.hasProject()
        ? UriBuilder.create().segment("files", "meta", "reports", reportTemplate.getProject(), reportTemplate.getName())
        .build()
        : UriBuilder.create().segment("files", "meta", "reports", reportTemplate.getName()).build();

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(uri).get()
        .authorize(getView().getListReportsAuthorizer()).send();

    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();

    // run report
    uri = "/shell/report";
    if(reportTemplate.hasProject()) {
      uri = "/project/" + reportTemplate.getProject() + "/commands/_report";
    }
    GWT.log(uri);
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(uri).post()
        .authorize(getView().getExecuteReportAuthorizer()).send();

    // download report design
    UriBuilder ub = UriBuilder.create().segment("files", reportTemplate.getDesign());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getDownloadReportDesignAuthorizer()).send();

    uri = UriBuilder.create().segment("report-template", reportTemplate.getName()).build();
    if(reportTemplate.hasProject()) {
      uri = UriBuilders.PROJECT_REPORT_TEMPLATE.create().build(reportTemplate.getProject(), reportTemplate.getName());
    }
    // remove
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(uri).delete()
        .authorize(getView().getRemoveReportTemplateAuthorizer()).send();

    // edit
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(uri).put()
        .authorize(getView().getUpdateReportTemplateAuthorizer()).send();
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
              ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

                @Override
                public void onResponseCode(Request request, Response response) {
                  if(response.getStatusCode() == Response.SC_OK) {
                    refreshProducedReports(reportTemplate);
                  } else {
                    getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
                  }
                }
              };

              ResourceRequestBuilderFactory.newBuilder().forResource(dto.getLink()).delete()
                  .withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler)
                  .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler)
                  .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
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
    String uri = UriBuilder.create().segment("report-template", reportTemplateDto.getName(), "reports").build();
    if(reportTemplateDto.hasProject()) {
      uri = UriBuilders.PROJECT_REPORT_TEMPLATE_REPORTS.create()
          .build(reportTemplateDto.getProject(), reportTemplateDto.getName());
    }
    ResourceRequestBuilderFactory.<JsArray<ReportDto>>newBuilder().forResource(uri).get()
        .withCallback(new ProducedReportsResourceCallback())
        .withCallback(Response.SC_NOT_FOUND, new NoProducedReportsResourceCallback()).send();
  }

  private void refreshReportTemplateDetails(ReportTemplateDto reportTemplateDto) {
    String reportTemplateName = reportTemplateDto.getName();
    String uri = UriBuilder.create().segment("report-template", reportTemplateName).build();
    if(reportTemplateDto.hasProject()) {
      uri = UriBuilders.PROJECT_REPORT_TEMPLATE.create().build(reportTemplateDto.getProject(), reportTemplateName);
    }

    ResourceRequestBuilderFactory.<ReportTemplateDto>newBuilder().forResource(uri).get()
        .withCallback(new ReportTemplateFoundCallBack())
        .withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
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

  private class ReportTemplateCreatedUpdatedHandler
      implements ReportTemplateUpdatedEvent.Handler, ReportTemplateCreatedEvent.Handler {

    @Override
    public void onReportTemplateUpdated(ReportTemplateUpdatedEvent event) {
      refreshReportTemplateDetails(event.getReportTemplate());
    }

    @Override
    public void onReportTemplateCreated(ReportTemplateCreatedEvent event) {
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
      String url = "/files" + filePath;
      getEventBus().fireEvent(new FileDownloadRequestEvent(url));
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
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(null);
    }

    @Override
    public void authorized() {
      if(reportTemplate.hasProject()) {
        getView().setVisiblePermissionsPanel(true);
        ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
        resourcePermissionsPresenter.initialize(ResourcePermissionType.REPORT_TEMPLATE,
            ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_REPORTTEMPLATE, reportTemplate.getProject(),
            reportTemplate.getName());
        setInSlot(null, resourcePermissionsPresenter);
      } else {
        getView().setVisiblePermissionsPanel(false);
      }
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
