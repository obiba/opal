/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import com.google.common.base.Strings;
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
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageRemovedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class DataShieldAdministrationPresenter extends PresenterWidget<DataShieldAdministrationPresenter.Display> {

  private String env;

  private Runnable removeMethodConfirmation;

  private final ModalProvider<DataShieldMethodPresenter> datashieldModalProvider;

  private TranslationMessages translationMessages;

  @Inject
  public DataShieldAdministrationPresenter(Display display, EventBus eventBus,
                                           ModalProvider<DataShieldMethodPresenter> datashieldModalProvider, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.datashieldModalProvider = datashieldModalProvider.setContainer(this);
  }

  void setEnvironment(String env) {
    this.env = env;
    getView().setEnvironment(env);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
  }

  private void addEventHandlers() {
    getView().getDataShieldMethodActionsColumn().setActionHandler(new ActionHandler<DataShieldMethodDto>() {
      @Override
      public void doAction(DataShieldMethodDto dto, String actionName) {
        if (actionName != null) {
          doDataShieldMethodActionImpl(dto, actionName);
        }
      }
    });
    addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
    registerHandler(getView().addMethodHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        DataShieldMethodPresenter presenter = datashieldModalProvider.get();
        if (!Strings.isNullOrEmpty(env)) presenter.setEnvironement(env);
        presenter.createNewMethod();
      }
    }));
    addHandler(DataShieldMethodCreatedEvent.getType(),
        new DataShieldMethodCreatedEvent.DataShieldMethodCreatedHandler() {

          @Override
          public void onDataShieldMethodCreated(DataShieldMethodCreatedEvent event) {
            updateDataShieldMethods();
          }
        });
    addHandler(DataShieldMethodUpdatedEvent.getType(),
        new DataShieldMethodUpdatedEvent.DataShieldMethodUpdatedHandler() {

          @Override
          public void onDataShieldMethodUpdated(DataShieldMethodUpdatedEvent event) {
            updateDataShieldMethods();
          }

        });
    addHandler(DataShieldPackageRemovedEvent.getType(),
        new DataShieldPackageRemovedEvent.DataShieldPackageRemovedHandler() {
          @Override
          public void onDataShieldPackageRemoved(DataShieldPackageRemovedEvent event) {
            updateDataShieldMethods();
          }
        });
    addHandler(DataShieldPackageUpdatedEvent.getType(),
        new DataShieldPackageUpdatedEvent.DataShieldPackageUpdatedHandler() {
          @Override
          public void onDataShieldPackageUpdated(DataShieldPackageUpdatedEvent event) {
            updateDataShieldMethods();
          }
        });
  }

  @Override
  protected void onReveal() {
    authorize();
  }

  // @Override
  public void authorize(HasAuthorization authorizer) {
    authorizeMethods(CascadingAuthorizer.newBuilder()//
        .or(AclRequest.newResourceAuthorizationRequestBuilder())//
        .authorize(authorizer).build());
  }

  private void authorize() {
    // view methods
    authorizeMethods(new CompositeAuthorizer(getView().getMethodsAuthorizer(), new MethodsUpdate()));
    // create method
    authorizeAddMethod(getView().getAddMethodAuthorizer());
  }

  private String environment() {
    return UriBuilder.create().segment("datashield", "env", "{env}").build(env);
  }

  private String methods() {
    return environment() + "/methods";
  }

  private String method(String method) {
    return environment() + UriBuilder.create().segment("method", "{method}").build(method);
  }

  private void authorizeMethods(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(methods()).get().authorize(authorizer).send();
  }

  private void authorizeAddMethod(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(methods()).post().authorize(authorizer).send();
  }

  private void authorizeEditMethod(DataShieldMethodDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(method(dto.getName())).put()
        .authorize(authorizer).send();
  }

  private void authorizeDeleteMethod(DataShieldMethodDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(method(dto.getName())).delete()
        .authorize(authorizer).send();
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
      authorizeEditMethod(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          DataShieldMethodPresenter presenter = datashieldModalProvider.get();
          presenter.setEnvironement(env);
          presenter.updateMethod(dto);
        }
      });

    } else if (actionName.equals(REMOVE_ACTION)) {
      authorizeDeleteMethod(dto, new Authorizer(getEventBus()) {
        @Override
        public void authorized() {
          removeMethodConfirmation = new Runnable() {
            @Override
            public void run() {
              deleteDataShieldMethod(dto);
            }
          };
          if (DataShieldConfigPresenter.DataShieldEnvironment.ASSIGN.equals(env)) {
            getEventBus().fireEvent(ConfirmationRequiredEvent
                .createWithMessages(removeMethodConfirmation, translationMessages.removeDataShieldAssignMethod(),
                    translationMessages.confirmDeleteDataShieldAssignMethod()));
          } else {
            getEventBus().fireEvent(ConfirmationRequiredEvent
                .createWithMessages(removeMethodConfirmation, translationMessages.removeDataShieldAggregateMethod(),
                    translationMessages.confirmDeleteDataShieldAggregateMethod()));
          }
        }
      });

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
      if (removeMethodConfirmation != null && event.getSource().equals(removeMethodConfirmation) &&
          event.isConfirmed()) {
        removeMethodConfirmation.run();
        removeMethodConfirmation = null;
      }
    }

  }

  public interface Display extends View {

    void showDataShieldMethods(List<DataShieldMethodDto> rows);

    HasActionHandler<DataShieldMethodDto> getDataShieldMethodActionsColumn();

    HandlerRegistration addMethodHandler(ClickHandler handler);

    HasAuthorization getAddMethodAuthorizer();

    HasAuthorization getMethodsAuthorizer();

    void setEnvironment(String env);
  }

}
