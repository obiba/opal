/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.ArrayList;

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceDataProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ServiceDto;
import org.obiba.opal.web.model.client.opal.ServiceStatus;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class IndexAdministrationPresenter extends
    ItemAdministrationPresenter<IndexAdministrationPresenter.Display, IndexAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken("!admin.search")
  @TabInfo(container = AdministrationPresenter.class, label = "Search", priority = 4)
  public interface Proxy extends TabContentProxyPlace<IndexAdministrationPresenter> {
  }

  public interface Display extends View {

//    String TEST_ACTION = "Test";

    String INDEX_ACTION = "Index now";

    String CLEAR_ACTION = "Clear";

//    String CANCEL_ACTION = "Cancel";

    String SCHEDULE = "Schedule indexing";

    enum Slots {
      Drivers, Permissions
    }

    Button getStartButton();

    Button getStopButton();

    HasClickHandlers getRefreshButton();

    MultiSelectionModel<TableIndexStatusDto> getSelectedIndices();

    DropdownButton getActionsDropdown();

    HasActionHandler<TableIndexStatusDto> getActions();

    HasData<TableIndexStatusDto> getIndexTable();
  }

  private final Provider<IndexPresenter> indexPresenter;

  private final AuthorizationPresenter authorizationPresenter;

  private final ResourceDataProvider<TableIndexStatusDto> resourceDataProvider = new ResourceDataProvider<TableIndexStatusDto>(
      Resources.indices());

  private Command confirmedCommand;

  @Inject
  public IndexAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      Provider<AuthorizationPresenter> authorizationPresenter, Provider<IndexPresenter> indexPresenter) {
    super(eventBus, display, proxy);
    this.indexPresenter = indexPresenter;
    this.authorizationPresenter = authorizationPresenter.get();
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.indices()).get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListIndicesAuthorization())).send();
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, AdministrationPresenter.TabSlot, this);
  }

  @Override
  public String getName() {
    return "Indices";
  }

  @Override
  protected void onReveal() {
    getView().getIndexTable().setVisibleRange(0, 10);
    refresh();
    // set permissions
    // AclRequest.newResourceAuthorizationRequestBuilder().authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.indices()).get().authorize(authorizer)
        .send();
  }

  @Override
  protected void onBind() {
    /* stop start search service */
    ResourceRequestBuilderFactory.<ServiceDto>newBuilder().forResource(Resources.searchService()).get()
        .withCallback(new ResourceCallback<ServiceDto>() {
          @Override
          public void onResource(Response response, ServiceDto resource) {
            //GWT.log("RES: "+resource.getStatus());
            if(response.getStatusCode() == 200) {
              if(resource.getStatus().isServiceStatus(ServiceStatus.RUNNING)) {
                getView().getStartButton().setVisible(false);
                getView().getStopButton().setVisible(true);
              } else {
                getView().getStartButton().setVisible(true);
                getView().getStopButton().setVisible(false);
              }
            }
          }
        }).send();
    ;
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {

      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if(event.getSource() == confirmedCommand && event.isConfirmed()) {
          confirmedCommand.execute();
        }
      }
    }));

    // Dropdown Actions
    getView().getActionsDropdown().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        GWT.log(getView().getActionsDropdown().getLastSelectedNavLink().getText());
        if(getView().getActionsDropdown().getLastSelectedNavLink().getText().equals(getView().CLEAR_ACTION)) {
          if(!getView().getSelectedIndices().getSelectedSet().isEmpty()) {

            for(TableIndexStatusDto object : getView().getSelectedIndices().getSelectedSet()) {
              ResponseCodeCallback callback = new ResponseCodeCallback() {

                @Override
                public void onResponseCode(Request request, Response response) {
                  refresh();
                }

              };
              ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
                  .forResource(Resources.index(object.getDatasource(), object.getTable())).accept("application/json")//
                  .withCallback(200, callback).withCallback(503, callback).delete().send();

              getView().getSelectedIndices().setSelected(object, false);
            }
          } else {
            getEventBus()
                .fireEvent(NotificationEvent.Builder.newNotification().error("IndexClearSelectAtLeastOne").build());
          }
        } else if(getView().getActionsDropdown().getLastSelectedNavLink().getText().equals(getView().SCHEDULE)) {
          if(!getView().getSelectedIndices().getSelectedSet().isEmpty()) {

            ArrayList<TableIndexStatusDto> objects = new ArrayList<TableIndexStatusDto>();
            for(TableIndexStatusDto object : getView().getSelectedIndices().getSelectedSet()) {
              objects.add(object);
            }

            IndexPresenter dialog = indexPresenter.get();
            dialog.updateSchedules(objects);
            addToPopupSlot(dialog);

          } else {
            getEventBus()
                .fireEvent(NotificationEvent.Builder.newNotification().error("IndexScheduleSelectAtLeastOne").build());
          }
        }
      }
    });

    getView().getActions().setActionHandler(new ActionHandler<TableIndexStatusDto>() {

      @Override
      public void doAction(final TableIndexStatusDto object, String actionName) {
        if(actionName.equalsIgnoreCase(Display.CLEAR_ACTION)) {
          ResponseCodeCallback callback = new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == 200) {
                refresh();
                getEventBus()
                    .fireEvent(NotificationEvent.Builder.newNotification().info("IndexClearCompleted").build());
              } else {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getEventBus().fireEvent(
                    NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                        .build());
              }
            }

          };
          ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
              .forResource(Resources.index(object.getDatasource(), object.getTable())).accept("application/json")//
              .withCallback(200, callback).withCallback(503, callback).delete().send();
        }
//        else if(actionName.equalsIgnoreCase(Display.INDEX_ACTION)) {
//          ResponseCodeCallback callback = new ResponseCodeCallback() {
//
//            @Override
//            public void onResponseCode(Request request, Response response) {
//              if(response.getStatusCode() == 200) {
//                refresh();
//                getEventBus()
//                    .fireEvent(NotificationEvent.Builder.newNotification().info("IndexNowCompleted").build());
//              } else {
//                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
//                getEventBus().fireEvent(
//                    NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
//                        .build());
//              }
//            }
//
//          };
//          ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
//              .forResource(Resources.index(object.getDatasource(), object.getTable())).accept("application/json")//
//              .withCallback(200, callback).withCallback(503, callback).put().send();
//        }
      }

    });

    // REFRESH
    registerHandler(getView().getRefreshButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        refresh();
      }
    }));

    // STOP
    registerHandler(getView().getStopButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResponseCodeCallback callback = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == 200) {
              getView().getStopButton().setVisible(false);
              getView().getStartButton().setVisible(true);
              refresh();
              getEventBus()
                  .fireEvent(NotificationEvent.Builder.newNotification().info("ServiceSearchStopCompleted").build());
            } else {
              ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
              getEventBus().fireEvent(
                  NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                      .build());
            }
          }

        };
        ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
            .forResource(Resources.searchService()).accept("application/json")//
            .withCallback(200, callback).withCallback(500, callback).delete().send();
      }
    }));
    // START
    registerHandler(getView().getStartButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResponseCodeCallback callback = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == 200) {
              getView().getStopButton().setVisible(true);
              getView().getStartButton().setVisible(false);
              refresh();
              getEventBus()
                  .fireEvent(NotificationEvent.Builder.newNotification().info("ServiceSearchStartCompleted").build());
            } else {
              ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
              getEventBus().fireEvent(
                  NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                      .build());
            }
          }

        };
        ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
            .forResource(Resources.searchService()).accept("application/json")//
            .withCallback(200, callback).withCallback(500, callback).put().send();
      }
    }));
  }

  private void refresh() {
    Range r = getView().getIndexTable().getVisibleRange();
    getView().getIndexTable().setVisibleRangeAndClearData(r, true);
    getView().getSelectedIndices().clear();
  }

  private final class ListIndicesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      // Only bind the table to its data provider if we're authorized
      if(resourceDataProvider.getDataDisplays().size() == 0) {
        resourceDataProvider.addDataDisplay(getView().getIndexTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }

  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(Display.Slots.Permissions);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      setInSlot(Display.Slots.Permissions, authorizationPresenter);
    }
  }

}
