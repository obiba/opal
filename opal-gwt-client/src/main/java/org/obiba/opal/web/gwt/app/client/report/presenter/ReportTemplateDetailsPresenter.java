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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateListReceivedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ReportTemplateDetailsPresenter extends WidgetPresenter<ReportTemplateDetailsPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String DOWNLOAD_ACTION = "Download";

  private Runnable actionRequiringConfirmation;

  private Provider<ReportTemplateUpdateDialogPresenter> reportTemplateUpdateDialogPresenterProvider;

  private ReportTemplateDto reportTemplate;

  public interface Display extends WidgetDisplay {

    void setReportTemplatesAvailable(boolean available);

    void setProducedReports(JsArray<FileDto> reports);

    HasActionHandler<FileDto> getActionColumn();

    HandlerRegistration addReportDesignClickHandler(ClickHandler handler);

    void setReportTemplateDetails(ReportTemplateDto reportTemplate);

    ReportTemplateDto getReportTemplateDetails();

    void setRemoveReportTemplateCommand(Command command);

    void setRunReportCommand(Command command);

    void setUpdateReportTemplateCommand(Command command);

    HasAuthorization getRemoveReportTemplateAuthorizer();

    HasAuthorization getRunReportAuthorizer();

    HasAuthorization getUpdateReportTemplateAuthorizer();

    HasAuthorization getListReportsAuthorizer();
  }

  @Inject
  public ReportTemplateDetailsPresenter(final Display display, final EventBus eventBus, Provider<ReportTemplateUpdateDialogPresenter> reportTemplateUpdateDialogPresenterProvider) {
    super(display, eventBus);
    this.reportTemplateUpdateDialogPresenterProvider = reportTemplateUpdateDialogPresenterProvider;
  }

  @Override
  public void refreshDisplay() {
    if(reportTemplate != null) {
      refreshProducedReports(reportTemplate);
    }
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    initUiComponents();
    addHandlers();
    setCommands();
  }

  @Override
  protected void onUnbind() {
    reportTemplate = null;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @SuppressWarnings("unchecked")
  private void initUiComponents() {
    getDisplay().setProducedReports((JsArray<FileDto>) JsArray.createArray());
    getDisplay().setReportTemplateDetails(null);
  }

  private void addHandlers() {
    getDisplay().getActionColumn().setActionHandler(new ActionHandler<FileDto>() {
      public void doAction(FileDto dto, String actionName) {
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });

    super.registerHandler(eventBus.addHandler(ReportTemplateSelectedEvent.getType(), new ReportTemplateSelectedEvent.Handler() {

      @Override
      public void onReportTemplateSelected(ReportTemplateSelectedEvent event) {
        ReportTemplateDto reportTemplate = event.getReportTemplate();
        if(reportTemplate != null) {
          refreshReportTemplateDetails(reportTemplate);
        } else {
          getDisplay().setReportTemplateDetails(null);
        }
      }
    }));

    super.registerHandler(eventBus.addHandler(FileDeletedEvent.getType(), new FileDeletedEvent.Handler() {

      @Override
      public void onFileDeleted(FileDeletedEvent event) {
        refreshProducedReports(reportTemplate);
      }
    }));

    super.registerHandler(getDisplay().addReportDesignClickHandler(new ReportDesignClickHandler()));
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    super.registerHandler(eventBus.addHandler(ReportTemplateUpdatedEvent.getType(), new ReportTemplateUpdatedHandler()));
    super.registerHandler(eventBus.addHandler(ReportTemplateListReceivedEvent.getType(), new ReportTemplateListReceivedEventHandler()));
  }

  private void setCommands() {
    getDisplay().setRunReportCommand(new RunReportCommand());
    getDisplay().setRemoveReportTemplateCommand(new RemoveReportTemplateCommand());
    getDisplay().setUpdateReportTemplateCommand(new EditReportTemplateCommand());
  }

  private void authorize() {
    // run report
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/report").post().authorize(getDisplay().getRunReportAuthorizer()).send();
    // remove
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-template/" + reportTemplate.getName()).delete().authorize(getDisplay().getRemoveReportTemplateAuthorizer()).send();

    // edit
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-template/" + reportTemplate.getName()).put().authorize(getDisplay().getUpdateReportTemplateAuthorizer()).send();

    // display reports
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files/meta/reports/" + reportTemplate.getName()).get().authorize(getDisplay().getListReportsAuthorizer()).send();
  }

  protected void doActionImpl(final FileDto dto, String actionName) {
    if(actionName.equals(DOWNLOAD_ACTION)) {
      downloadFile(dto);
    } else if(actionName.equals(DELETE_ACTION)) {
      actionRequiringConfirmation = new Runnable() {
        public void run() {
          deleteFile(dto);
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "deleteFile", "confirmDeleteFile"));
    }
  }

  private void deleteFile(final FileDto file) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() != Response.SC_OK) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
        } else {
          eventBus.fireEvent(new FileDeletedEvent(file));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/files" + file.getPath()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void downloadFile(final FileDto file) {
    downloadFile(file.getPath());
  }

  private void downloadFile(String filePath) {
    String url = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "")).append("ws/files").append(filePath).toString();
    eventBus.fireEvent(new FileDownloadEvent(url));
  }

  private void refreshProducedReports(ReportTemplateDto reportTemplate) {
    ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files/meta/reports/" + reportTemplate.getName()).get().withCallback(new ProducedReportsResourceCallback()).withCallback(404, new NoProducedReportsResourceCallback()).send();
  }

  private void refreshReportTemplateDetails(ReportTemplateDto reportTemplate) {
    String reportTemplateName = reportTemplate.getName();
    ResourceRequestBuilderFactory.<ReportTemplateDto> newBuilder().forResource("/report-template/" + reportTemplate.getName()).get().withCallback(new ReportTemplateFoundCallBack()).withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
  }

  private class RunReportCommand implements Command {

    @Override
    public void execute() {
      ResponseCodeCallback callbackHandler = new CommandResponseCallBack();
      ReportCommandOptionsDto reportCommandOptions = ReportCommandOptionsDto.create();
      reportCommandOptions.setName(getDisplay().getReportTemplateDetails().getName());
      ResourceRequestBuilderFactory.newBuilder().forResource("/shell/report").post().withResourceBody(ReportCommandOptionsDto.stringify(reportCommandOptions)).withCallback(Response.SC_CREATED, callbackHandler).send();
    }

  }

  private class EditReportTemplateCommand implements Command {

    @Override
    public void execute() {
      ReportTemplateUpdateDialogPresenter presenter = reportTemplateUpdateDialogPresenterProvider.get();
      presenter.bind();
      presenter.setDialogMode(Mode.UPDATE);
      ReportTemplateUpdateDialogPresenter.Display display = presenter.getDisplay();
      ReportTemplateDto reportTemplate = getDisplay().getReportTemplateDetails();
      display.setDesignFile(reportTemplate.getDesign());
      display.setFormat(reportTemplate.getFormat());
      display.setName(reportTemplate.getName());
      display.setNotificationEmails(asList(JsArrays.toSafeArray(reportTemplate.getEmailNotificationArray())));
      display.setReportParameters(asList(JsArrays.toSafeArray(reportTemplate.getParametersArray())));
      display.setSchedule(reportTemplate.getCron());
      display.setEnabledReportTemplateName(false);
      presenter.revealDisplay();
    }

  }

  private List<String> asList(JsArray<ParameterDto> parametersArray) {
    List<String> list = new ArrayList<String>();
    for(int i = 0; i < parametersArray.length(); i++) {
      list.add(parametersArray.get(i).getKey() + "=" + parametersArray.get(i).getValue());
    }
    return list;
  }

  private List<String> asList(JsArrayString array) {
    List<String> list = new ArrayList<String>();
    for(int i = 0; i < array.length(); i++) {
      list.add(array.get(i));
    }
    return list;
  }

  private class RemoveReportTemplateCommand implements Command {

    @Override
    public void execute() {
      actionRequiringConfirmation = new Runnable() {
        public void run() {
          String reportTemplateName = getDisplay().getReportTemplateDetails().getName();
          ResourceRequestBuilderFactory.newBuilder().forResource("/report-template/" + reportTemplateName).delete().withCallback(Response.SC_OK, new RemoveReportTemplateResponseCallBack()).withCallback(Response.SC_NOT_FOUND, new ReportTemplateNotFoundCallBack(reportTemplateName)).send();
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "removeReportTemplate", "confirmDeleteReportTemplate"));
    }

  }

  private class ReportTemplateUpdatedHandler implements ReportTemplateUpdatedEvent.Handler {

    @Override
    public void onReportTemplateUpdated(ReportTemplateUpdatedEvent event) {
      refreshReportTemplateDetails(event.getReportTemplate());
    }

  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private class ReportDesignClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      downloadFile(getDisplay().getReportTemplateDetails().getDesign());
    }

  }

  private class NoProducedReportsResourceCallback implements ResponseCodeCallback {

    @SuppressWarnings("unchecked")
    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().setProducedReports((JsArray<FileDto>) JsArray.createArray());
    }
  }

  private class ProducedReportsResourceCallback implements ResourceCallback<FileDto> {

    @Override
    public void onResource(Response response, FileDto reportFolder) {
      getDisplay().setProducedReports(JsArrays.toSafeArray(reportFolder.getChildrenArray()));
    }
  }

  private class CommandResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.INFO, "ReportJobStarted", null));
      }
    }
  }

  private class ReportTemplateFoundCallBack implements ResourceCallback<ReportTemplateDto> {

    @Override
    public void onResource(Response response, ReportTemplateDto resource) {
      reportTemplate = resource;
      getDisplay().setReportTemplateDetails(reportTemplate);
      refreshProducedReports(reportTemplate);
      authorize();
    }
  }

  private class ReportTemplateNotFoundCallBack implements ResponseCodeCallback {

    private String templateName;

    public ReportTemplateNotFoundCallBack(String reportTemplateName) {
      this.templateName = reportTemplateName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "ReportTemplateCannotBeFound", Arrays.asList(new String[] { templateName })));
    }

  }

  private class RemoveReportTemplateResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(new ReportTemplateDeletedEvent(getDisplay().getReportTemplateDetails()));
    }

  }

  class ReportTemplateListReceivedEventHandler implements ReportTemplateListReceivedEvent.Handler {

    @Override
    public void onReportTemplateListReceived(ReportTemplateListReceivedEvent event) {
      getDisplay().setReportTemplatesAvailable(event.getReportTemplates().length() != 0);
    }
  }
}
