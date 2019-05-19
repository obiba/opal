/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.task.presenter;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class TasksPresenter extends PresenterWidget<TasksPresenter.Display> {

  public static final String LOG_ACTION = "Log";

  public static final String CANCEL_ACTION = "Cancel";

  private String project;

  private final ModalProvider<TaskDetailsPresenter> jobDetailsModalProvider;

  private Runnable actionRequiringConfirmation;

  private TranslationMessages translationMessages;

  @Inject
  public TasksPresenter(Display display, EventBus eventBus, ModalProvider<TaskDetailsPresenter> jobDetailsModalProvider,
      TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.jobDetailsModalProvider = jobDetailsModalProvider.setContainer(this);
    getView().getActionsColumn().setActionHandler(new ActionHandler<CommandStateDto>() {
      @Override
      public void doAction(CommandStateDto dto, String actionName) {
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerHandler(getView().addClearButtonHandler(new ClearButtonHandler()));
    registerHandler(getView().addRefreshButtonHandler(new RefreshButtonHandler()));
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
  }

  @Override
  public void onReveal() {
    super.onReveal();
    updateTable();
  }

  public boolean containsClearableJobs(JsArray<CommandStateDto> jobs) {
    for(int i = 0; i < jobs.length(); i++) {
      CommandStateDto job = jobs.get(i);
      if("SUCCEEDED".equals(job.getStatus().toString()) || "FAILED".equals(job.getStatus().toString()) ||
          "CANCELED".equals(job.getStatus().toString())) {
        return true;
      }
    }
    return false;
  }

  private void updateTable() {
    UriBuilder uriBuilder = UriBuilder.create();
    if(project != null && !project.isEmpty()) {
      uriBuilder.segment("project", project, "commands");
    } else {
      uriBuilder.segment("shell", "commands");
    }
    ResourceRequestBuilderFactory.<JsArray<CommandStateDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<JsArray<CommandStateDto>>() {
          @Override
          public void onResource(Response response, JsArray<CommandStateDto> resource) {
            getView().renderRows(resource);
            getView().showClearJobsButton(containsClearableJobs(resource));
          }

        }).withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        // ignore
      }
    }, Response.SC_FORBIDDEN).send();
  }

  private void authorizeCancelJob(CommandStateDto dto, Authorizer authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/command/" + dto.getId() + "/status")
        .put().authorize(authorizer).send();
  }

  private void doActionImpl(final CommandStateDto dto, String actionName) {
    if(LOG_ACTION.equals(actionName)) {
      TaskDetailsPresenter taskDetailsPresenter = jobDetailsModalProvider.get();
      taskDetailsPresenter.setJob(dto);
    } else if(CANCEL_ACTION.equals(actionName)) {
      authorizeCancelJob(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          cancelJob(dto);
        }
      });
    }
  }

  private void cancelJob(final CommandStateDto dto) {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(ConfirmationTerminatedEvent.create());
            if(response.getStatusCode() == 200) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().info("jobCancelled").build());
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
            updateTable();
          }
        };
        UriBuilder uriBuilder = UriBuilder.create();
        uriBuilder.segment("shell", "command", dto.getId() + "", "status");
        ResourceRequestBuilderFactory.<JsArray<CommandStateDto>>newBuilder().forResource(uriBuilder.build()).put()
            .withBody("text/plain", "CANCELED").withCallback(400, callbackHandler).withCallback(404, callbackHandler)
            .withCallback(200, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.cancelJob(),
            translationMessages.confirmCancelJob()));
  }

  private void deleteCompletedJobs() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(ConfirmationTerminatedEvent.create());
            updateTable();
          }
        };

        ResourceRequestBuilderFactory.<JsArray<CommandStateDto>>newBuilder().forResource("/shell/commands/completed")
            .delete().withCallback(200, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.clearJobsList(),
            translationMessages.confirmClearJobsList()));
  }

  public void showProject(String project) {
    this.project = project;
    getView().inProject(project != null);
    updateTable();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    void renderRows(JsArray<CommandStateDto> rows);

    void showClearJobsButton(boolean show);

    HasActionHandler<CommandStateDto> getActionsColumn();

    HandlerRegistration addClearButtonHandler(ClickHandler handler);

    HandlerRegistration addRefreshButtonHandler(ClickHandler handler);

    void inProject(boolean b);
  }

  class ClearButtonHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      deleteCompletedJobs();
    }
  }

  class RefreshButtonHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      updateTable();
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
}
