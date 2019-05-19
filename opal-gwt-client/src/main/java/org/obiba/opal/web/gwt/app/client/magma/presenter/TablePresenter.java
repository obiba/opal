/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.analysis.AnalysesPresenter;
import org.obiba.opal.web.gwt.app.client.analysis.event.AnalyseVariablesRequestEvent;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginsResource;
import org.obiba.opal.web.gwt.app.client.cart.event.CartAddVariablesEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.copy.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.copy.ViewCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.*;
import org.obiba.opal.web.gwt.app.client.magma.copy.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TablePresenter.Display.Slots;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.ViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.ViewWhereModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.*;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.search.event.SearchTableVariablesEvent;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.OpalSystemCache;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.support.VariablesFilter;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import java.util.*;

import static com.google.gwt.http.client.Response.*;

public class TablePresenter extends PresenterWidget<TablePresenter.Display>
    implements TableUiHandlers, TableSelectionChangeEvent.Handler {

  private static final int DELAY_MILLIS = 2000;

  private JsArray<VariableDto> variables;

  private TableDto table;

  private TableIndexStatusDto statusDto;

  private boolean cancelIndexation = false;

  private final PlaceManager placeManager;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final ModalProvider<VariablesToViewPresenter> variablesToViewProvider;

  private final ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider;

  private final ModalProvider<ViewModalPresenter> viewPropertiesModalProvider;

  private final ModalProvider<ViewWhereModalPresenter> viewWhereModalProvider;

  private final ModalProvider<VariablePropertiesModalPresenter> variablePropertiesModalProvider;

  private final ModalProvider<AddVariablesModalPresenter> addVariablesModalProvider;

  private final ModalProvider<DataExportPresenter> dataExportModalProvider;

  private final ModalProvider<DataCopyPresenter> dataCopyModalProvider;

  private final ModalProvider<ViewCopyPresenter> viewCopyModalProvider;

  private final ValuesTablePresenter valuesTablePresenter;

  private AnalysesPresenter analysesPresenter;

  private final ModalProvider<IndexPresenter> indexPresenter;

  private final Provider<ContingencyTablePresenter> crossVariableProvider;

  private final ModalProvider<VariableAttributeModalPresenter> attributeModalProvider;

  private final ModalProvider<VariableTaxonomyModalPresenter> taxonomyModalProvider;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final OpalSystemCache opalSystemCache;

  private Runnable removeConfirmation;

  private Runnable deleteVariablesConfirmation;

  private Boolean sortAscending;

  private Timer indexProgressTimer;

  private String variableFilter;

  private String valuesFilter;

  private String valuesFilterText;

  /**
   * @param display
   * @param eventBus
   */
  @SuppressWarnings({"ConstructorWithTooManyParameters", "PMD.ExcessiveParameterList"})
  @Inject
  public TablePresenter(Display display, EventBus eventBus, PlaceManager placeManager,
                        ValuesTablePresenter valuesTablePresenter,
                        Provider<AnalysesPresenter> analysesPresenterProvider,
                        Provider<ContingencyTablePresenter> crossVariableProvider,
                        Provider<ResourcePermissionsPresenter> resourcePermissionsProvider, ModalProvider<IndexPresenter> indexPresenter,
                        ModalProvider<VariablesToViewPresenter> variablesToViewProvider,
                        ModalProvider<VariablePropertiesModalPresenter> variablePropertiesModalProvider,
                        ModalProvider<ViewModalPresenter> viewPropertiesModalProvider,
                        ModalProvider<ViewWhereModalPresenter> viewWhereModalProvider,
                        ModalProvider<AddVariablesModalPresenter> addVariablesModalProvider,
                        ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider,
                        ModalProvider<DataExportPresenter> dataExportModalProvider,
                        ModalProvider<DataCopyPresenter> dataCopyModalProvider,
                        ModalProvider<ViewCopyPresenter> viewCopyModalProvider,
                        ModalProvider<VariableAttributeModalPresenter> attributeModalProvider,
                        ModalProvider<VariableTaxonomyModalPresenter> taxonomyModalProvider, Translations translations,
                        TranslationMessages translationMessages, OpalSystemCache opalSystemCache) {
    super(eventBus, display);
    this.placeManager = placeManager;
    this.valuesTablePresenter = valuesTablePresenter;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.indexPresenter = indexPresenter.setContainer(this);
    this.variablesToViewProvider = variablesToViewProvider.setContainer(this);
    this.variablePropertiesModalProvider = variablePropertiesModalProvider.setContainer(this);
    this.addVariablesModalProvider = addVariablesModalProvider.setContainer(this);
    this.tablePropertiesModalProvider = tablePropertiesModalProvider.setContainer(this);
    this.viewPropertiesModalProvider = viewPropertiesModalProvider.setContainer(this);
    this.viewWhereModalProvider = viewWhereModalProvider.setContainer(this);
    this.dataExportModalProvider = dataExportModalProvider.setContainer(this);
    this.dataCopyModalProvider = dataCopyModalProvider.setContainer(this);
    this.viewCopyModalProvider = viewCopyModalProvider.setContainer(this);
    this.crossVariableProvider = crossVariableProvider;
    this.attributeModalProvider = attributeModalProvider.setContainer(this);
    this.taxonomyModalProvider = taxonomyModalProvider.setContainer(this);
    this.opalSystemCache = opalSystemCache;

    AnalysisPluginsResource.getInstance().getAnalysisPlugins(new AnalysisPluginsHandler(analysesPresenterProvider));
    getView().setUiHandlers(this);
  }

  @Override
  protected void onReset() {
    super.onReset();
    if (indexProgressTimer != null) {
      indexProgressTimer.cancel();
    }
  }

  @Override
  public void onTableSelectionChanged(TableSelectionChangeEvent event) {
    if (event.hasTable()) {
      updateDisplay(event.getTable());
    } else {
      updateDisplay(event.getDatasourceName(), event.getTableName());
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Values, valuesTablePresenter);
    addEventHandlers();
  }

  @Override
  protected void onHide() {
    super.onHide();
    if (indexProgressTimer != null) {
      indexProgressTimer.cancel();
    }
  }

  private void updateTableIndexStatus() {
    // Table indexation status
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/index").get()
        .authorize(getView().getTableIndexStatusAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/index").delete()
        .authorize(getView().getTableIndexEditAuthorizer()).send();

    updateIndexStatus();
  }

  private void addEventHandlers() {
    addRegisteredHandler(TableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());

    addRegisteredHandler(VariableRefreshEvent.getType(), new VariableRefreshEvent.Handler() {
      @Override
      public void onVariableRefresh(VariableRefreshEvent event) {
        if (table != null) {
          updateVariables();
        }
      }
    });

    registerHandler(getView().addVariableSortHandler(new VariableSortHandler()));

    addRegisteredHandler(TableIndexStatusRefreshEvent.getType(), new TableIndexStatusRefreshHandler());

    // Delete variables confirmation handler
    addRegisteredHandler(ConfirmationEvent.getType(), new DeleteVariableConfirmationEventHandler());

    addRegisteredHandler(ValuesQueryEvent.getType(), new ValuesQueryEvent.ValuesQueryHandler() {
      @Override
      public void onValuesQuery(ValuesQueryEvent event) {
        valuesFilter = event.getQuery();
        valuesFilterText = event.getText();
      }
    });
  }

  private String getIndexResource(String datasource, String tableName) {
    return UriBuilder.create().segment("datasource", "{}", "table", "{}", "index").build(datasource, tableName);
  }

  private void authorize() {
    if (table == null) return;

    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_COMMANDS_EXPORT.create().build(table.getDatasourceName())).post()//
        .authorize(getView().getExportDataAuthorizer())//
        .send();
    // copy data
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_COMMANDS_COPY.create().build(table.getDatasourceName())).post()
        .authorize(getView().getCopyDataAuthorizer()).send();

    // export variables in excel
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/variables/excel").get()
        .authorize(getView().getExcelDownloadAuthorizer()).send();

    if (table.hasViewLink()) {
      // download view
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getViewLink() + "/xml").get()
          .authorize(getView().getViewDownloadAuthorizer()).send();
      // remove view
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).delete()
          .authorize(getView().getRemoveAuthorizer()).send();
      // edit view
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).put()
          .authorize(getView().getEditAuthorizer()).send();
    } else {
      // download view
      getView().getViewDownloadAuthorizer().unauthorized();

      // edit table
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink()).put()
          .authorize(getView().getEditAuthorizer()).send();

      // Drop table
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink()).delete()
          .authorize(getView().getRemoveAuthorizer()).send();
    }

    if (analysesPresenter != null) {
      String url =
        UriBuilders.PROJECT_TABLE_DOWNLOAD_ANALYSES.create().build(table.getDatasourceName(), table.getName());

      ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(url)
        .authorize(getView().getAnalysesDownloadAuthorizer())
        .get()
        .send();

      url = UriBuilders.PROJECT_TABLE_ANALYSES.create().build(table.getDatasourceName(), table.getName());

      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(url) //
        .authorize(new CompositeAuthorizer(getView().getAnalysesAuthorizer(), new AnalysesUpdate())) //
        .get()
        .send();
    }

    // values
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/valueSets").get()
        .authorize(getView().getValuesAuthorizer()).send();

    // set permissions
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(
            UriBuilders.PROJECT_PERMISSIONS_TABLE.create().build(table.getDatasourceName(), table.getName())) //
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())) //
        .post().send();
  }

  private void updateDisplay(final String datasourceName, final String tableName) {
    updateDisplay(datasourceName, tableName, false);
  }

  private void updateDisplay(final String datasourceName, final String tableName, final boolean withSummary) {
    // rely on 304 response
    UriBuilder ub = UriBuilders.DATASOURCE_TABLE.create();
    if (withSummary) ub.query("counts", "true");
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build(datasourceName, tableName))
        .get().withCallback(new ResourceCallback<TableDto>() {
      @Override
      public void onResource(Response response, TableDto resource) {
        if (resource != null) {
          if (!withSummary) {
            updateDisplay(resource);
            // then get the summary
            updateDisplay(datasourceName, tableName, true);
          } else {
            updateDisplay(resource);
            String variableCount = resource.hasVariableCount() ? resource.getVariableCount() + "" : "-";
            String valueSetCount = resource.hasValueSetCount() ? resource.getValueSetCount() + "" : "-";
            getView().setTableSummary(variableCount, valueSetCount);
          }
        }
      }
    })//
        .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().warn("NoSuchValueTable").args(tableName).build());

            placeManager.revealPlace(ProjectPlacesHelper.getTablesPlace(datasourceName));
          }
        }) //
        .withCallback(Response.SC_BAD_REQUEST, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().error((ClientErrorDto) JsonUtils.unsafeEval(response.getText()))
                .build());
          }
        }).send();
  }

  private void updateDisplay(final TableDto tableDto) {
    opalSystemCache.requestTaxonomies(new OpalSystemCache.TaxonomiesHandler() {
      @Override
      public void onTaxonomies(List<TaxonomyDto> taxonomies) {
        table = tableDto;
        getView().initialize(tableDto, taxonomies);
        if (tableIsView()) {
          showViewProperties(table);
        } else {
          getView().setFromTables(null, null);
          getView().setWhereScript(null);
        }
        variableFilter = "";
        valuesFilter = null;
        valuesFilterText = "";
        updateVariables();
        updateTableIndexStatus();
        authorize();
        initProjectCommandsState();

        if (getView().isValuesTabSelected()) {
          valuesTablePresenter.setTable(tableDto);
          valuesTablePresenter.updateValuesDisplay("");
        } else if (getView().isAnalysesTabSelected()) {
          analysesPresenter.setTable(tableDto);
        }
      }
    });
  }

  private void showViewProperties(TableDto tableDto) {
    // Show from tables
    ResourceRequestBuilderFactory.<JsArray<ViewDto>>newBuilder().forResource(tableDto.getViewLink()).get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setFromTables(null, null);
            getView().setWhereScript(null);
          }
        }, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND)//
        .withCallback(new ViewResourceCallback()).send();
  }

  private void updateIndexStatus() {
    fireEvent(new TableValuesIndexUpdatedEvent());
    // If cancellation, call the delete ws
    if (cancelIndexation) {
      ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()
          .forResource(getIndexResource(table.getDatasourceName(), table.getName())).delete()
          .withCallback(new TableIndexStatusUnavailableCallback(), SC_INTERNAL_SERVER_ERROR, SC_FORBIDDEN, SC_NOT_FOUND,
              SC_SERVICE_UNAVAILABLE).withCallback(new TableIndexStatusResourceCallback()).send();

      cancelIndexation = false;
    } else {
      ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()
          .forResource(getIndexResource(table.getDatasourceName(), table.getName())).get()
          .withCallback(new TableIndexStatusUnavailableCallback(), SC_INTERNAL_SERVER_ERROR, SC_FORBIDDEN, SC_NOT_FOUND,
              SC_SERVICE_UNAVAILABLE).withCallback(new TableIndexStatusResourceCallback()).send();
    }
  }

  private void initProjectCommandsState() {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_COMMANDS_STATE.create().build(table.getDatasourceName()))
        .withCallback(SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            String responseText = response.getText();
            getView().toggleReadWriteButtons(!"REFRESHING".equals(responseText));
          }
        }).get().send();
  }

  private void updateVariables() {
    new VariablesFilter() {
      @Override
      public void beforeVariableResourceCallback() {
        getView().beforeRenderRows();
      }

      @Override
      public void onVariableResourceCallback() {
        if (table.getLink().equals(TablePresenter.this.table.getLink())) {
          variables = JsArrays.create();
          for (VariableDto v : results) {
            variables.push(v);
          }

          getView().renderRows(variables);
          getView().afterRenderRows();
        }
      }
    }//
        .withQuery(variableFilter)//
        .withVariable(true)//
        .withLimit(table.getVariableCount())//
        .withSortDir(
            sortAscending == null || sortAscending ? VariablesFilter.SORT_ASCENDING : VariablesFilter.SORT_DESCENDING)//
        .filter(getEventBus(), table);

  }

  private boolean tableIsView() {
    return table.hasViewLink();
  }

  @Override
  public void onShowDictionary() {
    variableFilter = valuesTablePresenter.getView().getFilterText();
    getView().setVariableFilter(variableFilter);

    // Fetch variables
    if (Strings.isNullOrEmpty(variableFilter)) {
      updateVariables();
    } else {
      doFilterVariables();
    }
  }

  @Override
  public void onShowValues() {
    valuesTablePresenter.setTable(table);
    valuesTablePresenter.updateValuesDisplay(variableFilter);
  }

  @Override
  public void onShowAnalyses() {
    analysesPresenter.setTable(table);
  }

  @Override
  public void onExportData() {
    DataExportPresenter provider = dataExportModalProvider.get();
    Set<TableDto> tables = new HashSet<>();
    tables.add(table);
    provider.setExportTables(tables, false);
    provider.setDatasourceName(table.getDatasourceName());
    provider.setValuesQuery(valuesFilter, valuesFilterText);
  }

  @Override
  public void onCopyData() {
    DataCopyPresenter presenter = dataCopyModalProvider.get();
    Set<TableDto> copyTables = new HashSet<>();
    copyTables.add(table);
    presenter.setCopyTables(copyTables, false);
    presenter.setDatasourceName(table.getDatasourceName());
    presenter.setValuesQuery(valuesFilter, valuesFilterText);
  }

  @Override
  public void onCopyView() {
    ViewCopyPresenter presenter = viewCopyModalProvider.get();
    presenter.setView(table);
  }

  @Override
  public void onDownloadDictionary() {
    String downloadUrl = table.getLink() + "/variables/excel";
    fireEvent(new FileDownloadRequestEvent(downloadUrl));
  }

  @Override
  public void onDownloadView() {
    String downloadUrl = UriBuilders.DATASOURCE_VIEW.create().build(table.getDatasourceName(), table.getName()) +
        "/xml";
    fireEvent(new FileDownloadRequestEvent(downloadUrl));
  }

  @Override
  public void onDownloadAnalyses() {
    fireEvent(new FileDownloadRequestEvent(
      UriBuilders.PROJECT_TABLE_DOWNLOAD_ANALYSES
        .create()
        .query("all", "true")
        .build(table.getDatasourceName(), table.getName())
    ));
  }

  @Override
  public void onSearchVariables() {
    fireEvent(new SearchTableVariablesEvent(table.getDatasourceName(), table.getName(), null));
  }

  @Override
  public void onAddVariable() {
    VariablePropertiesModalPresenter propertiesEditorPresenter = variablePropertiesModalProvider.get();
    propertiesEditorPresenter.initialize(table);
  }

  @Override
  public void onAddVariablesFromFile() {
    addVariablesModalProvider.get().initialize(table);
  }

  @Override
  public void onAddVariablesToView(List<VariableDto> variableDtos) {
    if (variableDtos.isEmpty()) {
      fireEvent(NotificationEvent.newBuilder().error("CopyVariableSelectAtLeastOne").build());
    } else {
      VariablesToViewPresenter variablesToViewPresenter = variablesToViewProvider.get();
      variablesToViewPresenter.show(table, variableDtos);
    }
  }

  @Override
  public void onAddVariablesToCart(List<VariableDto> variables) {
    if (variables.isEmpty()) return;
    Map<String, List<VariableDto>> tableVariables = Maps.newHashMap();
    String tableRef = MagmaPath.Builder.datasource(table.getDatasourceName()).table(table.getName()).build();
    tableVariables.put(tableRef, variables);
    fireEvent(new CartAddVariablesEvent(table.getEntityType(), tableVariables));
  }

  @Override
  public void onAnalyseVariables(List<VariableDto> variables) {
    if (variables.isEmpty()) return;
    fireEvent(new AnalyseVariablesRequestEvent(variables));
  }

  @Override
  public void onDeleteVariables(List<VariableDto> variableDtos) {
    if (variableDtos.isEmpty()) {
      fireEvent(NotificationEvent.newBuilder().error("DeleteVariableSelectAtLeastOne").build());
    } else {
      JsArrayString variableNames = JsArrays.create().cast();
      for (VariableDto variable : variableDtos) {
        variableNames.push(variable.getName());
      }

      deleteVariablesConfirmation = new RemoveVariablesRunnable(variableNames);

      fireEvent(ConfirmationRequiredEvent
          .createWithMessages(deleteVariablesConfirmation, translationMessages.removeVariables(),
              translationMessages.confirmRemoveVariables(variableNames.length())));
    }
  }

  @Override
  public void onEdit() {
    if (table.hasViewLink()) {
      UriBuilder ub = UriBuilders.DATASOURCE_VIEW.create();
      ResourceRequestBuilderFactory.<ViewDto>newBuilder()
          .forResource(ub.build(table.getDatasourceName(), table.getName())).get()
          .withCallback(new ResourceCallback<ViewDto>() {
            @Override
            public void onResource(Response response, ViewDto viewDto) {
              ViewModalPresenter p = viewPropertiesModalProvider.get();
              p.initialize(viewDto);
            }
          }).send();
    } else {
      TablePropertiesModalPresenter p = tablePropertiesModalProvider.get();
      p.initialize(table);
    }
  }

  @Override
  public void onEditWhere() {
    if (table.hasViewLink()) {
      UriBuilder ub = UriBuilders.DATASOURCE_VIEW.create();
      ResourceRequestBuilderFactory.<ViewDto>newBuilder()
          .forResource(ub.build(table.getDatasourceName(), table.getName())).get()
          .withCallback(new ResourceCallback<ViewDto>() {
            @Override
            public void onResource(Response response, ViewDto viewDto) {
              ViewWhereModalPresenter p = viewWhereModalProvider.get();
              p.initialize(viewDto);
            }
          }).send();
    }
  }

  @Override
  public void onRemove() {
    removeConfirmation = new RemoveRunnable();

    ConfirmationRequiredEvent event;
    event = tableIsView()
        ? ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeView(),
        translationMessages.confirmRemoveView())
        : ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeTable(),
        translationMessages.confirmRemoveTable());

    fireEvent(event);
  }

  @Override
  public void onIndexClear() {
    ResponseCodeCallback callback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if (response.getStatusCode() == SC_OK) {
          updateIndexStatus();
        } else {
          fireEvent(
              NotificationEvent.newBuilder().error((ClientErrorDto) JsonUtils.unsafeEval(response.getText())).build());
        }
      }

    };
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
        .forResource(UriBuilders.DATASOURCE_TABLE_INDEX.create().build(table.getDatasourceName(), table.getName()))//
        .withCallback(callback, SC_OK, SC_SERVICE_UNAVAILABLE).delete().send();
  }

  @Override
  public void onIndexNow() {
    ResponseCodeCallback callback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if (response.getStatusCode() == SC_OK) {
          // Wait a few seconds for the task to launch before checking its status
          indexProgressTimer = new Timer() {
            @Override
            public void run() {
              updateIndexStatus();
            }
          };
          // Schedule the timer to run once in X seconds.
          indexProgressTimer.schedule(DELAY_MILLIS);

        } else {
          fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
        }
      }

    };
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
        .forResource(getIndexResource(table.getDatasourceName(), table.getName()))//
        .withCallback(callback, SC_OK, SC_SERVICE_UNAVAILABLE).put().send();
  }

  @Override
  public void onIndexCancel() {
    ResponseCodeCallback callback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if (response.getStatusCode() == SC_OK) {
          cancelIndexation = true;
          updateIndexStatus();
        } else {
          ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
          fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).args(error.getArgumentsArray()).build());
        }
      }

    };
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
        .forResource(UriBuilders.DATASOURCE_TABLE_INDEX.create().build(table.getDatasourceName(), table.getName()))//
        .withCallback(callback, SC_OK, SC_SERVICE_UNAVAILABLE).delete().send();
  }

  @Override
  public void onIndexSchedule() {
    List<TableIndexStatusDto> objects = new ArrayList<>();
    objects.add(statusDto);

    IndexPresenter dialog = indexPresenter.get();
    dialog.setUpdateMethodCallbackRefreshIndices(false);
    dialog.setUpdateMethodCallbackRefreshTable(true);
    dialog.updateSchedules(objects);
  }

  @Override
  public void onCrossVariables() {
    // If both variables have been selected
    String selectedVariableName = getView().getSelectedVariableName();
    final String crossWithVariableName = getView().getCrossWithVariableName();

    if (!selectedVariableName.isEmpty() && !crossWithVariableName.isEmpty()) {
      ResourceRequestBuilderFactory.<VariableDto>newBuilder() //
          .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
              .build(table.getDatasourceName(), table.getName(), selectedVariableName)) //
          .get() //
          .withCallback(new SelectedVariableCallback(crossWithVariableName)).withCallback(
          new VariableNotFoundCallback(selectedVariableName), Response.SC_NOT_FOUND)//
          .send();
    }
  }

  @Override
  public void onApplyCustomAttribute(List<VariableDto> selectedItems) {
    VariableAttributeModalPresenter presenter = attributeModalProvider.get();
    presenter.setDialogMode(BaseVariableAttributeModalPresenter.Mode.APPLY);
    presenter.initialize(table, selectedItems);
  }

  @Override
  public void onApplyTaxonomyAttribute(List<VariableDto> selectedItems) {
    VariableTaxonomyModalPresenter presenter = taxonomyModalProvider.get();
    presenter.setDialogMode(BaseVariableAttributeModalPresenter.Mode.APPLY);
    presenter.initialize(table, selectedItems);
  }

  @Override
  public void onDeleteCustomAttribute(List<VariableDto> selectedItems) {
    VariableAttributeModalPresenter presenter = attributeModalProvider.get();
    presenter.setDialogMode(BaseVariableAttributeModalPresenter.Mode.DELETE);
    presenter.initialize(table, selectedItems);
  }

  @Override
  public void onDeleteTaxonomyAttribute(List<VariableDto> selectedItems) {
    VariableTaxonomyModalPresenter presenter = taxonomyModalProvider.get();
    presenter.setDialogMode(BaseVariableAttributeModalPresenter.Mode.DELETE);
    presenter.initialize(table, selectedItems);
  }

  @Override
  public void onVariablesFilterUpdate(String filter) {
    variableFilter = filter;
    if (Strings.isNullOrEmpty(filter)) {
      updateVariables();
    } else if (filter.length() > 1) {
      doFilterVariables();
    }
  }

  private final class VariableSortHandler implements ColumnSortEvent.Handler {

    @Override
    public void onColumnSort(ColumnSortEvent event) {
      sortAscending = event.isSortAscending();
      updateDisplay(table);
    }
  }

  private void doFilterVariables() {

    new VariablesFilter() {
      @Override
      public void beforeVariableResourceCallback() {
        getView().beforeRenderRows();
      }

      @Override
      public void onVariableResourceCallback() {
        if (table.getLink().equals(TablePresenter.this.table.getLink())) {
          TablePresenter.this.table = table;

          variables = JsArrays.create();
          for (VariableDto v : results) {
            variables.push(v);
          }
          getView().renderRows(variables);
          getView().afterRenderRows();
        }
      }
    }//
        .withVariable(true)//
        .withQuery(variableFilter)//
        .withLimit(table.getVariableCount())//
        .withSortDir(
            sortAscending == null || sortAscending ? VariablesFilter.SORT_ASCENDING : VariablesFilter.SORT_DESCENDING)//
        .filter(getEventBus(), table);
  }

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if (removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

  private class AnalysisPluginsHandler implements AnalysisPluginsResource.Handler {

    private final Provider<AnalysesPresenter> analysesPresenterProvider;

    AnalysisPluginsHandler(Provider<AnalysesPresenter> provider) {
      this.analysesPresenterProvider = provider;
    }

    @Override
    public void handle(List<PluginPackageDto> plugins) {
      if (plugins.isEmpty()) {
        getView().enableAnalyses(false);
      } else {
        analysesPresenter = analysesPresenterProvider.get();
        analysesPresenter.setPlugins(plugins);
        setInSlot(Slots.Analyses, analysesPresenter);
      }
    }
  }

  private class DeleteVariableConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if (deleteVariablesConfirmation != null && event.getSource().equals(deleteVariablesConfirmation) &&
          event.isConfirmed()) {
        deleteVariablesConfirmation.run();
        deleteVariablesConfirmation = null;
      }
    }
  }

  private class TableIndexStatusUnavailableCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getView().setIndexStatusVisible(false);
    }
  }

  private class TableIndexStatusResourceCallback implements ResourceCallback<JsArray<TableIndexStatusDto>> {

    @Override
    public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {

      // Hide contingency table and show only if up to date or outdated
      getView().hideContingencyTable();
      if (response.getStatusCode() == SC_OK) {
        getView().setIndexStatusVisible(true);
        statusDto = TableIndexStatusDto.get(JsArrays.toSafeArray(resource));
        getView().setIndexStatusAlert(statusDto);

        // Refetch if in progress
        if (statusDto.getStatus().getName().equals(TableIndexationStatus.IN_PROGRESS.getName())) {

          // Hide the Cancel button if progress is 100%
          getView().setCancelVisible(Double.compare(statusDto.getProgress(), 1d) < 0);

          indexProgressTimer = new Timer() {
            @Override
            public void run() {
              updateIndexStatus();
            }
          };

          // Schedule the timer to run once in X seconds.
          indexProgressTimer.schedule(DELAY_MILLIS);
        } else if (statusDto.getStatus().isTableIndexationStatus(TableIndexationStatus.UPTODATE) ||
            statusDto.getStatus().isTableIndexationStatus(TableIndexationStatus.OUTDATED)) {
          updateContingencyTableVariables();
        }
      }
    }

    private void updateContingencyTableVariables() {
      TableIndexationStatus status = statusDto.getStatus();
      if (status.isTableIndexationStatus(TableIndexationStatus.UPTODATE)) {
        ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(table.getLink() + "/variables")
            .get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableDto> resource) {
            getView().initContingencyTable(JsArrays.toSafeArray(resource));
          }
        }).send();
      }
      fireEvent(new TableValuesIndexUpdatedEvent());
    }
  }

  private class ViewResourceCallback implements ResourceCallback<JsArray<ViewDto>> {

    @Override
    public void onResource(Response response, JsArray<ViewDto> resource) {
      ViewDto viewDto = ViewDto.get(JsArrays.toSafeArray(resource));
      getView().setFromTables(viewDto.getFromArray(), viewDto.getInnerFromArray());
      getView().setWhereScript(viewDto.hasWhere() ? viewDto.getWhere() : null);
    }
  }

  private class TableIndexStatusRefreshHandler implements TableIndexStatusRefreshEvent.Handler {

    @Override
    public void onRefresh(TableIndexStatusRefreshEvent event) {
      updateIndexStatus();
    }
  }

  public interface Display extends View, HasUiHandlers<TableUiHandlers> {

    enum Slots {
      Permissions, Values, ContingencyTable, Analyses
    }

    void beforeRenderRows();

    void renderRows(JsArray<VariableDto> rows);

    void afterRenderRows();

    void initContingencyTable(JsArray<VariableDto> rows);

    void clear(boolean cleanFilter);

    void initialize(TableDto dto, List<TaxonomyDto> taxonomies);

    void setTableSummary(String variableCount, String valueSetCount);

    HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler);

    HasAuthorization getCopyDataAuthorizer();

    HasAuthorization getExcelDownloadAuthorizer();

    HasAuthorization getViewDownloadAuthorizer();

    HasAuthorization getAnalysesDownloadAuthorizer();

    HasAuthorization getExportDataAuthorizer();

    HasAuthorization getRemoveAuthorizer();

    HasAuthorization getEditAuthorizer();

    HasAuthorization getValuesAuthorizer();

    HasAuthorization getTableIndexStatusAuthorizer();

    HasAuthorization getTableIndexEditAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    HasAuthorization getAnalysesAuthorizer();

    void enableAnalyses(boolean enable);

    boolean isValuesTabSelected();

    boolean isAnalysesTabSelected();

    void setIndexStatusVisible(boolean b);

    void setIndexStatusAlert(TableIndexStatusDto statusDto);

    void setFromTables(JsArrayString tables, JsArrayString innerTables);

    void setWhereScript(String script);

    void setCancelVisible(boolean b);

    String getSelectedVariableName();

    String getCrossWithVariableName();

    void hideContingencyTable();

    void setVariableFilter(String variableFilter);

    void toggleReadWriteButtons(boolean toggleOn);
  }

  private class RemoveRunnable implements Runnable {
    @Override
    public void run() {
      if (tableIsView()) {
        removeView();
      } else {
        removeTable();
      }
    }

    private void gotoDatasource() {
      placeManager.revealPlace(ProjectPlacesHelper.getDatasourcePlace(table.getDatasourceName()));
    }

    private void removeView() {
      ResponseCodeCallback callbackHandler = newResponseCodeCallback();
      ResourceRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).delete()
          .withCallback(SC_OK, callbackHandler).withCallback(SC_FORBIDDEN, callbackHandler)
          .withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

    private void removeTable() {
      ResponseCodeCallback callbackHandler = newResponseCodeCallback();
      ResourceRequestBuilderFactory.newBuilder().forResource(table.getLink()).delete()
          .withCallback(SC_OK, callbackHandler).withCallback(SC_FORBIDDEN, callbackHandler)
          .withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

    private ResponseCodeCallback newResponseCodeCallback() {
      return new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          fireEvent(ConfirmationTerminatedEvent.create());
          if (response.getStatusCode() == SC_OK) {
            gotoDatasource();
          } else {
            String errorMessage = response.getText().isEmpty() ? response.getStatusCode() == SC_FORBIDDEN
                ? "Forbidden"
                : "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };
    }

  }

  private class RemoveVariablesRunnable implements Runnable {

    private static final int BATCH_SIZE = 20;

    int nb_deleted = 0;

    final JsArrayString variableNames;

    private RemoveVariablesRunnable(JsArrayString variableNames) {
      this.variableNames = variableNames;
    }

    private String getUri() {
      UriBuilder uriBuilder = tableIsView()
          ? UriBuilders.DATASOURCE_VIEW_VARIABLES.create()
          : UriBuilders.DATASOURCE_TABLE_VARIABLES.create();

      for (int i = nb_deleted, added = 0; i < variableNames.length() && added < BATCH_SIZE; i++, added++) {
        uriBuilder.query("variable", variableNames.get(i));
      }

      return uriBuilder.build(table.getDatasourceName(), table.getName());
    }

    @Override
    public void run() {
      // show loading
      getView().beforeRenderRows();
      ResourceRequestBuilderFactory.newBuilder().forResource(getUri())//
          .delete()//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(ConfirmationTerminatedEvent.create());
              if (response.getStatusCode() == SC_OK) {
                nb_deleted += BATCH_SIZE;

                if (nb_deleted < variableNames.length()) {
                  run();
                } else {
                  placeManager
                      .revealPlace(ProjectPlacesHelper.getTablePlace(table.getDatasourceName(), table.getName()));
                }
              } else {
                String errorMessage = response.getText().isEmpty() ? response.getStatusCode() == SC_FORBIDDEN
                    ? "Forbidden"
                    : "UnknownError" : response.getText();
                fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
              }

            }
          }, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND).send();
    }
  }

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.TABLE,
          ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_TABLE, table.getDatasourceName(),
          table.getName());

      setInSlot(Display.Slots.Permissions, resourcePermissionsPresenter);
    }
  }

  /**
   * Update analyses on authorization.
   */
  private final class AnalysesUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      getView().enableAnalyses(false);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      getView().enableAnalyses(true);
    }
  }

  private class VariableNotFoundCallback implements ResponseCodeCallback {

    final String name;

    protected VariableNotFoundCallback(String name) {
      this.name = name;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(NotificationEvent.newBuilder()
          .error(TranslationsUtils.replaceArguments(translations.variableNotFound(), name)).build());
    }
  }

  private class SelectedVariableCallback implements ResourceCallback<VariableDto> {
    private final String crossWithVariableName;

    public SelectedVariableCallback(String crossWithVariableName) {
      this.crossWithVariableName = crossWithVariableName;
    }

    @Override
    public void onResource(Response response, final VariableDto variableDto) {
      // check selected variable is valid
      if (VariableDtos.nature(variableDto) == VariableDtos.VariableNature.CATEGORICAL) {
        ResourceRequestBuilderFactory.<VariableDto>newBuilder() //
            .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
                .build(table.getDatasourceName(), table.getName(), crossWithVariableName)) //
            .get() //
            .withCallback(new CrossVariableCallback(variableDto)).withCallback(
            new VariableNotFoundCallback(crossWithVariableName), Response.SC_NOT_FOUND)//
            .send();
      } else {
        fireEvent(NotificationEvent.newBuilder()
            .error(TranslationsUtils.replaceArguments(translations.variableNotCategorical(), variableDto.getName()))
            .build());
      }
    }//
  }

  private class CrossVariableCallback implements ResourceCallback<VariableDto> {
    private final VariableDto variableDto;

    public CrossVariableCallback(VariableDto variableDto) {
      this.variableDto = variableDto;
    }

    @Override
    public void onResource(Response response, VariableDto crossWithVariable) {
      // check cross variable is valid
      VariableDtos.VariableNature nature = VariableDtos.nature(crossWithVariable);
      if (nature == VariableDtos.VariableNature.CATEGORICAL || nature == VariableDtos.VariableNature.CONTINUOUS) {
        ContingencyTablePresenter crossVariablePresenter = crossVariableProvider.get();
        crossVariablePresenter.initialize(table, variableDto, crossWithVariable);
        setInSlot(Display.Slots.ContingencyTable, crossVariablePresenter);
      } else {
        fireEvent(NotificationEvent.newBuilder().error(TranslationsUtils
            .replaceArguments(translations.variableNotCategoricalNorContinuous(), crossWithVariable.getName()))
            .build());
      }
    }//
  }
}
