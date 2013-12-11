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
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
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
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.ReportDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
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

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private ReportTemplateDto reportTemplate;

  @Inject
  public ReportTemplateDetailsPresenter(Display display, EventBus eventBus,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider) {
    super(eventBus, display);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
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
    addRegisteredHandler(ReportTemplateUpdatedEvent.getType(), new ReportTemplateUpdatedHandler());
  }

  private void authorize() {
    // display reports
    String uri;
    if (reportTemplate.hasProject()) {
      uri= UriBuilder.create().segment("files", "meta", "reports", reportTemplate.getProject(), reportTemplate.getName()).build();
    } else {
      uri= UriBuilder.create().segment("files", "meta", "reports", reportTemplate.getName()).build();
    }

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(uri).get()
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
          fireEvent(new FileDownloadRequestEvent(dto.getLink()));
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
          fireEvent(
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

  private void refreshProducedReports(ReportTemplateDto reportTemplate) {
    String uri = UriBuilder.create().segment("report-template", reportTemplate.getName(), "reports").build();
    if (reportTemplate.hasProject()) {
      uri = UriBuilders.PROJECT_REPORT_TEMPLATE_REPORTS.create().build(reportTemplate.getProject(), reportTemplate.getName());
    }
    ResourceRequestBuilderFactory.<JsArray<ReportDto>>newBuilder().forResource(uri).get()
        .withCallback(new ProducedReportsResourceCallback()).withCallback(Response.SC_NOT_FOUND, new NoProducedReportsResourceCallback())
        .send();
  }

  private void refreshReportTemplateDetails(ReportTemplateDto reportTemplate) {
    String reportTemplateName = reportTemplate.getName();
    String uri = UriBuilder.create().segment("report-template", reportTemplateName).build();
    if (reportTemplate.hasProject()) {
      uri = UriBuilders.PROJECT_REPORT_TEMPLATE.create().build(reportTemplate.getProject(), reportTemplateName);
    }

    ResourceRequestBuilderFactory.<ReportTemplateDto>newBuilder().forResource(uri).get()
        .withCallback(new ReportTemplateFoundCallBack())
        .withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
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
      if (reportTemplate.hasProject()) {
        getView().setVisiblePermissionsPanel(true);
        ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
        resourcePermissionsPresenter.initialize(ResourcePermissionType.REPORT_TEMPLATE, ResourcePermissionRequestPaths
            .reportTemplatePermissions(reportTemplate.getProject(), reportTemplate.getName()));
        setInSlot(null, resourcePermissionsPresenter);
      } else {
        getView().setVisiblePermissionsPanel(false);
      }
    }
  }

  public interface Display extends View {

    void setProducedReports(JsArray<ReportDto> reports);

    HasActionHandler<ReportDto> getActionColumn();

    HandlerRegistration addReportDesignClickHandler(ClickHandler handler);

    void setReportTemplateDetails(@Nullable ReportTemplateDto reportTemplate);

    ReportTemplateDto getReportTemplateDetails();

    HasAuthorization getListReportsAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    void setVisiblePermissionsPanel(boolean value);
  }

}
