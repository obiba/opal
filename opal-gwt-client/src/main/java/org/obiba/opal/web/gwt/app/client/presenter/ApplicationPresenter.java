/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.presenter;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.cart.event.CartAddVariableEvent;
import org.obiba.opal.web.gwt.app.client.cart.event.CartAddVariableItemsEvent;
import org.obiba.opal.web.gwt.app.client.cart.event.CartAddVariablesEvent;
import org.obiba.opal.web.gwt.app.client.cart.event.CartCountsUpdateEvent;
import org.obiba.opal.web.gwt.app.client.cart.service.CartService;
import org.obiba.opal.web.gwt.app.client.event.ModalClosedEvent;
import org.obiba.opal.web.gwt.app.client.event.ModalShownEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.fs.service.FileService;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueMapPopupPresenter;
import org.obiba.opal.web.gwt.app.client.magma.sql.event.SQLQueryCreationEvent;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.TaxonomyAttributes;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectHiddenEvent;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.search.event.SearchDatasourceVariablesEvent;
import org.obiba.opal.web.gwt.app.client.search.event.SearchEntityEvent;
import org.obiba.opal.web.gwt.app.client.search.event.SearchTableVariablesEvent;
import org.obiba.opal.web.gwt.app.client.search.event.SearchTaxonomyVariablesEvent;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.ui.VariableSearchListItem;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

  private final CartService cartService;

  private final FileService fileService;

  private int activeModals = 0;

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public ApplicationPresenter(Display display, Proxy proxy, EventBus eventBus, RequestCredentials credentials,
      NotificationPresenter messageDialog,
      UnhandledResponseNotificationPresenter unhandledResponseNotificationPresenter,
      ModalProvider<FileSelectorPresenter> fileSelectorProvider,
      ModalProvider<ValueMapPopupPresenter> valueMapPopupProvider, RequestUrlBuilder urlBuilder,
      PlaceManager placeManager, CartService cartService, FileService fileService) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.unhandledResponseNotificationPresenter = unhandledResponseNotificationPresenter;
    this.fileSelectorProvider = fileSelectorProvider.setContainer(this);
    this.valueMapPopupProvider = valueMapPopupProvider.setContainer(this);
    this.urlBuilder = urlBuilder;
    this.placeManager = placeManager;
    this.cartService = cartService;
    this.fileService = fileService;
    getView().setUiHandlers(this);
    getView().setCartCounts(cartService.getVariablesCount());
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
        getView().setDownloadInfo(urlBuilder.buildAbsoluteUrl(event.getUrl()), event.getPassword());
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
            getView().setDownloadInfo(urlBuilder.buildAbsoluteUrl(uriBuilder.build()), event.getPassword());
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

    addRegisteredHandler(CartAddVariableEvent.getType(), new CartAddVariableEvent.CartAddVariableHandler() {
      @Override
      public void onCartAddVariable(CartAddVariableEvent event) {
        int originalCount = cartService.getVariablesCount();
        try {
          cartService.addVariable(event.getDatasource(), event.getTable(), event.getVariable());
        } catch (Exception e) {
          fireEvent(NotificationEvent.newBuilder().warn("CartLimitExceeded").build());
        }
        updateCartVariablesCount(originalCount, event.getEntityType(), cartService.getVariablesCount());
      }
    });

    addRegisteredHandler(CartAddVariablesEvent.getType(), new CartAddVariablesEvent.CartAddVariablesHandler() {
      @Override
      public void onCartAddVariables(CartAddVariablesEvent event) {
        int originalCount = cartService.getVariablesCount();
        try {
          cartService.addVariables(event.getTableVariables());
        } catch (Exception e) {
          fireEvent(NotificationEvent.newBuilder().warn("CartLimitExceeded").build());
        }
        updateCartVariablesCount(originalCount, event.getEntityType(), cartService.getVariablesCount());
      }
    });

    addRegisteredHandler(CartAddVariableItemsEvent.getType(), new CartAddVariableItemsEvent.CartAddVariableItemsHandler() {
      @Override
      public void onCartAddVariableItems(CartAddVariableItemsEvent event) {
        int originalCount = cartService.getVariablesCount();
        try {
          cartService.addVariableItems(event.getTableVariables());
        } catch (Exception e) {
          fireEvent(NotificationEvent.newBuilder().warn("CartLimitExceeded").build());
        }
        updateCartVariablesCount(originalCount, event.getEntityType(), cartService.getVariablesCount());
      }
    });

    addRegisteredHandler(CartCountsUpdateEvent.getType(), new CartCountsUpdateEvent.CartCountsUpdateHandler() {
      @Override
      public void onCartCountsUpdate(CartCountsUpdateEvent event) {
        getView().setCartCounts(event.getVariablesCount());
      }
    });

    addRegisteredHandler(SearchEntityEvent.getType(), new SearchEntityEvent.SearchEntityHandler() {
      @Override
      public void onSearchEntity(SearchEntityEvent event) {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.SEARCH_ENTITY)
            .with(ParameterTokens.TOKEN_TYPE, event.getEntityType()) //
            .with(ParameterTokens.TOKEN_ID, event.getEntityId());
        if (!Strings.isNullOrEmpty(event.getTableReference())) builder.with(ParameterTokens.TOKEN_TABLE, event.getTableReference());
        placeManager.revealPlaceHierarchy(Lists.newArrayList(PlaceRequestHelper.createRequestBuilder(Places.SEARCH).build(), builder.build()));
      }
    });

    addRegisteredHandler(SearchDatasourceVariablesEvent.getType(), new SearchDatasourceVariablesEvent.SearchDatasourceVariablesHandler() {
      @Override
      public void onSearchDatasourceVariables(SearchDatasourceVariablesEvent event) {
        revealSearchVariables("in(project,(" + event.getDatasource().replaceAll(" ", "+") + ")),exists(table),exists(name.analyzed)");
      }
    });

    addRegisteredHandler(SearchTableVariablesEvent.getType(), new SearchTableVariablesEvent.SearchTableVariablesHandler() {
      @Override
      public void onSearchTableVariables(SearchTableVariablesEvent event) {
        StringBuilder rqlQuery = new StringBuilder()
            .append("in(project,(").append(event.getDatasource().replaceAll(" ", "+")).append("))")
            .append(",in(table,(").append(event.getTable().replaceAll(" ", "+")).append("))")
            .append(",exists(name.analyzed)");
        if (event.getTaxonomyAttributes() != null) {
          TaxonomyAttributes taxonomyAttributes = event.getTaxonomyAttributes();
          for (String taxonomyName : event.getTaxonomyAttributes().keySet()) {
            Map<String, List<String>> vocabularyMap = taxonomyAttributes.get(taxonomyName);
            for (String vocabularyName : vocabularyMap.keySet()) {
              rqlQuery.append(",in(").append(taxonomyName).append("-").append(vocabularyName).append(",(")
                  .append(Joiner.on(",").join(vocabularyMap.get(vocabularyName))).append("))");
            }
          }
        }
        revealSearchVariables(rqlQuery.toString());
      }
    });

    addRegisteredHandler(SearchTaxonomyVariablesEvent.getType(), new SearchTaxonomyVariablesEvent.SearchTaxonomyVariablesHandler() {
      @Override
      public void onSearchTaxonomyVariables(SearchTaxonomyVariablesEvent event) {
        String field = event.getTaxonomy() + "-" + event.getVocabulary();
        if (Strings.isNullOrEmpty(event.getTerm()))
          revealSearchVariables("exists(" + field + ")");
        else
          revealSearchVariables("in(" + field + ",(" + event.getTerm() + "))");
      }
    });

    addRegisteredHandler(GeneralConfigSavedEvent.getType(), new GeneralConfigSavedEvent.GeneralConfigSavedHandler() {
      @Override
      public void onGeneralConfigSaved(GeneralConfigSavedEvent event) {
        refreshApplicationName();
      }
    });

    addRegisteredHandler(SQLQueryCreationEvent.getType(), new SQLQueryCreationEvent.SQLQueryCreationHandler() {
      @Override
      public void onSQLQueryCreation(SQLQueryCreationEvent event) {
        placeManager.revealPlace(ProjectPlacesHelper.getDatasourcePlace(event.getProject()));
      }
    });

    registerUserMessageEventHandler();
    registerModalEvents();
  }

  private void updateCartVariablesCount(int originalCount, String entityType, int newCount) {
    getView().setCartCounts(newCount);
    int diffCount = newCount - originalCount;
    String msg = diffCount == 0 ? "NoVariableAddedToCart" : (diffCount == 1 ? "VariableAddedToCart" : "VariablesAddedToCart");
    fireEvent(NotificationEvent.newBuilder().info(msg).args("" + diffCount, entityType).build());
  }

  private void revealSearchVariables(String rqlQuery) {
    PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.SEARCH_VARIABLES)
        .with(ParameterTokens.TOKEN_RQL_QUERY, rqlQuery)
        .with(ParameterTokens.TOKEN_OFFSET, "0");
    placeManager.revealPlaceHierarchy(Lists.newArrayList(PlaceRequestHelper.createRequestBuilder(Places.SEARCH).build(), builder.build()));
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
        .accept("text/plain")
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
    cartService.clear();
    fileService.clear();
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

  @Override
  public void onSearch(VariableSuggestOracle.AdvancedSearchSuggestion suggestion) {
    String rql = "";
    if (!Strings.isNullOrEmpty(suggestion.getDatasource()))
      rql = "in(project," + suggestion.getDatasource() + ")";
    if (!Strings.isNullOrEmpty(suggestion.getTable()))
      rql = rql + ",in(table," + suggestion.getTable() + ")";
    rql = (Strings.isNullOrEmpty(rql) ? "" : rql + ",") + "contains(" + suggestion.getReplacementString() + ")";
    
    PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.SEARCH_VARIABLES)
        .with(ParameterTokens.TOKEN_RQL_QUERY, rql)
        .with(ParameterTokens.TOKEN_OFFSET, "0");
    placeManager.revealPlaceHierarchy(Lists.newArrayList(PlaceRequestHelper.createRequestBuilder(Places.SEARCH).build(), builder.build()));
  }

  public interface Display extends View, HasUiHandlers<ApplicationUiHandlers> {

    void setDownloadInfo(String Url, String password);

    HasAuthorization getAdministrationAuthorizer();

    void setUsername(String username);

    void setVersion(String version);

    void setCartCounts(int count);

    void addSearchItem(String text, VariableSearchListItem.ItemType type);

    void clearSearch();

    void setApplicationName(String text);
  }
}
