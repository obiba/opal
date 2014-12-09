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

import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.ModalClosedEvent;
import org.obiba.opal.web.gwt.app.client.event.ModalShownEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueMapPopupPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectHiddenEvent;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.ui.HasUrl;
import org.obiba.opal.web.gwt.app.client.ui.VariableSearchListItem;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class ApplicationPresenter extends Presenter<ApplicationPresenter.Display, ApplicationPresenter.Proxy>
    implements ApplicationUiHandlers {

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> WORKBENCH = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> NOTIFICATION
      = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<ApplicationPresenter> {}

  private final RequestCredentials credentials;

  private final NotificationPresenter messageDialog;

  private final UnhandledResponseNotificationPresenter unhandledResponseNotificationPresenter;

  private final ModalProvider<FileSelectorPresenter> fileSelectorProvider;

  private final ModalProvider<ValueMapPopupPresenter> valueMapPopupProvider;

  private final RequestUrlBuilder urlBuilder;

  private final PlaceManager placeManager;

  private int activeModals = 0;

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public ApplicationPresenter(Display display, Proxy proxy, EventBus eventBus, RequestCredentials credentials,
      NotificationPresenter messageDialog,
      UnhandledResponseNotificationPresenter unhandledResponseNotificationPresenter,
      ModalProvider<FileSelectorPresenter> fileSelectorProvider,
      ModalProvider<ValueMapPopupPresenter> valueMapPopupProvider, RequestUrlBuilder urlBuilder,
      PlaceManager placeManager) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.unhandledResponseNotificationPresenter = unhandledResponseNotificationPresenter;
    this.fileSelectorProvider = fileSelectorProvider.setContainer(this);
    this.valueMapPopupProvider = valueMapPopupProvider.setContainer(this);
    this.urlBuilder = urlBuilder;
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  protected void revealInParent() {
    RevealRootContentEvent.fire(this, this);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FileSelectionRequestEvent.getType(), new FileSelectionRequestEvent.Handler() {
      @Override
      public void onFileSelectionRequired(FileSelectionRequestEvent event) {
        FileSelectorPresenter fsp = fileSelectorProvider.get();
        fsp.handle(event);
      }
    });
    addRegisteredHandler(GeoValueDisplayEvent.getType(), new GeoValueDisplayEvent.Handler() {

      @Override
      public void onGeoValueDisplay(GeoValueDisplayEvent event) {
        ValueMapPopupPresenter vmp = valueMapPopupProvider.get();
        vmp.handle(event);
      }
    });
    addRegisteredHandler(FileDownloadRequestEvent.getType(), new FileDownloadRequestEvent.FileDownloadRequestHandler() {

      @Override
      public void onFileDownloadRequest(FileDownloadRequestEvent event) {
        getView().getDownloader().setUrl(urlBuilder.buildAbsoluteUrl(event.getUrl()));
      }
    });
    addRegisteredHandler(FilesDownloadRequestEvent.getType(),
        new FilesDownloadRequestEvent.FilesDownloadRequestHandler() {
          @Override
          public void onFilesDownloadRequest(FilesDownloadRequestEvent event) {
            UriBuilder uriBuilder = UriBuilder.create().fromPath(FileDtos.getLink(event.getParent()));
            for(FileDto child : event.getChildren()) {
              uriBuilder.query("file", child.getName());
            }
            getView().getDownloader().setUrl(urlBuilder.buildAbsoluteUrl(uriBuilder.build()));
          }
        });

    // Update search box on event
    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(),
        new DatasourceSelectionChangeEvent.DatasourceSelectionChangeHandler() {
          @Override
          public void onDatasourceSelectionChange(DatasourceSelectionChangeEvent event) {
            getView().clearSearch();
            getView().addSearchItem(event.getDatasource(), VariableSearchListItem.ItemType.DATASOURCE);
          }
        });

    addRegisteredHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {
      @Override
      public void onTableSelectionChanged(TableSelectionChangeEvent event) {
        getView().clearSearch();
        getView().addSearchItem(event.getDatasourceName(), VariableSearchListItem.ItemType.DATASOURCE);
        getView().addSearchItem(event.getTableName(), VariableSearchListItem.ItemType.TABLE);
      }
    });

    addRegisteredHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {
      @Override
      public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
        getView().clearSearch();
        getView().addSearchItem(event.getTable().getDatasourceName(), VariableSearchListItem.ItemType.DATASOURCE);
        getView().addSearchItem(event.getTable().getName(), VariableSearchListItem.ItemType.TABLE);
      }
    });

    addRegisteredHandler(ProjectHiddenEvent.getType(), new ProjectHiddenEvent.ProjectHiddenHandler() {
      @Override
      public void onProjectHidden(ProjectHiddenEvent event) {
        getView().clearSearch();
      }
    });

    addRegisteredHandler(GeneralConfigSavedEvent.getType(), new GeneralConfigSavedEvent.GeneralConfigSavedHandler() {
      @Override
      public void onGeneralConfigSaved(GeneralConfigSavedEvent event) {
        refreshApplicationName();
      }
    });

    registerUserMessageEventHandler();
    registerModalEvents();
  }

  @Override
  protected void onReveal() {
    getView().setUsername(credentials.getUsername());
    getView().setVersion(ResourceRequestBuilderFactory.newBuilder().getVersion());

    refreshApplicationName();

    authorize();
  }

  private void refreshApplicationName() {
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(UriBuilders.SYSTEM_NAME.create().build()) //
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setApplicationName(response.getText());
          }
        }, Response.SC_OK).send();
  }

  private void authorize() {
    // Edit system config
//    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/system/conf/general").put()
//        .authorize(getView().getAdministrationAuthorizer()).send();
    fireEvent(new RequestAdministrationPermissionEvent(new HasAuthorization() {

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

  private void registerModalEvents() {
    getEventBus().addHandler(ModalShownEvent.getType(), new ModalShownEvent.ModalShownHandler() {
      @Override
      public void onModalShown(ModalShownEvent event) {
        activeModals++;
      }
    });

    getEventBus().addHandler(ModalClosedEvent.getType(), new ModalClosedEvent.Handler() {
      @Override
      public void onModalClosed(ModalClosedEvent event) {
        activeModals--;
        if (activeModals < 0) {
          GWT.log("WARN: Active modal count is invalid: " + activeModals);
          activeModals = 0;
        }
      }
    });
  }

  private void registerUserMessageEventHandler() {
    addRegisteredHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        if(isVisible() && !event.isConsumed() && activeModals == 0) {
          messageDialog.setNotification(event);
          setInSlot(NOTIFICATION, messageDialog);
        }
      }
    });

    addRegisteredHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {
      @Override
      public void onUnhandledResponse(UnhandledResponseEvent e) {
        if(e.isConsumed() || activeModals > 0) return;
        unhandledResponseNotificationPresenter.withResponseEvent(e);
        setInSlot(NOTIFICATION, unhandledResponseNotificationPresenter);
      }
    });
  }

  @Override
  public void onQuit() {
    fireEvent(new SessionEndedEvent());
  }

  @Override
  public void onSelection(VariableSuggestOracle.VariableSuggestion suggestion) {
    // Get the table dto to fire the event to select the variable
    String datasourceName = suggestion.getDatasource();
    String tableName = suggestion.getTable();
    String variableName = suggestion.getVariable();

    getView().clearSearch();

    String path = MagmaPath.Builder.datasource(datasourceName).table(tableName).variable(variableName).build();

    PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.PROJECT)
        .with(ParameterTokens.TOKEN_NAME, datasourceName) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()) //
        .with(ParameterTokens.TOKEN_PATH, path);
    placeManager.revealPlace(builder.build());
  }

  public interface Display extends View, HasUiHandlers<ApplicationUiHandlers> {

    HasUrl getDownloader();

    HasAuthorization getAdministrationAuthorizer();

    void setUsername(String username);

    void setVersion(String version);

    void addSearchItem(String text, VariableSearchListItem.ItemType type);

    void clearSearch();

    void setApplicationName(String text);
  }
}
