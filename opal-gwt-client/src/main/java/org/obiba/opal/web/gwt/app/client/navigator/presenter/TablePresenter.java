/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.CopyVariablesToViewEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableIndexStatusRefreshEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.navigator.util.VariablesFilter;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.TextBoxClearable;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;
import static com.google.gwt.http.client.Response.SC_SERVICE_UNAVAILABLE;

@SuppressWarnings("OverlyCoupledClass")
public class TablePresenter extends Presenter<TablePresenter.Display, TablePresenter.Proxy> {

  private static final int DELAY_MILLIS = 1000;

  private JsArray<VariableDto> variables;

  private TableDto table;

  private TableIndexStatusDto statusDto;

  private String previous;

  private String next;

  private boolean cancelIndexation = false;

  private final Provider<AuthorizationPresenter> authorizationPresenter;

  @Inject
  private CodingViewDialogPresenter codingViewDialogPresenter;

  private final ValuesTablePresenter valuesTablePresenter;

  private final Provider<IndexPresenter> indexPresenter;

  private Runnable removeConfirmation;

  private Boolean sortAscending;

  private Column<?, ?> sortColumn;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public TablePresenter(Display display, EventBus eventBus, Proxy proxy, ValuesTablePresenter valuesTablePresenter,
      Provider<AuthorizationPresenter> authorizationPresenter, Provider<IndexPresenter> indexPresenter) {
    super(eventBus, display, proxy);
    this.valuesTablePresenter = valuesTablePresenter;
    this.authorizationPresenter = authorizationPresenter;
    this.indexPresenter = indexPresenter;
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, NavigatorPresenter.CENTER_PANE, this);
  }

  // This makes this presenter reveal itself whenever a TableSelectionChangeEvent occurs (anywhere for any reason).
  @SuppressWarnings("UnusedDeclaration")
  @ProxyEvent
  public void onTableSelectionChanged(TableSelectionChangeEvent e) {
    if(!isVisible()) {
      forceReveal();
      updateDisplay(e.getDatasourceName(), e.getTableName(), e.getPrevious(), e.getNext());
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Values, valuesTablePresenter);

    addEventHandlers();
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
    registerHandler(getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeHandler()));
    registerHandler(
        getEventBus().addHandler(SiblingVariableSelectionEvent.getType(), new SiblingVariableSelectionHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));
    getView().setAddVariablesToViewCommand(new AddVariablesToViewCommand());
    getView().setExcelDownloadCommand(new ExcelDownloadCommand());
    getView().setExportDataCommand(new ExportDataCommand());
    getView().setCopyDataCommand(new CopyDataCommand());
    getView().setParentCommand(new ParentCommand());
    getView().setPreviousCommand(new PreviousCommand());
    getView().setNextCommand(new NextCommand());
    getView().setValuesTabCommand(new ValuesCommand());
    getView().setVariablesTabCommand(new VariablesCommand());

    // Copy variables handler
    getView().getCopyVariables().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new CopyVariablesToViewEvent(table, getView().getSelectedItems()));
      }

    });

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

    //Link actions: CLEAR
    final UriBuilder ub = UriBuilder.create().segment("datasource", "{}", "table", "{}", "index");
    getView().getClear().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
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
            .forResource(ub.build(table.getDatasourceName(), table.getName()))//
            .withCallback(callback, SC_OK, SC_SERVICE_UNAVAILABLE).delete().send();
      }
    });
    getView().getCancel().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
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
            .forResource(ub.build(table.getDatasourceName(), table.getName()))//
            .withCallback(callback, SC_OK, SC_SERVICE_UNAVAILABLE).delete().send();
      }
    });

    //Link actions: INDEX NOW
    getView().getIndexNow().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
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
    });

    // Link action: Schedule indexing
    getView().getScheduleIndexing().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<TableIndexStatusDto> objects = new ArrayList<TableIndexStatusDto>();
        objects.add(statusDto);

        IndexPresenter dialog = indexPresenter.get();
        dialog.setUpdateMethodCallbackRefreshIndices(false);
        dialog.setUpdateMethodCallbackRefreshTable(true);
        dialog.updateSchedules(objects);
        addToPopupSlot(dialog);

      }
    });

  }

  private String getIndexResource(String datasource, String table) {
    return UriBuilder.create().segment("datasource", "{}", "table", "{}", "index").build(datasource, table);
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    authorize();
  }

  private void authorize() {
    if (table == null) return;

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

    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  private void updateDisplay(String datasourceName, String tableName, final String previous, final String next) {
    if(table != null && table.getDatasourceName().equals(datasourceName) && table.getName().equals(tableName)) {
      updateDisplay(table, previous, next);
    } else {
      UriBuilder ub = UriBuilder.create().segment("datasource", "{}", "table", "{}").query("counts","true");
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build(datasourceName, tableName)).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto resource) {
              updateDisplay(resource, previous, next);
            }
          }).send();
    }
  }

  private void updateDisplay(TableDto tableDto, String previous, String next) {
    getView().clear(table == null || !table.getLink().equals(tableDto.getLink()));

    table = tableDto;
    this.previous = previous;
    this.next = next;

    getView().setTable(tableDto);
    getView().setParentName(tableDto.getDatasourceName());
    getView().setPreviousName(previous);
    getView().setNextName(next);
    getView().setRemoveCommand(new RemoveCommand());

    if(getView().isValuesTabSelected()) {
      valuesTablePresenter.setTable(tableDto);
    }

    if(tableIsView()) {
      getView().setViewDownloadCommand(new DownloadViewCommand());
      getView().setEditCommand(new EditCommand());
      showFromTables(tableDto);

    } else {
      getView().setViewDownloadCommand(null);
      getView().setEditCommand(null);
      getView().setFromTables(null);
    }

    updateVariables();
    updateTableIndexStatus();
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

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void downloadMetadata() {
    String downloadUrl = table.getLink() + "/variables/excel";
    getEventBus().fireEvent(new FileDownloadEvent(downloadUrl));
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

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void removeView() {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == SC_OK) {
          getEventBus().fireEvent(new DatasourceUpdatedEvent(table.getDatasourceName()));
          getEventBus().fireEvent(new DatasourceSelectionChangeEvent(table.getDatasourceName()));
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

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void removeTable() {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == SC_OK) {
          getEventBus().fireEvent(new DatasourceUpdatedEvent(table.getDatasourceName()));
          getEventBus().fireEvent(new DatasourceSelectionChangeEvent(table.getDatasourceName()));
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

  private boolean tableIsView() {
    return table.hasViewLink();
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
        authz.setAclRequest("view", new AclRequest(AclAction.VIEW_READ, node), //
            new AclRequest(AclAction.VIEW_VALUES, node), //
            new AclRequest(AclAction.VIEW_EDIT, node), //
            new AclRequest(AclAction.VIEW_VALUES_EDIT, node), //
            new AclRequest(AclAction.VIEW_ALL, node));
      } else {
        String node = nodeBuilder.segment("table", table.getName()).build();
        authz.setAclRequest("table", new AclRequest(AclAction.TABLE_READ, node), //
            new AclRequest(AclAction.TABLE_VALUES, node), //
            new AclRequest(AclAction.TABLE_EDIT, node),//
            new AclRequest(AclAction.TABLE_ALL, node));
      }
      setInSlot(Display.Slots.Permissions, authz);
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

  private final class NextCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new SiblingTableSelectionEvent(table, Direction.NEXT));
    }
  }

  private final class PreviousCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new SiblingTableSelectionEvent(table, Direction.PREVIOUS));
    }
  }

  private final class ParentCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new DatasourceSelectionChangeEvent(table.getDatasourceName()));
    }
  }

  private final class ExcelDownloadCommand implements Command {
    @Override
    public void execute() {
      downloadMetadata();
    }
  }

  private final class ExportDataCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new WizardRequiredEvent(DataExportPresenter.WizardType, table));
    }
  }

  private final class CopyDataCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new WizardRequiredEvent(DataCopyPresenter.WizardType, table));
    }
  }

  private final class DownloadViewCommand implements Command {
    @Override
    public void execute() {
      String downloadUrl = table.getViewLink() + "/xml";
      getEventBus().fireEvent(new FileDownloadEvent(downloadUrl));
    }
  }

  private final class RemoveCommand implements Command {
    @Override
    public void execute() {
      removeConfirmation = new Runnable() {
        @Override
        public void run() {
          if(tableIsView()) {
            removeView();
          } else {
            removeTable();
          }
        }
      };

      ConfirmationRequiredEvent event;
      event = tableIsView()
          ? ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeView", "confirmRemoveView")
          : ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeTable", "confirmRemoveTable");

      getEventBus().fireEvent(event);
    }
  }

  private final class AddVariablesToViewCommand implements Command {

    @Override
    public void execute() {
      if(getView().getSelectedItems().isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("CopyVariableSelectAtLeastOne").build());
      } else {
        getEventBus().fireEvent(new CopyVariablesToViewEvent(table, getView().getSelectedItems()));
      }
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

  private final class EditCommand implements Command {
    @Override
    public void execute() {
      UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName(), "view", table.getName());
      ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<ViewDto>() {

            @Override
            public void onResource(Response response, ViewDto viewDto) {
              viewDto.setDatasourceName(table.getDatasourceName());
              viewDto.setName(table.getName());

              getEventBus().fireEvent(new ViewConfigurationRequiredEvent(viewDto));
            }
          }).send();
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

      // Add click handlers
      for(Anchor tableLink : getView().getFromTablesAnchor()) {
        updateFromTableLink(tableLink);
      }
    }

    private void updateFromTableLink(Anchor tableLink) {
      final String[] s = tableLink.getText().split("\\.");

      tableLink.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          getEventBus().fireEvent(new TableSelectionChangeEvent(TablePresenter.this, s[0], s[1]));
        }
      });
    }
  }

  private class TableSelectionChangeHandler implements TableSelectionChangeEvent.Handler {
    @Override
    public void onTableSelectionChanged(TableSelectionChangeEvent event) {
      updateDisplay(event.getDatasourceName(), event.getTableName(), event.getPrevious(), event.getNext());
      authorize();
    }
  }

  private class VariableNameFieldUpdater implements FieldUpdater<VariableDto, String> {
    @Override
    public void update(int index, VariableDto variableDto, String value) {
      getEventBus().fireEvent(
          new VariableSelectionChangeEvent(table, variableDto, getPreviousVariable(index), getNextVariable(index)));
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
      getEventBus().fireEvent(new VariableSelectionChangeEvent(table, variableDto, getPreviousVariable(siblingIndex),
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

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<TablePresenter> {}

  public interface Display extends View {

    enum Slots {
      Permissions, Values
    }

    void setVariableSelection(VariableDto variable, int index);

    void beforeRenderRows();

    void renderRows(JsArray<VariableDto> rows);

    void afterRenderRows();

    void clear(boolean cleanFilter);

    void setTable(TableDto dto);

    void setExcelDownloadCommand(Command cmd);

    void setExportDataCommand(Command cmd);

    void setViewDownloadCommand(Command cmd);

    void setParentCommand(Command cmd);

    void setNextCommand(Command cmd);

    void setPreviousCommand(Command cmd);

    void setRemoveCommand(Command cmd);

    void setEditCommand(Command cmd);

    void setAddVariablesToViewCommand(Command cmd);

    void setParentName(String name);

    void setPreviousName(String name);

    void setNextName(String name);

    void setVariableNameFieldUpdater(FieldUpdater<VariableDto, String> updater);

    void setVariableIndexFieldUpdater(FieldUpdater<VariableDto, String> updater);

    void setCopyDataCommand(Command cmd);

    HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler);

    HasAuthorization getCopyDataAuthorizer();

    HasAuthorization getExcelDownloadAuthorizer();

    HasAuthorization getViewDownloadAuthorizer();

    HasAuthorization getExportDataAuthorizer();

    HasAuthorization getRemoveAuthorizer();

    HasAuthorization getEditAuthorizer();

    HasAuthorization getValuesAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    HasAuthorization getTableIndexStatusAuthorizer();

    HasAuthorization getTableIndexEditAuthorizer();

    String getClickableColumnName(Column<?, ?> column);

    void setValuesTabCommand(Command cmd);

    void setVariablesTabCommand(Command cmd);

    boolean isValuesTabSelected();

    void setIndexStatusVisible(boolean b);

    void setIndexStatusAlert(TableIndexStatusDto statusDto);

    HasClickHandlers getClear();

    HasClickHandlers getCancel();

    HasClickHandlers getIndexNow();

    HasClickHandlers getScheduleIndexing();

    void setFromTables(JsArrayString tables);

    List<Anchor> getFromTablesAnchor();

    HasClickHandlers getCopyVariables();

    List<VariableDto> getSelectedItems();

    HandlerRegistration addFilterVariableHandler(KeyUpHandler handler);

    TextBoxClearable getFilter();

    void setCancelVisible(boolean b);
  }

}
