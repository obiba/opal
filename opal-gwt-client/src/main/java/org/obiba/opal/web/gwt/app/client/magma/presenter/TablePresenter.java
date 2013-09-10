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
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.CopyVariablesToViewEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.SiblingTableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.magma.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableIndexStatusRefreshEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.VariablesFilter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.cell.client.FieldUpdater;
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
import com.google.gwt.user.cellview.client.Column;
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

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;
import static com.google.gwt.http.client.Response.SC_SERVICE_UNAVAILABLE;

public class TablePresenter extends PresenterWidget<TablePresenter.Display>
    implements TableUiHandlers, TableSelectionChangeEvent.Handler, VariableSelectionChangeEvent.Handler {

  private static final int DELAY_MILLIS = 1000;

  private JsArray<VariableDto> variables;

  private TableDto table;

  private TableIndexStatusDto statusDto;

  private String previous;

  private String next;

  private boolean cancelIndexation = false;

  private final Provider<AuthorizationPresenter> authorizationPresenter;

  private final ModalProvider<ConfigureViewStepPresenter> configureViewStepProvider;

  private final ModalProvider<VariablesToViewPresenter> variablesToViewProvider;

  @Inject
  private CodingViewModalPresenter codingViewModalPresenter;

  private final ValuesTablePresenter valuesTablePresenter;

  private final Provider<IndexPresenter> indexPresenter;

  private Runnable removeConfirmation;

  private Boolean sortAscending;

  private Column<?, ?> sortColumn;

  private boolean tableUpdatePending = false;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public TablePresenter(Display display, EventBus eventBus, ValuesTablePresenter valuesTablePresenter,
      Provider<AuthorizationPresenter> authorizationPresenter, Provider<IndexPresenter> indexPresenter,
      ModalProvider<ConfigureViewStepPresenter> configureViewStepProvider,
      ModalProvider<VariablesToViewPresenter> variablesToViewProvider) {
    super(eventBus, display);
    this.valuesTablePresenter = valuesTablePresenter;
    this.authorizationPresenter = authorizationPresenter;
    this.indexPresenter = indexPresenter;
    this.configureViewStepProvider = configureViewStepProvider.setContainer(this);
    this.variablesToViewProvider = variablesToViewProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void onTableSelectionChanged(TableSelectionChangeEvent event) {
    if(event.hasTable()) {
      updateDisplay(event.getTable(), event.getPrevious(), event.getNext());
    } else {
      updateDisplay(event.getDatasourceName(), event.getTableName(), event.getPrevious(), event.getNext());
    }
  }

  @Override
  public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
    if(this != event.getSource() && event.hasTable() && event.getTable().hasValueSetCount()) {
      updateDisplay(event.getTable(), null, null);
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Values, valuesTablePresenter);

    addEventHandlers();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
  }

  private void updateTableIndexStatus() {
    // Table indexation status
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/index").get()
        .authorize(getView().getTableIndexStatusAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/index").delete()
        .authorize(getView().getTableIndexEditAuthorizer()).send();

    updateIndexStatus();
  }

  @SuppressWarnings({ "OverlyLongMethod" })
  private void addEventHandlers() {
    registerHandler(getEventBus().addHandler(TableSelectionChangeEvent.getType(), this));
    registerHandler(
        getEventBus().addHandler(VariableSelectionChangeEvent.getType(), this));
    registerHandler(
        getEventBus().addHandler(SiblingVariableSelectionEvent.getType(), new SiblingVariableSelectionHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));
    getView().setValuesTabCommand(new ValuesCommand());
    getView().setVariablesTabCommand(new VariablesCommand());

    FieldUpdater<VariableDto, String> updater = new VariableNameFieldUpdater();
    getView().setVariableNameFieldUpdater(updater);
    getView().setVariableIndexFieldUpdater(updater);
    registerHandler(getView().addVariableSortHandler(new VariableSortHandler()));

    // Filter variable event
    registerHandler(getView().addFilterVariableHandler(new FilterVariableHandler()));
    // Filter: Clear filter event
    registerHandler(getView().getFilter().getClear().addClickHandler(new FilterClearHandler()));

    // OPAL-975
    registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedEventHandler()));

    registerHandler(
        getEventBus().addHandler(TableIndexStatusRefreshEvent.getType(), new TableIndexStatusRefreshHandler()));
  }

  private String getIndexResource(String datasource, String table) {
    return UriBuilder.create().segment("datasource", "{}", "table", "{}", "index").build(datasource, table);
  }

  private void authorize() {
    if(table == null) return;

    UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName());
    // export variables in excel
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/variables/excel").get()
        .authorize(getView().getExcelDownloadAuthorizer()).send();
    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/commands/_copy").post()//
        .authorize(getView().getExportDataAuthorizer())//
        .send();
    // copy data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/commands/_copy").post()
        .authorize(getView().getCopyDataAuthorizer()).send();
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
      // Drop table
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink()).delete()
          .authorize(getView().getRemoveAuthorizer()).send();
    }

    // values
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/valueSets").get()
        .authorize(getView().getValuesAuthorizer()).send();
  }

  private void updateDisplay(String datasourceName, String tableName, final String previous, final String next) {
    if(table != null && table.getDatasourceName().equals(datasourceName) && table.getName().equals(tableName)) return;
    if(tableUpdatePending) return;

    tableUpdatePending = true;
    UriBuilder ub = UriBuilder.URI_DATASOURCE_TABLE.query("counts", "true");
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build(datasourceName, tableName)).get()
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            if(resource != null) {
              updateDisplay(resource, previous, next);
            }
            tableUpdatePending = false;
          }
        }).send();
  }

  private void updateDisplay(TableDto tableDto, String previous, String next) {
    if(table == null || !table.getLink().equals(tableDto.getLink())) {
      getView().clear(true);

      table = tableDto;
      this.previous = previous;
      this.next = next;

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
            //.withLimit(table.getVariableCount())//
        .withSortDir(
            sortAscending == null || sortAscending ? VariablesFilter.SORT_ASCENDING : VariablesFilter.SORT_DESCENDING)//
        .withSortField(getView().getClickableColumnName(sortColumn) == null
            ? "index"
            : getView().getClickableColumnName(sortColumn))//
        .filter(getEventBus(), table);

  }

  private VariableDto getPreviousVariable(int index) {
    VariableDto previous = null;
    if(index > 0) {
      previous = variables.get(index - 1);
    }
    return previous;
  }

  private VariableDto getNextVariable(int index) {
    VariableDto next = null;
    if(index < variables.length() - 1) {
      next = variables.get(index + 1);
    }
    return next;
  }

  private boolean tableIsView() {
    return table.hasViewLink();
  }

  @Override
  public void onNextTable() {
    getEventBus().fireEvent(new SiblingTableSelectionEvent(table, Direction.NEXT));
  }

  @Override
  public void onPreviousTable() {
    getEventBus().fireEvent(new SiblingTableSelectionEvent(table, Direction.PREVIOUS));
  }

  @Override
  public void onExportData() {
    getEventBus().fireEvent(new WizardRequiredEvent(DataExportPresenter.WizardType, table));
  }

  @Override
  public void onCopyData() {
    getEventBus().fireEvent(new WizardRequiredEvent(DataCopyPresenter.WizardType, table));
  }

  @Override
  public void onDownloadDictionary() {
    String downloadUrl = table.getLink() + "/variables/excel";
    getEventBus().fireEvent(new FileDownloadEvent(downloadUrl));
  }

  @Override
  public void onDownloadView() {
    String downloadUrl = table.getViewLink() + "/xml";
    getEventBus().fireEvent(new FileDownloadEvent(downloadUrl));
  }

  @Override
  public void onAddVariablesToView(List<VariableDto> variables) {
    if(variables.isEmpty()) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("CopyVariableSelectAtLeastOne").build());
    } else {
      VariablesToViewPresenter variablesToViewPresenter = variablesToViewProvider.get();
      variablesToViewPresenter.initialize(table, variables);
    }
  }

  @Override
  public void onEdit() {
    UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName(), "view", table.getName());
    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<ViewDto>() {

          @Override
          public void onResource(Response response, ViewDto viewDto) {
            viewDto.setDatasourceName(table.getDatasourceName());
            viewDto.setName(table.getName());
            // TODO: this popup is going to die soon and won't need a ModalProvider
            configureViewStepProvider.get();
            getEventBus().fireEvent(new ViewConfigurationRequiredEvent(viewDto));

          }
        }).send();
  }

  @Override
  public void onRemove() {
    removeConfirmation = new RemoveRunnable();

    ConfirmationRequiredEvent event;
    event = tableIsView()
        ? ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeView", "confirmRemoveView")
        : ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeTable", "confirmRemoveTable");

    getEventBus().fireEvent(event);
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
          getEventBus().fireEvent(
              NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                  .build());
        }
      }

    };
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
        .forResource(UriBuilder.URI_DATASOURCE_TABLE_INDEX.build(table.getDatasourceName(), table.getName()))//
        .withCallback(callback, SC_OK, SC_SERVICE_UNAVAILABLE).delete().send();
  }

  @Override
  public void onIndexNow() {
    ResponseCodeCallback callback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == SC_OK) {
          // Wait a few seconds for the task to launch before checking its status
          Timer t = new Timer() {
            @Override
            public void run() {
              updateIndexStatus();
            }
          };
          // Schedule the timer to run once in X seconds.
          t.schedule(DELAY_MILLIS);
        } else {
          getEventBus().fireEvent(NotificationEvent.Builder.newNotification().error(response.getText()).build());
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
          getEventBus().fireEvent(
              NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                  .build());
        }
      }

    };
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
        .forResource(UriBuilder.URI_DATASOURCE_TABLE_INDEX.build(table.getDatasourceName(), table.getName()))//
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

  @Override
  public void onFromTable(String tableFullName) {
    String[] s = tableFullName.split("\\.");
    getEventBus().fireEvent(new TableSelectionChangeEvent(this, s[0], s[1]));
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
        String sortColumnName = getView().getClickableColumnName(sortColumn);
        doFilterVariables(sortColumnName);
      }
    }
  }

  private final class VariableSortHandler implements ColumnSortEvent.Handler {

    @Override
    public void onColumnSort(ColumnSortEvent event) {
      sortAscending = event.isSortAscending();
      sortColumn = event.getColumn();
      updateDisplay(table, previous, next);
    }
  }

  private final class FilterVariableHandler implements KeyUpHandler {

    @Override
    public void onKeyUp(KeyUpEvent event) {
      String sortColumnName = getView().getClickableColumnName(sortColumn);
      String filter = getView().getFilter().getText();

      if(filter.isEmpty()) {
        updateVariables();
      } else if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
        doFilterVariables(sortColumnName);
      }
    }
  }

  private void doFilterVariables(final String sortColumnName) {
    final String query = getView().getFilter().getText();

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
            //.withLimit(table.getVariableCount())//
        .withSortDir(
            sortAscending == null || sortAscending ? VariablesFilter.SORT_ASCENDING : VariablesFilter.SORT_DESCENDING)//
        .withSortField(sortColumnName == null ? "index" : sortColumnName)//
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

          Timer t = new Timer() {
            @Override
            public void run() {
              updateIndexStatus();
            }
          };

          // Schedule the timer to run once in X seconds.
          t.schedule(DELAY_MILLIS);
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

  private class VariableNameFieldUpdater implements FieldUpdater<VariableDto, String> {
    @Override
    public void update(int index, VariableDto variableDto, String value) {
      getEventBus().fireEvent(new VariableSelectionChangeEvent(TablePresenter.this, table, variableDto, getPreviousVariable(index),
          getNextVariable(index)));
    }
  }

  private class SiblingVariableSelectionHandler implements SiblingVariableSelectionEvent.Handler {
    @Override
    public void onSiblingVariableSelection(SiblingVariableSelectionEvent event) {

      // Look for the variable and its position in the list by its name.
      // Having a position of the current variable would be more efficient.
      int siblingIndex = 0;
      for(int i = 0; i < variables.length(); i++) {
        if(variables.get(i).getName().equals(event.getCurrentSelection().getName())) {
          if(event.getDirection() == SiblingVariableSelectionEvent.Direction.NEXT && i < variables.length() - 1) {
            siblingIndex = i + 1;
          } else siblingIndex = event.getDirection() == SiblingVariableSelectionEvent.Direction.PREVIOUS && i != 0
              ? i - 1
              : i;
          break;
        }
      }
      VariableDto variableDto = variables.get(siblingIndex);

      getView().setVariableSelection(variableDto, siblingIndex);
      getEventBus().fireEvent(
          new VariableSelectionChangeEvent(TablePresenter.this, table, variableDto, getPreviousVariable(siblingIndex),
              getNextVariable(siblingIndex)));
    }
  }

  // OPAL-975
  private class ViewSavedEventHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      if(table != null) {
        updateDisplay(table, previous, next);
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

    void setVariableSelection(VariableDto variable, int index);

    void beforeRenderRows();

    void renderRows(JsArray<VariableDto> rows);

    void afterRenderRows();

    void clear(boolean cleanFilter);

    void setTable(TableDto dto);

    void setVariableNameFieldUpdater(FieldUpdater<VariableDto, String> updater);

    void setVariableIndexFieldUpdater(FieldUpdater<VariableDto, String> updater);

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

    String getClickableColumnName(Column<?, ?> column);

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

    private void removeView() {

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            getEventBus().fireEvent(new DatasourceUpdatedEvent(table.getDatasourceName()));
            getEventBus().fireEvent(new DatasourceSelectionChangeEvent(TablePresenter.this, table.getDatasourceName()));
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
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
            getEventBus().fireEvent(new DatasourceUpdatedEvent(table.getDatasourceName()));
            getEventBus().fireEvent(new DatasourceSelectionChangeEvent(TablePresenter.this, table.getDatasourceName()));
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      ResourceRequestBuilderFactory.newBuilder().forResource(table.getLink()).delete()
          .withCallback(SC_OK, callbackHandler).withCallback(SC_FORBIDDEN, callbackHandler)
          .withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

  }
}
