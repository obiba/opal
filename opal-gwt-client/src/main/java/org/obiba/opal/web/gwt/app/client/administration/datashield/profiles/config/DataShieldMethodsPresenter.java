/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageRemovedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.DataShieldProfilePresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class DataShieldMethodsPresenter extends PresenterWidget<DataShieldMethodsPresenter.Display> implements DataShieldMethodsUiHandlers {

  private String env;

  private Runnable removeMethodConfirmation;

  private Runnable removeMethodsConfirmation;

  private final ModalProvider<DataShieldMethodModalPresenter> methodModalProvider;

  private final TranslationMessages translationMessages;

  private DataShieldProfileDto profile;

  @Inject
  public DataShieldMethodsPresenter(Display display, EventBus eventBus,
                                    ModalProvider<DataShieldMethodModalPresenter> methodModalProvider, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.methodModalProvider = methodModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void setEnvironment(String env) {
    this.env = env;
    getView().setEnvironment(env);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
  }

  private void addEventHandlers() {
    getView().setMethodActionHandler(new ActionHandler<DataShieldMethodDto>() {
      @Override
      public void doAction(DataShieldMethodDto dto, String actionName) {
        if (actionName != null) {
          doDataShieldMethodActionImpl(dto, actionName);
        }
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
    addRegisteredHandler(DataShieldMethodCreatedEvent.getType(),
        new DataShieldMethodCreatedEvent.DataShieldMethodCreatedHandler() {

          @Override
          public void onDataShieldMethodCreated(DataShieldMethodCreatedEvent event) {
            if (profile.getName().equals(event.getProfile()))
              updateDataShieldMethods();
          }
        });
    addRegisteredHandler(DataShieldMethodUpdatedEvent.getType(),
        new DataShieldMethodUpdatedEvent.DataShieldMethodUpdatedHandler() {

          @Override
          public void onDataShieldMethodUpdated(DataShieldMethodUpdatedEvent event) {
            if (profile.getName().equals(event.getProfile()))
              updateDataShieldMethods();
          }

        });
    addRegisteredHandler(DataShieldPackageRemovedEvent.getType(),
        new DataShieldPackageRemovedEvent.DataShieldPackageRemovedHandler() {
          @Override
          public void onDataShieldPackageRemoved(DataShieldPackageRemovedEvent event) {
            if (profile.getCluster().equals(event.getCluster()))
              updateDataShieldMethods();
          }
        });
    addRegisteredHandler(DataShieldPackageUpdatedEvent.getType(),
        new DataShieldPackageUpdatedEvent.DataShieldPackageUpdatedHandler() {
          @Override
          public void onDataShieldPackageUpdated(DataShieldPackageUpdatedEvent event) {
            if (profile.getCluster().equals(event.getCluster()))
              updateDataShieldMethods();
          }
        });
  }

  @Override
  protected void onReveal() {
    authorize();
  }

  private void authorize() {
    // view methods
    authorizeMethods(new CompositeAuthorizer(getView().getMethodsAuthorizer(), new MethodsUpdate()));
    // create method
    authorizeAddMethod(getView().getAddMethodAuthorizer());
  }

  private String methods() {
    return UriBuilder.create().segment("datashield", "env", "{env}", "methods")
        .query("profile", profile.getName())
        .build(env);
  }

  private String method(String method) {
    return UriBuilder.create().segment("datashield", "env", "{env}", "method", "{method}")
        .query("profile", profile.getName())
        .build(env, method);
  }

  private void authorizeMethods(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(methods()).get().authorize(authorizer).send();
  }

  private void authorizeAddMethod(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(methods()).post().authorize(authorizer).send();
  }

  private void updateDataShieldMethods() {
    ResourceRequestBuilderFactory.<JsArray<DataShieldMethodDto>>newBuilder().forResource(methods()).get()//
        .withCallback(new ResourceCallback<JsArray<DataShieldMethodDto>>() {

          @Override
          public void onResource(Response response, JsArray<DataShieldMethodDto> resource) {
            getView().showDataShieldMethods(JsArrays.toList(resource));
          }
        }).send();
  }

  protected void doDataShieldMethodActionImpl(final DataShieldMethodDto dto, String actionName) {
    if (actionName.equals(EDIT_ACTION)) {
      DataShieldMethodModalPresenter presenter = methodModalProvider.get();
      presenter.initialize(profile, env);
      presenter.updateMethod(dto);
    } else if (actionName.equals(REMOVE_ACTION)) {
      removeMethodConfirmation = new Runnable() {
        @Override
        public void run() {
          deleteDataShieldMethod(dto);
        }
      };
      if (DataShieldProfilePresenter.DataShieldEnvironment.ASSIGN.equals(env)) {
        getEventBus().fireEvent(ConfirmationRequiredEvent
            .createWithMessages(removeMethodConfirmation, translationMessages.removeDataShieldAssignMethod(),
                translationMessages.confirmDeleteDataShieldAssignMethod(dto.getName())));
      } else {
        getEventBus().fireEvent(ConfirmationRequiredEvent
            .createWithMessages(removeMethodConfirmation, translationMessages.removeDataShieldAggregateMethod(),
                translationMessages.confirmDeleteDataShieldAggregateMethod(dto.getName())));
      }
    }
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void deleteDataShieldMethod(DataShieldMethodDto dto) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        fireEvent(ConfirmationTerminatedEvent.create());
        if (response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NOT_FOUND) {
          updateDataShieldMethods();
        } else {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error("Failed removing aggregating method.").build());
        }
      }

    };
    ResourceRequestBuilderFactory.newBuilder().forResource(method(dto.getName())).delete() //
        .withCallback(Response.SC_OK, callbackHandler) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
        .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void deleteDataShieldMethods(List<DataShieldMethodDto> dtos) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        fireEvent(ConfirmationTerminatedEvent.create());
        if (response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NOT_FOUND) {
          updateDataShieldMethods();
        } else {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error("Failed removing methods.").build());
        }
      }

    };
    UriBuilder builder = UriBuilder.create().segment("datashield", "env", "{env}", "methods")
        .query("profile", profile.getName());
    for (DataShieldMethodDto dto : dtos)
      builder.query("name", dto.getName());
    ResourceRequestBuilderFactory.newBuilder().forResource(builder.build(env)).delete() //
        .withCallback(Response.SC_OK, callbackHandler) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
        .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  public void setProfile(DataShieldProfileDto profile) {
    this.profile = profile;
  }

  @Override
  public void onAddMethod() {
    DataShieldMethodModalPresenter presenter = methodModalProvider.get();
    presenter.initialize(profile, env);
    presenter.createNewMethod();
  }

  @Override
  public void onRemoveMethods(final List<DataShieldMethodDto> selectedItems) {
    removeMethodsConfirmation = new Runnable() {
      @Override
      public void run() {
        deleteDataShieldMethods(selectedItems);
      }
    };
    if (DataShieldProfilePresenter.DataShieldEnvironment.ASSIGN.equals(env)) {
      getEventBus().fireEvent(ConfirmationRequiredEvent
          .createWithMessages(removeMethodsConfirmation, translationMessages.removeDataShieldAssignMethods(),
              translationMessages.confirmDeleteDataShieldAssignMethods()));
    } else {
      getEventBus().fireEvent(ConfirmationRequiredEvent
          .createWithMessages(removeMethodsConfirmation, translationMessages.removeDataShieldAggregateMethods(),
              translationMessages.confirmDeleteDataShieldAggregateMethods()));
    }
  }

  //
  // Inner Classes / Interfaces
  //

  /**
   *
   */
  private final class MethodsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      updateDataShieldMethods();
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if (event.getSource().equals(removeMethodConfirmation) && event.isConfirmed()) {
        removeMethodConfirmation.run();
        removeMethodConfirmation = null;
      } else if (event.getSource().equals(removeMethodsConfirmation) && event.isConfirmed()) {
        removeMethodsConfirmation.run();
        removeMethodsConfirmation = null;
      }
    }

  }

  public interface Display extends View, HasUiHandlers<DataShieldMethodsUiHandlers> {

    void showDataShieldMethods(List<DataShieldMethodDto> rows);

    void setMethodActionHandler(ActionHandler<DataShieldMethodDto> handler);

    HasAuthorization getAddMethodAuthorizer();

    HasAuthorization getMethodsAuthorizer();

    void setEnvironment(String env);
  }

}
