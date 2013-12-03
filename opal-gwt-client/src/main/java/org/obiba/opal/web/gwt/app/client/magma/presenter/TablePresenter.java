/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.TableIndexStatusRefreshEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.ViewPropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.VariablesFilter;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;
import static com.google.gwt.http.client.Response.SC_SERVICE_UNAVAILABLE;

public class TablePresenter extends PresenterWidget<TablePresenter.Display>
    implements TableUiHandlers, TableSelectionChangeEvent.Handler {

  private static final int DELAY_MILLIS = 2000;

  private JsArray<VariableDto> variables;

  private TableDto table;

  private TableIndexStatusDto statusDto;

  private boolean cancelIndexation = false;

  private final PlaceManager placeManager;

  private final Provider<AuthorizationPresenter> authorizationPresenter;

  private final ModalProvider<ConfigureViewStepPresenter> configureViewStepProvider;

  private final ModalProvider<VariablesToViewPresenter> variablesToViewProvider;

  private final ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider;

  private final ModalProvider<ViewPropertiesModalPresenter> viewPropertiesModalProvider;

  private final ModalProvider<VariablePropertiesModalPresenter> variablePropertiesModalProvider;

  private final ModalProvider<DataExportPresenter> dataExportModalProvider;

  private final ModalProvider<DataCopyPresenter> dataCopyModalProvider;

  @Inject
  private CodingViewModalPresenter codingViewModalPresenter;

  private final ValuesTablePresenter valuesTablePresenter;

  private final Provider<IndexPresenter> indexPresenter;

  private final Translations translations;

  private Runnable removeConfirmation;

  private Runnable deleteVariablesConfirmation;

  private Boolean sortAscending;

  private Timer indexProgressTimer;

  /**
   * @param display
   * @param eventBus
   */
  @SuppressWarnings({ "ConstructorWithTooManyParameters", "PMD.ExcessiveParameterList" })
  @Inject
  public TablePresenter(Display display, EventBus eventBus, PlaceManager placeManager,
      ValuesTablePresenter valuesTablePresenter, Provider<AuthorizationPresenter> authorizationPresenter,
      Provider<IndexPresenter> indexPresenter, ModalProvider<ConfigureViewStepPresenter> configureViewStepProvider,
      ModalProvider<VariablesToViewPresenter> variablesToViewProvider,
      ModalProvider<VariablePropertiesModalPresenter> variablePropertiesModalProvider,
      ModalProvider<ViewPropertiesModalPresenter> viewPropertiesModalProvider,
      ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider,
      ModalProvider<DataExportPresenter> dataExportModalProvider,
      ModalProvider<DataCopyPresenter> dataCopyModalProvider, Translations translations) {

    super(eventBus, display);
    this.placeManager = placeManager;
    this.valuesTablePresenter = valuesTablePresenter;
    this.authorizationPresenter = authorizationPresenter;
    this.indexPresenter = indexPresenter;
    this.translations = translations;
    this.configureViewStepProvider = configureViewStepProvider.setContainer(this);
    this.variablesToViewProvider = variablesToViewProvider.setContainer(this);
    this.variablePropertiesModalProvider = variablePropertiesModalProvider.setContainer(this);
    this.tablePropertiesModalProvider = tablePropertiesModalProvider.setContainer(this);
    this.viewPropertiesModalProvider = viewPropertiesModalProvider.setContainer(this);
    this.dataExportModalProvider = dataExportModalProvider.setContainer(this);
    this.dataCopyModalProvider = dataCopyModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void onTableSelectionChanged(TableSelectionChangeEvent event) {
    if(event.hasTable()) {
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
    if(indexProgressTimer != null) {
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
        updateVariables();
      }
    });

    getView().setValuesTabCommand(new ValuesCommand());
    getView().setVariablesTabCommand(new VariablesCommand());

    registerHandler(getView().addVariableSortHandler(new VariableSortHandler()));

    // Filter variable event
    registerHandler(getView().addFilterVariableHandler(new FilterVariableHandler()));
    // Filter: Clear filter event
    registerHandler(getView().getFilter().getClear().addClickHandler(new FilterClearHandler()));

    // OPAL-975
    addRegisteredHandler(ViewSavedEvent.getType(), new ViewSavedEventHandler());

    addRegisteredHandler(TableIndexStatusRefreshEvent.getType(), new TableIndexStatusRefreshHandler());

    // Delete variables confirmation handler
    addRegisteredHandler(ConfirmationEvent.getType(), new DeleteVariableConfirmationEventHandler());
  }

  private String getIndexResource(String datasource, String tableName) {
    return UriBuilder.create().segment("datasource", "{}", "table", "{}", "index").build(datasource, tableName);
  }

  private void authorize() {
    if(table == null) return;

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

    if(table.hasViewLink()) {
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

  private void updateDisplay(String datasourceName, String tableName) {
    // rely on 304 response
    UriBuilder ub = UriBuilders.DATASOURCE_TABLE.create().query("counts", "true");
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build(datasourceName, tableName)).get()
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            if(resource != null) {
              updateDisplay(resource);
            }
          }
        }).send();
  }

  private void updateDisplay(TableDto tableDto) {
    getView().clear(table == null || !table.getLink().equals(tableDto.getLink()));

    table = tableDto;

    getView().setTable(tableDto);

    if(tableIsView()) {
      showFromTables(table);
    } else {
      getView().setFromTables(null);
    }

    if(getView().isValuesTabSelected()) {
      valuesTablePresenter.setTable(tableDto);
    }

    updateVariables();
    updateTableIndexStatus();
    authorize();
  }

  private void showFromTables(TableDto tableDto) {// Show from tables
    ResourceRequestBuilderFactory.<JsArray<ViewDto>>newBuilder().forResource(tableDto.getViewLink()).get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setFromTables(null);
          }
        }, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND)//
        .withCallback(new ViewResourceCallback()).send();
  }

  private void updateIndexStatus() {
    // If cancellation, call the delete ws
    if(cancelIndexation) {
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

  private void updateVariables() {
    new VariablesFilter() {
      @Override
      public void beforeVariableResourceCallback() {
        getView().beforeRenderRows();
      }

      @Override
      public void onVariableResourceCallback() {
        if(table.getLink().equals(TablePresenter.this.table.getLink())) {
          variables = JsArrays.create();
          for(VariableDto v : results) {
            variables.push(v);
          }

          getView().renderRows(variables);
          getView().afterRenderRows();
        }
      }
    }//
        .withQuery(getView().getFilter().getText())//
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
  public void onExportData() {
    DataExportPresenter export = dataExportModalProvider.get();
    Set<TableDto> tables = new HashSet<TableDto>();
    tables.add(table);
    export.setExportTables(tables, false);
    export.setDatasourceName(table.getDatasourceName());
  }

  @Override
  public void onCopyData() {
    DataCopyPresenter copy = dataCopyModalProvider.get();
    Set<TableDto> copyTables = new HashSet<TableDto>();
    copyTables.add(table);
    copy.setCopyTables(copyTables, false);
    copy.setDatasourceName(table.getDatasourceName());
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
  public void onAddVariable() {
    VariablePropertiesModalPresenter propertiesEditorPresenter = variablePropertiesModalProvider.get();
    propertiesEditorPresenter.initialize(table);
  }

  @Override
  public void onAddVariablesToView(List<VariableDto> variableDtos) {
    if(variableDtos.isEmpty()) {
      fireEvent(NotificationEvent.newBuilder().error("CopyVariableSelectAtLeastOne").build());
    } else {
      VariablesToViewPresenter variablesToViewPresenter = variablesToViewProvider.get();
      variablesToViewPresenter.initialize(table, variableDtos);
    }
  }

  @Override
  public void onDeleteVariables(List<VariableDto> variableDtos) {
    if(variableDtos.isEmpty()) {
      fireEvent(NotificationEvent.newBuilder().error("DeleteVariableSelectAtLeastOne").build());
    } else {
      JsArrayString variableNames = JsArrays.create().cast();
      for(VariableDto variable : variableDtos) {
        variableNames.push(variable.getName());
      }

      deleteVariablesConfirmation = new RemoveVariablesRunnable(variableNames);

      fireEvent(ConfirmationRequiredEvent
          .createWithMessages(deleteVariablesConfirmation, translations.confirmationTitleMap().get("deleteVariables"),
              TranslationsUtils.replaceArguments(translations.confirmationMessageMap()
                  .get(variableNames.length() > 1 ? "confirmDeleteVariables" : "confirmDeleteVariable"),
                  String.valueOf(variableNames.length()))));
    }
  }

  @Override
  public void onEdit() {
    if(table.hasViewLink()) {
      UriBuilder ub = UriBuilders.DATASOURCE_VIEW.create();
      ResourceRequestBuilderFactory.<ViewDto>newBuilder()
          .forResource(ub.build(table.getDatasourceName(), table.getName())).get()
          .withCallback(new ResourceCallback<ViewDto>() {
            @Override
            public void onResource(Response response, ViewDto viewDto) {
              ViewPropertiesModalPresenter p = viewPropertiesModalProvider.get();
              p.initialize(viewDto);
            }
          }).send();
    } else {
      TablePropertiesModalPresenter p = tablePropertiesModalProvider.get();
      p.initialize(table);
    }
  }

  @Override
  public void onRemove() {
    removeConfirmation = new RemoveRunnable();

    ConfirmationRequiredEvent event;
    event = tableIsView()
        ? ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeView", "confirmRemoveView")
        : ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeTable", "confirmRemoveTable");

    fireEvent(event);
  }

  @Override
  public void onIndexClear() {
    ResponseCodeCallback callback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == SC_OK) {
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
  public void onIndexNow() {
    ResponseCodeCallback callback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == SC_OK) {
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
        if(response.getStatusCode() == SC_OK) {
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
    List<TableIndexStatusDto> objects = new ArrayList<TableIndexStatusDto>();
    objects.add(statusDto);

    IndexPresenter dialog = indexPresenter.get();
    dialog.setUpdateMethodCallbackRefreshIndices(false);
    dialog.setUpdateMethodCallbackRefreshTable(true);
    dialog.updateSchedules(objects);
    addToPopupSlot(dialog);
  }

  private final class ValuesCommand implements Command {

    @Override
    public void execute() {
      valuesTablePresenter.setTable(table);
      valuesTablePresenter.setFilter(getView().getFilter().getText());
    }
  }

  private final class VariablesCommand implements Command {

    @Override
    public void execute() {
      getView().getFilter().setText(valuesTablePresenter.getView().getFilterText());

      // Fetch variables
      if(valuesTablePresenter.getView().getFilterText().isEmpty()) {
        updateVariables();
      } else {
        doFilterVariables();
      }
    }
  }

  private final class VariableSortHandler implements ColumnSortEvent.Handler {

    @Override
    public void onColumnSort(ColumnSortEvent event) {
      sortAscending = event.isSortAscending();
      updateDisplay(table);
    }
  }

  private final class FilterVariableHandler implements KeyUpHandler {

    @Override
    public void onKeyUp(KeyUpEvent event) {
      String filter = getView().getFilter().getText();

      if(filter.isEmpty()) {
        updateVariables();
      } else if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
        doFilterVariables();
      }
    }
  }

  private void doFilterVariables() {
    String query = getView().getFilter().getText();

    new VariablesFilter() {
      @Override
      public void beforeVariableResourceCallback() {
        getView().beforeRenderRows();
      }

      @Override
      public void onVariableResourceCallback() {
        if(table.getLink().equals(TablePresenter.this.table.getLink())) {
          TablePresenter.this.table = table;

          variables = JsArrays.create();
          for(VariableDto v : results) {
            variables.push(v);
          }
          getView().renderRows(variables);
          getView().afterRenderRows();
        }
      }
    }//
        .withVariable(true)//
        .withQuery(query)//
        .withLimit(table.getVariableCount())//
        .withSortDir(
            sortAscending == null || sortAscending ? VariablesFilter.SORT_ASCENDING : VariablesFilter.SORT_DESCENDING)//
        .filter(getEventBus(), table);
  }

  private final class FilterClearHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      updateVariables();
    }
  }

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

  private class DeleteVariableConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(deleteVariablesConfirmation != null && event.getSource().equals(deleteVariablesConfirmation) &&
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

      if(response.getStatusCode() == SC_OK) {
        getView().setIndexStatusVisible(true);
        statusDto = TableIndexStatusDto.get(JsArrays.toSafeArray(resource));
        getView().setIndexStatusAlert(statusDto);

        // Refetch if in progress
        if(statusDto.getStatus().getName().equals(TableIndexationStatus.IN_PROGRESS.getName())) {

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
        }
      }
    }
  }

  private class ViewResourceCallback implements ResourceCallback<JsArray<ViewDto>> {

    @Override
    public void onResource(Response response, JsArray<ViewDto> resource) {
      ViewDto viewDto = ViewDto.get(JsArrays.toSafeArray(resource));
      getView().setFromTables(viewDto.getFromArray());
    }
  }

  // OPAL-975
  private class ViewSavedEventHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      if(table != null) {
        updateDisplay(table);
      }
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
      Permissions, Values
    }

    void beforeRenderRows();

    void renderRows(JsArray<VariableDto> rows);

    void afterRenderRows();

    void clear(boolean cleanFilter);

    void setTable(TableDto dto);

    HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler);

    HasAuthorization getCopyDataAuthorizer();

    HasAuthorization getExcelDownloadAuthorizer();

    HasAuthorization getViewDownloadAuthorizer();

    HasAuthorization getExportDataAuthorizer();

    HasAuthorization getRemoveAuthorizer();

    HasAuthorization getEditAuthorizer();

    HasAuthorization getValuesAuthorizer();

    HasAuthorization getTableIndexStatusAuthorizer();

    HasAuthorization getTableIndexEditAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    void setValuesTabCommand(Command cmd);

    void setVariablesTabCommand(Command cmd);

    boolean isValuesTabSelected();

    void setIndexStatusVisible(boolean b);

    void setIndexStatusAlert(TableIndexStatusDto statusDto);

    void setFromTables(JsArrayString tables);

    HandlerRegistration addFilterVariableHandler(KeyUpHandler handler);

    TextBoxClearable getFilter();

    void setCancelVisible(boolean b);
  }

  private class RemoveRunnable implements Runnable {
    @Override
    public void run() {
      if(tableIsView()) {
        removeView();
      } else {
        removeTable();
      }
    }

    private void gotoDatasource() {
      placeManager.revealPlace(ProjectPlacesHelper.getDatasourcePlace(table.getDatasourceName()));
    }

    private void removeView() {

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            gotoDatasource();
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      ResourceRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).delete()
          .withCallback(SC_OK, callbackHandler).withCallback(SC_FORBIDDEN, callbackHandler)
          .withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

    private void removeTable() {

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            gotoDatasource();
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      ResourceRequestBuilderFactory.newBuilder().forResource(table.getLink()).delete()
          .withCallback(SC_OK, callbackHandler).withCallback(SC_FORBIDDEN, callbackHandler)
          .withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(SC_NOT_FOUND, callbackHandler).send();
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

      for(int i = nb_deleted, added = 0; i < variableNames.length() && added < BATCH_SIZE; i++, added++) {
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
              if(response.getStatusCode() == SC_OK) {
                nb_deleted += BATCH_SIZE;

                if(nb_deleted < variableNames.length()) {
                  run();
                } else {
                  placeManager
                      .revealPlace(ProjectPlacesHelper.getTablePlace(table.getDatasourceName(), table.getName()));
                }
              } else {

                String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
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
      AuthorizationPresenter authz = authorizationPresenter.get();
      UriBuilder nodeBuilder = UriBuilder.create().segment("datasource", table.getDatasourceName());

      if(table.hasViewLink()) {
        String node = nodeBuilder.segment("view", table.getName()).build();
        authz.setAclRequest("view", new AclRequest(AclAction.TABLE_READ, node), //
            new AclRequest(AclAction.TABLE_VALUES, node), //
            new AclRequest(AclAction.TABLE_EDIT, node), //
            new AclRequest(AclAction.TABLE_VALUES_EDIT, node), //
            new AclRequest(AclAction.TABLE_ALL, node));
      } else {
        String node = nodeBuilder.segment("table", table.getName()).build();
        authz.setAclRequest("table", new AclRequest(AclAction.TABLE_READ, node), //
            new AclRequest(AclAction.TABLE_VALUES, node), //
            new AclRequest(AclAction.TABLE_EDIT, node),//
            new AclRequest(AclAction.TABLE_VALUES_EDIT, node),//
            new AclRequest(AclAction.TABLE_ALL, node));
      }
      setInSlot(Display.Slots.Permissions, authz);
    }
  }

}
