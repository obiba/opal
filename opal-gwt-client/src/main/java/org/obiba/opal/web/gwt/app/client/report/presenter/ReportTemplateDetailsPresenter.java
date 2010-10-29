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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.ReportCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;

public class ReportTemplateDetailsPresenter extends WidgetPresenter<ReportTemplateDetailsPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String DOWNLOAD_ACTION = "Download";

  private Runnable actionRequiringConfirmation;

  public interface Display extends WidgetDisplay {
    void setProducedReports(JsArray<FileDto> reports);

    HasActionHandler getActionColumn();

    HandlerRegistration addReportDesignClickHandler(ClickHandler handler);

    void setReportTemplateDetails(ReportTemplateDto reportTemplate);

    ReportTemplateDto getReportTemplateDetails();

    void setRemoveReportTemplateCommand(Command command);

    void setRunReportCommand(Command command);

    void setUpdateReportTemplateCommand(Command command);
  }

  public interface ActionHandler {
    void doAction(FileDto dto, String actionName);
  }

  public interface HasActionHandler {
    void setActionHandler(ActionHandler handler);
  }

  @Inject
  public ReportTemplateDetailsPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
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
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void initUiComponents() {
    getDisplay().setProducedReports(null);
  }

  private void addHandlers() {
    getDisplay().getActionColumn().setActionHandler(new ActionHandler() {
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
        ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files/meta/reports/" + reportTemplate.getName()).get().withCallback(new ProducedReportsResourceCallback()).withCallback(404, new NoProducedReportsResourceCallback()).send();
        getDisplay().setReportTemplateDetails(reportTemplate);
      }
    }));

    super.registerHandler(getDisplay().addReportDesignClickHandler(new ReportDesignClickHandler()));
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));

  }

  private void setCommands() {
    getDisplay().setRemoveReportTemplateCommand(new RemoveReportTemplateCommand());
    getDisplay().setRunReportCommand(new RunReportCommand());
    getDisplay().setUpdateReportTemplateCommand(new UpdateReportTemplateCommand());
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

  private class RunReportCommand implements Command {

    @Override
    public void execute() {
      ResponseCodeCallback callbackHandler = new CommandResponseCallBack();
      ReportCommandOptionsDto reportCommandOptions = ReportCommandOptionsDto.create();
      reportCommandOptions.setName(getDisplay().getReportTemplateDetails().getName());
      ResourceRequestBuilderFactory.newBuilder().forResource("/shell/report").post().withResourceBody(ReportCommandOptionsDto.stringify(reportCommandOptions)).withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_CREATED, callbackHandler).send();
      eventBus.fireEvent(new NotificationEvent(NotificationType.INFO, "ReportJobStarted", null));
    }

  }

  private class UpdateReportTemplateCommand implements Command {

    @Override
    public void execute() {
      // TODO Implement the edit/update report template dialog.
      eventBus.fireEvent(new NotificationEvent(NotificationType.WARNING, "Showing the edit/update report template dialog (not implemented yet)", null));
    }

  }

  private class RemoveReportTemplateCommand implements Command {

    @Override
    public void execute() {
      actionRequiringConfirmation = new Runnable() {
        public void run() {
          ResponseCodeCallback callbackHandler = new CommandResponseCallBack();
          ResourceRequestBuilderFactory.newBuilder().forResource("/report-template/" + getDisplay().getReportTemplateDetails().getName()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
          eventBus.fireEvent(new ReportTemplateDeletedEvent(getDisplay().getReportTemplateDetails()));
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "removeReportTemplate", "confirmDeleteReportTemplate"));
    }

  }

  private class CommandResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() != Response.SC_OK) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
      }
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

    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().setProducedReports(null);
    }
  }

  private class ProducedReportsResourceCallback implements ResourceCallback<FileDto> {

    @Override
    public void onResource(Response response, FileDto reportFolder) {
      getDisplay().setProducedReports(reportFolder.getChildrenArray());
    }

  }

}
