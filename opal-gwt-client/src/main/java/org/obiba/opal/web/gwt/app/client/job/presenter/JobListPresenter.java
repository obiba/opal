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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
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
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 *
 */
public class JobListPresenter extends ItemAdministrationPresenter<JobListPresenter.Display, JobListPresenter.Proxy> {

  public static final String LOG_ACTION = "Log";

  public static final String CANCEL_ACTION = "Cancel";

  private final ModalProvider<JobDetailsPresenter> jobDetailsModalProvider;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private Runnable actionRequiringConfirmation;

  @Inject
  public JobListPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<JobDetailsPresenter> jobDetailsModalProvider, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);

    this.jobDetailsModalProvider = jobDetailsModalProvider.setContainer(this);
    this.breadcrumbsHelper = breadcrumbsHelper;

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
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerHandler(getView().addClearButtonHandler(new ClearButtonHandler()));
    registerHandler(getView().addRefreshButtonHandler(new RefreshButtonHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
  }

  @Override
  public void onReset() {
    updateTable();
  }

  @Override
  public void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
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
    ResourceRequestBuilderFactory.<JsArray<CommandStateDto>>newBuilder().forResource("/shell/commands").get()
        .withCallback(new ResourceCallback<JsArray<CommandStateDto>>() {
          @Override
          public void onResource(Response response, JsArray<CommandStateDto> resource) {
            getView().renderRows(resource);
            getView().showClearJobsButton(containsClearableJobs(resource));
          }

        }).send();
  }

  private void authorizeCancelJob(CommandStateDto dto, Authorizer authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/command/" + dto.getId() + "/status")
        .put().authorize(authorizer).send();
  }

  private void doActionImpl(final CommandStateDto dto, String actionName) {
    if(LOG_ACTION.equals(actionName)) {
      JobDetailsPresenter jobDetailsPresenter = jobDetailsModalProvider.get();
      jobDetailsPresenter.setJob(dto);
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

    getEventBus().fireEvent(
        ConfirmationRequiredEvent.createWithKeys(actionRequiringConfirmation, "cancelJob", "confirmCancelJob"));
  }

  private void deleteCompletedJobs() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            updateTable();
          }
        };

        ResourceRequestBuilderFactory.<JsArray<CommandStateDto>>newBuilder().forResource("/shell/commands/completed")
            .delete().withCallback(200, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(
        ConfirmationRequiredEvent.createWithKeys(actionRequiringConfirmation, "clearJobsList", "confirmClearJobsList"));
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageJobsTitle();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View, HasBreadcrumbs {

    @Nullable
    SelectionModel<CommandStateDto> getTableSelection();

    void renderRows(JsArray<CommandStateDto> rows);

    void showClearJobsButton(boolean show);

    HasActionHandler<CommandStateDto> getActionsColumn();

    HandlerRegistration addClearButtonHandler(ClickHandler handler);

    HandlerRegistration addRefreshButtonHandler(ClickHandler handler);
  }

  @ProxyStandard
  @NameToken(Places.jobs)
  public interface Proxy extends ProxyPlace<JobListPresenter> {}

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
