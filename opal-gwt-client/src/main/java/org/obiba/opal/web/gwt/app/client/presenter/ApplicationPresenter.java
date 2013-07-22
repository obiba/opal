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

import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.ui.HasUrl;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueMapPopupPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.VariableSearchListItem;
import org.obiba.opal.web.gwt.app.client.workbench.view.VariableSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

/**
 *
 */
public class ApplicationPresenter extends Presenter<ApplicationPresenter.Display, ApplicationPresenter.Proxy>
    implements ApplicationUiHandlers {

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> WORKBENCH = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<ApplicationPresenter> {}

  private final RequestCredentials credentials;

  private final NotificationPresenter messageDialog;

  private final Provider<FileSelectorPresenter> fileSelectorPresenter;

  private final Provider<ValueMapPopupPresenter> valueMapPopupPresenter;

  private final RequestUrlBuilder urlBuilder;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public ApplicationPresenter(Display display, Proxy proxy, EventBus eventBus, RequestCredentials credentials,
      NotificationPresenter messageDialog, Provider<FileSelectorPresenter> fileSelectorPresenter,
      Provider<ValueMapPopupPresenter> valueMapPopupPresenter, RequestUrlBuilder urlBuilder) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.fileSelectorPresenter = fileSelectorPresenter;
    this.valueMapPopupPresenter = valueMapPopupPresenter;
    this.urlBuilder = urlBuilder;
    getView().setUiHandlers(this);
  }

  @Override
  protected void revealInParent() {
    RevealRootContentEvent.fire(this, this);
  }

  @Override
  protected void onBind() {

    registerHandler(
        getEventBus().addHandler(FileSelectionRequiredEvent.getType(), new FileSelectionRequiredEvent.Handler() {

          @Override
          public void onFileSelectionRequired(FileSelectionRequiredEvent event) {
            FileSelectorPresenter fsp = fileSelectorPresenter.get();
            fsp.handle(event);
            addToPopupSlot(fsp);
          }
        }));
    registerHandler(getEventBus().addHandler(GeoValueDisplayEvent.getType(), new GeoValueDisplayEvent.Handler() {

      @Override
      public void onGeoValueDisplay(GeoValueDisplayEvent event) {
        ValueMapPopupPresenter vmp = valueMapPopupPresenter.get();
        vmp.handle(event);
        addToPopupSlot(vmp);
      }
    }));
    registerHandler(getEventBus().addHandler(FileDownloadEvent.getType(), new FileDownloadEvent.Handler() {

      @Override
      public void onFileDownload(FileDownloadEvent event) {
        getView().getDownloader().setUrl(urlBuilder.buildAbsoluteUrl(event.getUrl()));
      }
    }));

    // Update search box on event
    registerHandler(getEventBus()
        .addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {
          @Override
          public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
            getView().clearSearch();
            getView().addSearchItem(event.getSelection(), VariableSearchListItem.ItemType.DATASOURCE);
          }
        }));

    registerHandler(
        getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {
          @Override
          public void onTableSelectionChanged(TableSelectionChangeEvent event) {
            getView().clearSearch();
            getView().addSearchItem(event.getDatasourceName(), VariableSearchListItem.ItemType.DATASOURCE);
            getView().addSearchItem(event.getTableName(), VariableSearchListItem.ItemType.TABLE);
          }
        }));

    registerHandler(
        getEventBus().addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {
          @Override
          public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
            getView().clearSearch();
            getView().addSearchItem(event.getTable().getDatasourceName(), VariableSearchListItem.ItemType.DATASOURCE);
            getView().addSearchItem(event.getTable().getName(), VariableSearchListItem.ItemType.TABLE);
          }
        }));

    registerUserMessageEventHandler();
  }

  @Override
  protected void onReveal() {
    getView().setUsername(credentials.getUsername());
    getView().setVersion(ResourceRequestBuilderFactory.newBuilder().getVersion());
    authorize();
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").get()
        .authorize(getView().getProjectsAutorizer()).send();

    getEventBus().fireEvent(new RequestAdministrationPermissionEvent(new HasAuthorization() {

      @Override
      public void unauthorized() {
      }

      @Override
      public void beforeAuthorization() {
        getView().getAdministrationAuthorizer().beforeAuthorization();
      }

      @Override
      public void authorized() {
        getView().getAdministrationAuthorizer().authorized();
      }
    }));
  }

  private void registerUserMessageEventHandler() {
    registerHandler(getEventBus().addHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        messageDialog.setNotification(event);
        // false : don't center the dialog
        addToPopupSlot(messageDialog, false);
      }
    }));
  }

  @Override
  public void onDashboard() {
    getEventBus().fireEvent(new PlaceChangeEvent(Places.dashboardPlace));
  }

  @Override
  public void onProjects() {
    getEventBus().fireEvent(new PlaceChangeEvent(Places.projectsPlace));
  }

  @Override
  public void onAdministration() {
    getEventBus().fireEvent(new PlaceChangeEvent(Places.administrationPlace));
  }

  @Override
  public void onHelp() {
    HelpUtil.openPage();
  }

  @Override
  public void onQuit() {
    getEventBus().fireEvent(new SessionEndedEvent());
  }

  @Override
  public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
    // Get the table dto to fire the event to select the variable
    final String datasourceName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getDatasource();
    final String tableName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getTable();
    final String variableName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getVariable();

    UriBuilder ub = UriBuilder.URI_DATASOURCE_TABLE;
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build(datasourceName, tableName)).get()
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, final TableDto tableDto) {

            UriBuilder ub = UriBuilder.URI_DATASOURCE_TABLE_VARIABLES;
            ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(ub.build(datasourceName, tableName)).get()
                .withCallback(new ResourceCallback<JsArray<VariableDto>>() {

                  @Override
                  public void onResource(Response response, JsArray<VariableDto> resource) {
                    JsArray<VariableDto> variables = JsArrays.toSafeArray(resource);

                    VariableDto previous = null;
                    VariableDto selection = null;
                    VariableDto next = null;
                    for(int i = 0; i < variables.length(); i++) {
                      if(variables.get(i).getName().equals(variableName)) {
                        selection = variables.get(i);

                        if(i >= 0) {
                          previous = variables.get(i - 1);
                        }

                        if(i < variables.length() - 1) {
                          next = variables.get(i + 1);
                        }
                      }
                    }
                    getEventBus().fireEvent(new VariableSelectionChangeEvent(tableDto, selection, previous, next));
                  }
                })//
                .withCallback(Response.SC_SERVICE_UNAVAILABLE, new ResponseCodeCallback() {
                  @Override
                  public void onResponseCode(Request request, Response response) {
                    getEventBus().fireEvent(NotificationEvent.newBuilder().error("SearchServiceUnavailable").build());
                  }
                }).send();
          }
        }).send();
  }

  public interface Display extends View, HasUiHandlers<ApplicationUiHandlers> {

    HasUrl getDownloader();

    HasAuthorization getAdministrationAuthorizer();

    void setCurrentSelection(MenuItem selection);

    void clearSelection();

    void setUsername(String username);

    void setVersion(String version);

    HasAuthorization getProjectsAutorizer();

    void addSearchItem(String text, VariableSearchListItem.ItemType type);

    void clearSearch();
  }
}
