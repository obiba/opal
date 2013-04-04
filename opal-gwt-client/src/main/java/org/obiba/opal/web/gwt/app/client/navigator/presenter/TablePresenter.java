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
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.search.VariableItemDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
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

  private static final String SORT_DESCENDING = "DESC";

  private static final String SORT_ASCENDING = "ASC";

  private JsArray<VariableDto> variables;

  private TableDto table;

  private TableDto originalTable;

  private TableIndexStatusDto statusDto;

  private String previous;

  private String next;

  private boolean cancelIndexation = false;

  private Provider<AuthorizationPresenter> authorizationPresenter;

  @Inject
  private CodingViewDialogPresenter codingViewDialogPresenter;

  private ValuesTablePresenter valuesTablePresenter;

  private Provider<IndexPresenter> indexPresenter;

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
      updateDisplay(e.getSelection(), e.getPrevious(), e.getNext());

      if(originalTable == null) originalTable = e.getSelection();
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Values, valuesTablePresenter);

    addEventHandlers();
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
    registerHandler(getView().addVariableSuggestionHandler(new VariableSuggestionHandler()));
    registerHandler(getView().addVariableSortHandler(new VariableSortHandler()));

    // Filter variable event
    registerHandler(getView().addFilterVariableHandler(new FilterVariableHandler()));

    // OPAL-975
    registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedEventHandler()));

    registerHandler(
        getEventBus().addHandler(TableIndexStatusRefreshEvent.getType(), new TableIndexStatusRefreshHandler()));

    //Link actions: CLEAR
    final UriBuilder ub = UriBuilder.create().segment("datasource", "{}", "table", "{}", "index");
    getView().getClear().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showWaitCursor();

        ResponseCodeCallback callback = new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == SC_OK) {
              updateIndexStatus();
            } else {
              showDefaultCursor();
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
        showWaitCursor();

        ResponseCodeCallback callback = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == SC_OK) {
              cancelIndexation = true;
              updateIndexStatus();
            } else {
              showDefaultCursor();
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
        showWaitCursor();

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
              showDefaultCursor();
              ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
              getEventBus().fireEvent(
                  NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                      .build());
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

  private void showWaitCursor() {RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.WAIT);}

  private void showDefaultCursor() {RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.DEFAULT);}

  private String getIndexResource(String datasource, String table) {
    return UriBuilder.create().segment("datasource", "{}", "table", "{}", "index").build(datasource, table);
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    authorize();
  }

  private void authorize() {
    UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName());
    // export variables in excel
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/variables/excel").get()
        .authorize(getView().getExcelDownloadAuthorizer()).send();
    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/commands/_copy").post()//
        .authorize(CascadingAuthorizer.newBuilder()//
            .and("/functional-units", HttpMethod.GET)//
            .and("/functional-units/entities/table", HttpMethod.GET)//
            .authorize(getView().getExportDataAuthorizer()).build())//
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

  private void updateDisplay(TableDto tableDto, String previous, String next) {
    table = tableDto;
    this.previous = previous;
    this.next = next;

    getView().clear();
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
    ResourceRequestBuilderFactory.<JsArray<ViewDto>>newBuilder().forResource(tableDto.getViewLink()).get()
        .withCallback(new ViewResourceCallback()).send();
  }

  private void updateTableIndexStatus() {// Check if service is enabled
    // Table indexation status
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/index").get()
        .authorize(getView().getTableIndexStatusAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/index").delete()
        .authorize(getView().getTableIndexEditAuthorizer()).send();

    updateIndexStatus();
  }

  private void updateIndexStatus() {
    // If cancelation, call the delete ws
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
    String sortColumnName = getView().getClickableColumnName(sortColumn);

    UriBuilder ub = UriBuilder.create()//
        .segment("datasource", table.getDatasourceName(), "table", table.getName(), "variables");

    if(sortColumnName != null) {
      ub.query("sortField", sortColumnName);
    }
    ub.query("sortDir", sortAscending == null || sortAscending ? SORT_ASCENDING : SORT_DESCENDING);

    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(ub.build()).get()
        .withCallback(new VariablesResourceCallback(table)).send();
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

  private final class VariableSuggestionHandler implements SelectionHandler<Suggestion> {

    @Override
    public void onSelection(SelectionEvent<Suggestion> evt) {
      String value = evt.getSelectedItem().getReplacementString();
      // look for the variable and fire selection
      for(int i = 0; i < variables.length(); i++) {
        if(variables.get(i).getName().equals(value)) {
          VariableDto selection = variables.get(i);
          getEventBus().fireEvent(
              new VariableSelectionChangeEvent(table, selection, getPreviousVariable(i), getNextVariable(i)));
          getView().setVariableSelection(selection, i);
          getView().clearVariableSuggestion();
          break;
        }
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

        String query = getView().getFilter().getText();

        UriBuilder ub = UriBuilder.create()
            .segment("datasource", table.getDatasourceName(), "table", table.getName(), "variables", "_search")
            .query("query", query)//
            .query("limit", String.valueOf(table.getVariableCount()))//
            .query("variable", "true");

        // Keep sort info
        if(sortColumnName != null) {
          ub.query("sortField", sortColumnName);
        }
        ub.query("sortDir", sortAscending == null || sortAscending ? SORT_ASCENDING : SORT_DESCENDING);

        ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<QueryResultDto>() {
              @Override
              public void onResource(Response response, QueryResultDto resource) {
                if(response.getStatusCode() == Response.SC_OK) {
                  QueryResultDto resultDto = (QueryResultDto) JsonUtils.unsafeEval(response.getText());

                  JsArray<VariableDto> variables = JsArrays.create();
                  for(int i = 0; i < resultDto.getHitsArray().length(); i++) {
                    VariableItemDto varDto = (VariableItemDto) resultDto.getHitsArray().get(i)
                        .getExtension(VariableItemDto.ItemResultDtoExtensions.item);

                    variables.push(varDto.getVariable());
                  }
                  getView().renderRows(variables);
                }
              }
            })//
            .withCallback(Response.SC_SERVICE_UNAVAILABLE, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                getEventBus().fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
              }
            }).send();
      }
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
      UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName());
      ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<DatasourceDto>() {
            @Override
            public void onResource(Response response, DatasourceDto resource) {
              getEventBus().fireEvent(new DatasourceSelectionChangeEvent(resource));
            }

          }).send();
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
      showDefaultCursor();
    }
  }

  private class TableIndexStatusResourceCallback implements ResourceCallback<JsArray<TableIndexStatusDto>> {

    @Override
    public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {

      if(response.getStatusCode() == SC_OK) {
        getView().setIndexStatusVisible(true);
        getView().setIndexStatusVisible(true);
        statusDto = TableIndexStatusDto.get(JsArrays.toSafeArray(resource));
        getView().setIndexStatusAlert(statusDto);

        // Refetch if in progress
        if(statusDto.getStatus().getName().equals(TableIndexationStatus.IN_PROGRESS.getName())) {

          Timer t = new Timer() {
            @Override
            public void run() {
              updateIndexStatus();
            }
          };

          // Schedule the timer to run once in 2 seconds.
          t.schedule(DELAY_MILLIS);
        } else {
          showDefaultCursor();
        }
      } else {
        showDefaultCursor();
      }
    }
  }

  class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private final TableDto table;

    VariablesResourceCallback(TableDto table) {
      this.table = table;
      getView().beforeRenderRows();
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      if(table.getLink().equals(TablePresenter.this.table.getLink())) {
        variables = JsArrays.toSafeArray(resource);
        getView().renderRows(variables);
        for(int i = 0; i < variables.length(); i++) {
          getView().addVariableSuggestion(variables.get(i).getName());
        }
        getView().afterRenderRows();
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
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void updateFromTableLink(Anchor tableLink) {
    String[] s = tableLink.getText().split("\\.");
    UriBuilder ub = UriBuilder.create().segment("datasource", "{}", "table", "{}");
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build(s[0], s[1])).get()
        .withCallback(new TableResourceCallback(tableLink)).withCallback(SC_NOT_FOUND, new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        // Nothing table does not exists
      }
    }).send();
  }

  class TableResourceCallback implements ResourceCallback<TableDto> {

    private final Anchor link;

    TableResourceCallback(Anchor link) {
      this.link = link;
    }

    @Override
    public void onResource(Response response, final TableDto resource) {
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          getEventBus().fireEvent(new TableSelectionChangeEvent(TablePresenter.this, resource));
        }
      });
    }
  }

  private class TableSelectionChangeHandler implements TableSelectionChangeEvent.Handler {
    @Override
    public void onTableSelectionChanged(TableSelectionChangeEvent event) {
      updateDisplay(event.getSelection(), event.getPrevious(), event.getNext());
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
      updateTableIndexStatus();
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

    void clear();

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

    void addVariableSuggestion(String suggestion);

    HandlerRegistration addVariableSuggestionHandler(SelectionHandler<Suggestion> handler);

    HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler);

    void clearVariableSuggestion();

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

    HasText getFilter();
  }

}
