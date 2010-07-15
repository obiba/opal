/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import java.util.Arrays;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

/**
 *
 */
public class JobListPresenter extends WidgetPresenter<JobListPresenter.Display> {
  //
  // Constants
  //

  public static final String LOG_ACTION = "Log";

  public static final String CANCEL_ACTION = "Cancel";

  //
  // Instance Variables
  //

  private JobDetailsPresenter jobDetailsPresenter;

  private Runnable actionRequiringConfirmation;

  //
  // Constructors
  //

  @Inject
  public JobListPresenter(Display display, EventBus eventBus, JobDetailsPresenter jobDetailsPresenter) {
    super(display, eventBus);

    this.jobDetailsPresenter = jobDetailsPresenter;

    getDisplay().getActionsColumn().setActionHandler(new ActionHandler() {
      public void doAction(CommandStateDto dto, String actionName) {
        System.out.println("Actions Column fieldUpdater.update = " + actionName);
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    super.registerHandler(getDisplay().addClearButtonHandler(new ClearButtonHandler()));
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
    updateTable();
  }

  @Override
  public void revealDisplay() {
    updateTable();
  }

  //
  // Methods
  //

  public boolean containsClearableJobs(JsArray<CommandStateDto> jobs) {
    for(int i = 0; i < jobs.length(); i++) {
      CommandStateDto job = jobs.get(i);
      if(job.getStatus().toString().equals("SUCCEEDED") || job.getStatus().toString().equals("FAILED") || job.getStatus().toString().equals("CANCELED")) {
        return true;
      }
    }
    return false;
  }

  private void updateTable() {
    ResourceRequestBuilderFactory.<JsArray<CommandStateDto>> newBuilder().forResource("/shell/commands").get().withCallback(new ResourceCallback<JsArray<CommandStateDto>>() {
      @Override
      public void onResource(Response response, JsArray<CommandStateDto> resource) {
        getDisplay().renderRows(resource);
        getDisplay().showClearJobsButton(containsClearableJobs(resource));
      }

    }).send();
  }

  private void doActionImpl(CommandStateDto dto, String actionName) {
    if(LOG_ACTION.equals(actionName)) {
      jobDetailsPresenter.getDisplay().showDialog(dto);
    } else if(CANCEL_ACTION.equals(actionName)) {
      cancelJob(dto);
    }
  }

  private void cancelJob(final CommandStateDto dto) {
    actionRequiringConfirmation = new Runnable() {
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == 200) {
              eventBus.fireEvent(new UserMessageEvent(MessageDialogType.INFO, "jobCancelled", Arrays.asList(new String[] { String.valueOf(dto.getId()) })));
            } else {
              eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, response.getText(), null));
            }
            refreshDisplay();
          }
        };

        ResourceRequestBuilderFactory.<JsArray<CommandStateDto>> newBuilder().forResource("/shell/command/" + dto.getId() + "/status").put().withBody("text/plain", "CANCELED").withCallback(400, callbackHandler).withCallback(404, callbackHandler).withCallback(200, callbackHandler).send();
      }
    };

    eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "cancelJob", "confirmCancelJob"));
  }

  private void deleteCompletedJobs() {
    actionRequiringConfirmation = new Runnable() {
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == 200) {
              eventBus.fireEvent(new UserMessageEvent(MessageDialogType.INFO, "completedJobsDeleted", null));
            }
            refreshDisplay();
          }
        };

        ResourceRequestBuilderFactory.<JsArray<CommandStateDto>> newBuilder().forResource("/shell/commands/completed").delete().withCallback(200, callbackHandler).send();
      }
    };

    eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "clearJobsList", "confirmClearJobsList"));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    SelectionModel<CommandStateDto> getTableSelection();

    void renderRows(JsArray<CommandStateDto> rows);

    void showClearJobsButton(boolean show);

    HasActionHandler getActionsColumn();

    HandlerRegistration addClearButtonHandler(ClickHandler handler);
  }

  public interface HasActionHandler {

    void setActionHandler(ActionHandler handler);
  }

  public interface ActionHandler {

    void doAction(CommandStateDto dto, String actionName);
  }

  class ClearButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      deleteCompletedJobs();
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
}
