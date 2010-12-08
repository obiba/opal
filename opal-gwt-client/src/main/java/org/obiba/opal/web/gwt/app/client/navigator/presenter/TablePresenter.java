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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TablePresenter extends WidgetPresenter<TablePresenter.Display> {

  private JsArray<VariableDto> variables;

  private TableDto table;

  @Inject
  private Provider<NavigatorPresenter> navigationPresenter;

  private Runnable removeViewConfirmation;

  //
  // Constructors
  //

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public TablePresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  protected void onBind() {
    super.registerHandler(eventBus.addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeHandler()));
    super.registerHandler(eventBus.addHandler(SiblingVariableSelectionEvent.getType(), new SiblingVariableSelectionHandler()));
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new RemoveViewConfirmationEventHandler()));
    getDisplay().setExcelDownloadCommand(new ExcelDownloadCommand());
    getDisplay().setParentCommand(new ParentCommand());
    getDisplay().setPreviousCommand(new PreviousCommand());
    getDisplay().setNextCommand(new NextCommand());
    super.getDisplay().setVariableNameFieldUpdater(new VariableNameFieldUpdater());

  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  private void updateDisplay(TableDto tableDto, String previous, String next) {
    if(table == null || !isCurrentTable(tableDto)) {
      table = tableDto;
      getDisplay().clear();
      getDisplay().setTable(tableDto);
      getDisplay().setParentName(tableDto.getDatasourceName());
      getDisplay().setPreviousName(previous);
      getDisplay().setNextName(next);

      if(tableIsView()) {
        getDisplay().setDownloadViewCommand(new DownloadViewCommand());
        getDisplay().setRemoveCommand(new RemoveCommand());
        getDisplay().setEditCommand(new EditCommand());
      } else {
        getDisplay().setDownloadViewCommand(null);
        getDisplay().setRemoveCommand(null);
        getDisplay().setEditCommand(null);
      }

      updateVariables();
    }
  }

  private boolean isCurrentTable(TableDto tableDto) {
    return table.getDatasourceName().equals(tableDto.getDatasourceName()) && table.getName().equals(tableDto.getName());
  }

  private void updateVariables() {
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(table.getLink() + "/variables").get().withCallback(new VariablesResourceCallback(table)).send();
  }

  private void downloadMetadata() {
    String downloadUrl = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "ws")).append(this.table.getLink()).append("/variables/xlsx").toString();
    eventBus.fireEvent(new FileDownloadEvent(downloadUrl));
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
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, errorMessage, null));
        } else {
          eventBus.fireEvent(new WorkbenchChangeEvent(navigationPresenter.get()));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private boolean tableIsView() {
    return table.hasViewLink();
  }

  //
  // Interfaces and classes
  //

  final class NextCommand implements Command {
    @Override
    public void execute() {
      eventBus.fireEvent(new SiblingTableSelectionEvent(table, Direction.NEXT));
    }
  }

  final class PreviousCommand implements Command {
    @Override
    public void execute() {
      eventBus.fireEvent(new SiblingTableSelectionEvent(table, Direction.PREVIOUS));
    }
  }

  final class ParentCommand implements Command {
    @Override
    public void execute() {
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + table.getDatasourceName()).get().withCallback(new ResourceCallback<DatasourceDto>() {
        @Override
        public void onResource(Response response, DatasourceDto resource) {
          eventBus.fireEvent(new DatasourceSelectionChangeEvent(resource));
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

  final class DownloadViewCommand implements Command {
    @Override
    public void execute() {
      String downloadUrl = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "ws")).append(table.getViewLink()).append("/xml").toString();
      eventBus.fireEvent(new FileDownloadEvent(downloadUrl));
    }
  }

  final class RemoveCommand implements Command {
    @Override
    public void execute() {
      removeViewConfirmation = new Runnable() {
        public void run() {
          removeView(table.getName());
        }
      };

      eventBus.fireEvent(new ConfirmationRequiredEvent(removeViewConfirmation, "removeView", "confirmRemoveView"));
    }
  }

  class RemoveViewConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(removeViewConfirmation != null && event.getSource().equals(removeViewConfirmation) && event.isConfirmed()) {
        removeViewConfirmation.run();
        removeViewConfirmation = null;
      }
    }
  }

  final class EditCommand implements Command {
    @Override
    public void execute() {
      ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource("/datasource/" + table.getDatasourceName() + "/view/" + table.getName()).get().withCallback(new ResourceCallback<ViewDto>() {

        @Override
        public void onResource(Response response, ViewDto viewDto) {
          viewDto.setDatasourceName(table.getDatasourceName());
          viewDto.setName(table.getName());

          eventBus.fireEvent(new ViewConfigurationRequiredEvent(viewDto));
        }
      }).send();
    }
  }

  class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private TableDto table;

    public VariablesResourceCallback(TableDto table) {
      super();
      this.table = table;
      getDisplay().beforeRenderRows();
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      if(this.table.getLink().equals(TablePresenter.this.table.getLink())) {
        variables = (resource != null) ? resource : JsArray.createArray().<JsArray<VariableDto>> cast();
        getDisplay().renderRows(variables);
        getDisplay().afterRenderRows();
      }
    }
  }

  class TableSelectionChangeHandler implements TableSelectionChangeEvent.Handler {
    @Override
    public void onTableSelectionChanged(TableSelectionChangeEvent event) {
      updateDisplay(event.getSelection(), event.getPrevious(), event.getNext());
    }
  }

  class VariableNameFieldUpdater implements FieldUpdater<VariableDto, String> {
    @Override
    public void update(int index, VariableDto variableDto, String value) {
      eventBus.fireEvent(new VariableSelectionChangeEvent(variableDto, getPreviousVariable(index), getNextVariable(index)));
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

      getDisplay().setVariableSelection(siblingSelection, siblingIndex);
      eventBus.fireEvent(new VariableSelectionChangeEvent(siblingSelection, getPreviousVariable(siblingIndex), getNextVariable(siblingIndex)));
    }
  }

  public interface Display extends WidgetDisplay {

    void setVariableSelection(VariableDto variable, int index);

    /**
     * @param downloadViewCommand
     */

    void beforeRenderRows();

    void renderRows(JsArray<VariableDto> rows);

    void afterRenderRows();

    void clear();

    void setTable(TableDto dto);

    void setExcelDownloadCommand(Command cmd);

    void setDownloadViewCommand(Command cmd);

    void setParentCommand(Command cmd);

    void setNextCommand(Command cmd);

    void setPreviousCommand(Command cmd);

    void setRemoveCommand(Command cmd);

    void setEditCommand(Command cmd);

    void setParentName(String name);

    void setPreviousName(String name);

    void setNextName(String name);

    void setVariableNameFieldUpdater(FieldUpdater<VariableDto, String> updater);
  }

}
