/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.job.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.inject.client.GwtEventBusAdaptor;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 *
 */
public class JobListPresenter extends Presenter<JobListPresenter.Display, JobListPresenter.Proxy> {
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
  public JobListPresenter(Display display, EventBus eventBus, Proxy proxy, JobDetailsPresenter jobDetailsPresenter) {
    super(eventBus, display, proxy);

    this.jobDetailsPresenter = jobDetailsPresenter;

    getView().getActionsColumn().setActionHandler(new ActionHandler<CommandStateDto>() {
      public void doAction(CommandStateDto dto, String actionName) {
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, ApplicationPresenter.WORKBENCH, this);
  }

  @Override
  protected void onBind() {
    super.registerHandler(getView().addClearButtonHandler(new ClearButtonHandler()));
    super.registerHandler(getView().addRefreshButtonHandler(new RefreshButtonHandler()));
    super.registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void onReset() {
    updateTable();
  }

  @Override
  public void onReveal() {
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
        getView().renderRows(resource);
        getView().showClearJobsButton(containsClearableJobs(resource));
      }

    }).send();
  }

  private void authorizeCancelJob(CommandStateDto dto, Authorizer authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/command/" + dto.getId() + "/status").put().authorize(authorizer).send();
  }

  private void doActionImpl(final CommandStateDto dto, String actionName) {
    if(LOG_ACTION.equals(actionName)) {
      jobDetailsPresenter.getDisplay().showDialog(dto);
    } else if(CANCEL_ACTION.equals(actionName)) {
      authorizeCancelJob(dto, new Authorizer(new GwtEventBusAdaptor(getEventBus())) {

        @Override
        public void authorized() {
          cancelJob(dto);
        }
      });
    }
  }

  private void cancelJob(final CommandStateDto dto) {
    actionRequiringConfirmation = new Runnable() {
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == 200) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().info("jobCancelled").build());
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
            updateTable();
          }
        };

        ResourceRequestBuilderFactory.<JsArray<CommandStateDto>> newBuilder().forResource("/shell/command/" + dto.getId() + "/status").put().withBody("text/plain", "CANCELED").withCallback(400, callbackHandler).withCallback(404, callbackHandler).withCallback(200, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "cancelJob", "confirmCancelJob"));
  }

  private void deleteCompletedJobs() {
    actionRequiringConfirmation = new Runnable() {
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            updateTable();
          }
        };

        ResourceRequestBuilderFactory.<JsArray<CommandStateDto>> newBuilder().forResource("/shell/commands/completed").delete().withCallback(200, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "clearJobsList", "confirmClearJobsList"));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    SelectionModel<CommandStateDto> getTableSelection();

    void renderRows(JsArray<CommandStateDto> rows);

    void showClearJobsButton(boolean show);

    HasActionHandler<CommandStateDto> getActionsColumn();

    HandlerRegistration addClearButtonHandler(ClickHandler handler);

    HandlerRegistration addRefreshButtonHandler(ClickHandler handler);
  }

  @ProxyStandard
  @NameToken(Places.jobs)
  public interface Proxy extends ProxyPlace<JobListPresenter> {
  }

  class ClearButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      deleteCompletedJobs();
    }
  }

  class RefreshButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      updateTable();
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
