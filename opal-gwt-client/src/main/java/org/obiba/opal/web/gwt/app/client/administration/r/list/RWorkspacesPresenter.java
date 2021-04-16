/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.r.list;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.r.RWorkspaceDto;

/**
 *
 */
public class RWorkspacesPresenter extends PresenterWidget<RWorkspacesPresenter.Display> implements  RWorkspacesUiHandlers {

  public static final String REMOVE_ACTION = "Remove";

  private Runnable actionRequiringConfirmation;

  private final TranslationMessages translationMessages;

  @Inject
  public RWorkspacesPresenter(Display display, EventBus eventBus, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
  }

  @Override
  public void onReveal() {
    super.onReveal();
    onRefresh();
  }

  @Override
  public void onRefresh() {
    ResourceRequestBuilderFactory.<JsArray<RWorkspaceDto>>newBuilder().forResource("/service/r/workspaces").get()
        .withCallback(new ResourceCallback<JsArray<RWorkspaceDto>>() {
          @Override
          public void onResource(Response response, JsArray<RWorkspaceDto> resource) {
            getView().renderRows(resource);
          }

        }).withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        // ignore
      }
    }, Response.SC_FORBIDDEN).send();
  }

  @Override
  public void onRemove(final RWorkspaceDto workspace) {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(ConfirmationTerminatedEvent.create());
            if(response.getStatusCode() == Response.SC_OK) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().info("rWorkspaceRemoved").build());
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
            onRefresh();
          }
        };
        UriBuilder uriBuilder = UriBuilder.create();
        uriBuilder.segment("service", "r", "workspaces").query("context", workspace.getContext())
        .query("user", workspace.getUser()).query("name", workspace.getName());
        ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).delete()
            .withCallback(Response.SC_OK, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeWorkspace(),
            translationMessages.confirmRemoveRWorkspace()));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View, HasUiHandlers<RWorkspacesUiHandlers> {

    void renderRows(JsArray<RWorkspaceDto> rows);

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
