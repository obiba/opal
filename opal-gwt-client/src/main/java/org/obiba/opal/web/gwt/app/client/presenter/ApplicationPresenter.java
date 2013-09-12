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
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueMapPopupPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.ui.HasUrl;
import org.obiba.opal.web.gwt.app.client.ui.VariableSearchListItem;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

/**
 *
 */
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

  private final ModalProvider<FileSelectorPresenter> fileSelectorProvider;

  private final Provider<ValueMapPopupPresenter> valueMapPopupPresenter;

  private final RequestUrlBuilder urlBuilder;

  private final PlaceManager placeManager;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public ApplicationPresenter(Display display, Proxy proxy, EventBus eventBus, RequestCredentials credentials,
      NotificationPresenter messageDialog, ModalProvider<FileSelectorPresenter> fileSelectorProvider,
      Provider<ValueMapPopupPresenter> valueMapPopupPresenter, RequestUrlBuilder urlBuilder,
      PlaceManager placeManager) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.fileSelectorProvider = fileSelectorProvider.setContainer(this);
    this.valueMapPopupPresenter = valueMapPopupPresenter;
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
    registerHandler(
        getEventBus().addHandler(FileSelectionRequestEvent.getType(), new FileSelectionRequestEvent.Handler() {

          @Override
          public void onFileSelectionRequired(FileSelectionRequestEvent event) {
            FileSelectorPresenter fsp = fileSelectorProvider.get();
            fsp.handle(event);
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
    registerHandler(getEventBus().addHandler(FileDownloadRequestEvent.getType(), new FileDownloadRequestEvent.Handler() {

      @Override
      public void onFileDownloadRequest(FileDownloadRequestEvent event) {
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
        setInSlot(NOTIFICATION, messageDialog);
      }
    }));
  }

  @Override
  public void onDashboard() {
    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(Places.DASHBOARD).build());
  }

  @Override
  public void onProjects() {
    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(Places.PROJECTS).build());
  }

  @Override
  public void onAdministration() {
    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(Places.ADMINISTRATION).build());
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
    String datasourceName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getDatasource();
    String tableName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getTable();
    String variableName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getVariable();

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

    HasAuthorization getProjectsAutorizer();

    void addSearchItem(String text, VariableSearchListItem.ItemType type);

    void clearSearch();
  }
}
