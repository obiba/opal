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
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ReportTemplateDetailsPresenter extends WidgetPresenter<ReportTemplateDetailsPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String DOWNLOAD_ACTION = "Download";

  private Runnable actionRequiringConfirmation;

  public interface Display extends WidgetDisplay {
    void setProducedReports(JsArray<FileDto> reports);

    HasActionHandler getActionColumn();
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
        ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files/meta/reports/" + event.getReportTemplate().getName()).get().withCallback(new ProducedReportsResourceCallback()).withCallback(404, new NoProducedReportsResourceCallback()).send();
      }
    }));

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
    String url = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "")).append("ws/files").append(file.getPath()).toString();
    eventBus.fireEvent(new FileDownloadEvent(url));
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
