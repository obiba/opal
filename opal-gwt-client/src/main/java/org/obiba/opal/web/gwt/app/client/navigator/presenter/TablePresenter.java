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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingVariableSelectionEvent;
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

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class TablePresenter extends Presenter<TablePresenter.Display, TablePresenter.Proxy> {

  private JsArray<VariableDto> variables;

  private TableDto table;

  private String previous;

  private String next;

  private Provider<AuthorizationPresenter> authorizationPresenter;

  @Inject
  private CodingViewDialogPresenter codingViewDialogPresenter;

  private ValuesTablePresenter valuesTablePresenter;

  private Runnable removeConfirmation;

  private Boolean sortAscending;

  private Column<?, ?> sortColumn;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public TablePresenter(final Display display, final EventBus eventBus, Proxy proxy, ValuesTablePresenter valuesTablePresenter, Provider<AuthorizationPresenter> authorizationPresenter) {
    super(eventBus, display, proxy);
    this.valuesTablePresenter = valuesTablePresenter;
    this.authorizationPresenter = authorizationPresenter;
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, NavigatorPresenter.CENTER_PANE, this);
  }

  // This makes this presenter reveal itself whenever a TableSelectionChangeEvent occurs (anywhere for any reason).
  @ProxyEvent
  public void onTableSelectionChanged(TableSelectionChangeEvent e) {
    if(isVisible() == false) {
      forceReveal();
      updateDisplay(e.getSelection(), e.getPrevious(), e.getNext());
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Values, valuesTablePresenter);

    addEventHandlers();
  }

  private void addEventHandlers() {
    super.registerHandler(getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeHandler()));
    super.registerHandler(getEventBus().addHandler(SiblingVariableSelectionEvent.getType(), new SiblingVariableSelectionHandler()));
    super.registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));
    getView().setCreateCodingViewCommand(new CreateCodingViewCommand());
    getView().setExcelDownloadCommand(new ExcelDownloadCommand());
    getView().setExportDataCommand(new ExportDataCommand());
    getView().setCopyDataCommand(new CopyDataCommand());
    getView().setParentCommand(new ParentCommand());
    getView().setPreviousCommand(new PreviousCommand());
    getView().setNextCommand(new NextCommand());
    getView().setValuesTabCommand(new ValuesCommand());

    VariableNameFieldUpdater updater = new VariableNameFieldUpdater();
    super.getView().setVariableNameFieldUpdater(updater);
    super.getView().setVariableIndexFieldUpdater(updater);
    super.registerHandler(getView().addVariableSuggestionHandler(new VariableSuggestionHandler()));
    super.registerHandler(getView().addVariableSortHandler(new VariableSortHandler()));

    // OPAL-975
    super.registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedEventHandler()));
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    authorize();
  }

  private void authorize() {
    // export variables in excel
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/variables/excel").get().authorize(getView().getExcelDownloadAuthorizer()).send();
    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/copy").post()//
    .authorize(CascadingAuthorizer.newBuilder().and("/files/meta", HttpMethod.GET)//
    .and("/functional-units", HttpMethod.GET)//
    .and("/functional-units/entities/table", HttpMethod.GET)//
    .authorize(getView().getExportDataAuthorizer()).build())//
    .send();
    // copy data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/copy").post().authorize(getView().getCopyDataAuthorizer()).send();
    if(table.hasViewLink()) {
      // download view
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getViewLink() + "/xml").get().authorize(getView().getViewDownloadAuthorizer()).send();
      // remove view
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).delete().authorize(getView().getRemoveAuthorizer()).send();
      // edit view
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).put().authorize(getView().getEditAuthorizer()).send();
    } else {
      // Drop table
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink()).delete().authorize(getView().getRemoveAuthorizer()).send();
    }

    // values
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getLink() + "/valueSets").get().authorize(getView().getValuesAuthorizer()).send();

    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder().authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  private void updateDisplay(TableDto tableDto, String previous, String next) {
    this.table = tableDto;
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
    } else {
      getView().setViewDownloadCommand(null);
      getView().setEditCommand(null);
    }

    updateVariables();
  }

  private void updateVariables() {
    String sortColumnName = getView().getClickableColumnName(sortColumn);
    String sortColumArg = (sortColumnName != null ? ("?sortField=" + sortColumnName) : "");
    String sortDirArg = (sortAscending != null ? (sortAscending ? "&sortDir=ASC" : "&sortDir=DESC") : "");
    // TODO use uribuilder
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(table.getLink() + "/variables" + sortColumArg + sortDirArg).get().withCallback(new VariablesResourceCallback(table)).send();
  }

  private void downloadMetadata() {
    String downloadUrl = new StringBuilder(this.table.getLink()).append("/variables/excel").toString();
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

  private void removeView(String viewName) {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() != Response.SC_OK) {
          String errorMessage = response.getText().length() != 0 ? response.getText() : "UnknownError";
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
        } else {
          getEventBus().fireEvent(new DatasourceUpdatedEvent(table.getDatasourceName()));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void removeTable(String viewName) {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() != Response.SC_OK) {
          String errorMessage = response.getText().length() != 0 ? response.getText() : "UnknownError";
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
        } else {
          getEventBus().fireEvent(new DatasourceUpdatedEvent(table.getDatasourceName()));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource(table.getLink()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private boolean tableIsView() {
    return table.hasViewLink();
  }

  final class ValuesCommand implements Command {

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
        authz.setAclRequest("view", new AclRequest(AclAction.VIEW_ALL, node), //
        new AclRequest(AclAction.VIEW_READ, node), //
        new AclRequest(AclAction.VIEW_VALUES, node), //
        new AclRequest(AclAction.VIEW_EDIT, node), //
        new AclRequest(AclAction.VIEW_VALUES_EDIT, node));
      } else {
        String node = nodeBuilder.segment("table", table.getName()).build();
        authz.setAclRequest("table", new AclRequest(AclAction.TABLE_ALL, node), //
        new AclRequest(AclAction.TABLE_READ, node), //
        new AclRequest(AclAction.TABLE_VALUES, node), //
        new AclRequest(AclAction.TABLE_EDIT, node));
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
          getEventBus().fireEvent(new VariableSelectionChangeEvent(table, selection, getPreviousVariable(i), getNextVariable(i)));
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

  final class NextCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new SiblingTableSelectionEvent(table, Direction.NEXT));
    }
  }

  final class PreviousCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new SiblingTableSelectionEvent(table, Direction.PREVIOUS));
    }
  }

  final class ParentCommand implements Command {
    @Override
    public void execute() {
      UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName());
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource(ub.build()).get().withCallback(new ResourceCallback<DatasourceDto>() {
        @Override
        public void onResource(Response response, DatasourceDto resource) {
          getEventBus().fireEvent(new DatasourceSelectionChangeEvent(resource));
        }

      }).send();
    }
  }

  final class ExcelDownloadCommand implements Command {
    @Override
    public void execute() {
      downloadMetadata();
    }
  }

  final class ExportDataCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new WizardRequiredEvent(DataExportPresenter.WizardType, table));
    }
  }

  final class CopyDataCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new WizardRequiredEvent(DataCopyPresenter.WizardType, table));
    }
  }

  final class DownloadViewCommand implements Command {
    @Override
    public void execute() {
      String downloadUrl = new StringBuilder(table.getViewLink()).append("/xml").toString();
      getEventBus().fireEvent(new FileDownloadEvent(downloadUrl));
    }
  }

  final class RemoveCommand implements Command {
    @Override
    public void execute() {
      removeConfirmation = new Runnable() {
        public void run() {
          if(tableIsView()) {
            removeView(table.getName());
          } else {
            removeTable(table.getName());
          }
        }
      };

      ConfirmationRequiredEvent event;
      if(tableIsView()) {
        event = new ConfirmationRequiredEvent(removeConfirmation, "removeView", "confirmRemoveView");
      } else {
        event = new ConfirmationRequiredEvent(removeConfirmation, "removeTable", "confirmRemoveTable");
      }

      getEventBus().fireEvent(event);
    }
  }

  final class CreateCodingViewCommand implements Command {

    @Override
    public void execute() {
      codingViewDialogPresenter.bind();
      codingViewDialogPresenter.setTableVariables(table, variables);
      codingViewDialogPresenter.revealDisplay();
    }
  }

  class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

  final class EditCommand implements Command {
    @Override
    public void execute() {
      UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName(), "view", table.getName());
      ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource(ub.build()).get().withCallback(new ResourceCallback<ViewDto>() {

        @Override
        public void onResource(Response response, ViewDto viewDto) {
          viewDto.setDatasourceName(table.getDatasourceName());
          viewDto.setName(table.getName());

          getEventBus().fireEvent(new ViewConfigurationRequiredEvent(viewDto));
        }
      }).send();
    }
  }

  class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private TableDto table;

    public VariablesResourceCallback(TableDto table) {
      super();
      this.table = table;
      getView().beforeRenderRows();
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      if(this.table.getLink().equals(TablePresenter.this.table.getLink())) {
        variables = JsArrays.toSafeArray(resource);
        getView().renderRows(variables);
        for(int i = 0; i < variables.length(); i++) {
          getView().addVariableSuggestion(variables.get(i).getName());
        }
        getView().afterRenderRows();
      }
    }
  }

  class TableSelectionChangeHandler implements TableSelectionChangeEvent.Handler {
    @Override
    public void onTableSelectionChanged(TableSelectionChangeEvent event) {
      updateDisplay(event.getSelection(), event.getPrevious(), event.getNext());
      authorize();
    }
  }

  class VariableNameFieldUpdater implements FieldUpdater<VariableDto, String> {
    @Override
    public void update(int index, VariableDto variableDto, String value) {
      getEventBus().fireEvent(new VariableSelectionChangeEvent(table, variableDto, getPreviousVariable(index), getNextVariable(index)));
    }
  }

  class SiblingVariableSelectionHandler implements SiblingVariableSelectionEvent.Handler {
    @Override
    public void onSiblingVariableSelection(SiblingVariableSelectionEvent event) {
      VariableDto siblingSelection = event.getCurrentSelection();

      // Look for the variable and its position in the list by its name.
      // Having a position of the current variable would be more efficient.
      int siblingIndex = 0;
      for(int i = 0; i < variables.length(); i++) {
        if(variables.get(i).getName().equals(event.getCurrentSelection().getName())) {
          if(event.getDirection().equals(SiblingVariableSelectionEvent.Direction.NEXT) && i < variables.length() - 1) {
            siblingIndex = i + 1;
          } else if(event.getDirection().equals(SiblingVariableSelectionEvent.Direction.PREVIOUS) && i != 0) {
            siblingIndex = i - 1;
          } else {
            siblingIndex = i;
          }
          break;
        }
      }
      siblingSelection = variables.get(siblingIndex);

      getView().setVariableSelection(siblingSelection, siblingIndex);
      getEventBus().fireEvent(new VariableSelectionChangeEvent(table, siblingSelection, getPreviousVariable(siblingIndex), getNextVariable(siblingIndex)));
    }
  }

  // OPAL-975
  class ViewSavedEventHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      if(table != null) {
        updateDisplay(table, previous, next);
      }
    }
  }

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<TablePresenter> {
  }

  public interface Display extends View {

    enum Slots {
      Permissions, Values
    }

    void setVariableSelection(VariableDto variable, int index);

    void setValuesDisplay(ValuesTablePresenter.Display display);

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

    void setCreateCodingViewCommand(Command cmd);

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

    String getClickableColumnName(Column<?, ?> column);

    void setValuesTabCommand(Command cmd);

    boolean isValuesTabSelected();

  }

}
