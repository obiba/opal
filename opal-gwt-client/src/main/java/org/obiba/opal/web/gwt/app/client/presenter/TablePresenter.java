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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class TablePresenter extends WidgetPresenter<TablePresenter.Display> {

  private JsArray<VariableDto> variables;

  private TableDto table;

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
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    super.registerHandler(eventBus.addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeHandler()));
    super.registerHandler(eventBus.addHandler(SiblingVariableSelectionEvent.getType(), new SiblingVariableSelectionHandler()));
    super.registerHandler(getDisplay().addSpreadSheetClickHandler(new SpreadSheetClickHandler()));
    super.registerHandler(getDisplay().addParentLinkClickHandler(new ParentClickHandler()));
    super.registerHandler(getDisplay().addParentImageClickHandler(new ParentClickHandler()));
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
    super.registerHandler(getDisplay().addPreviousClickHandler(new PreviousClickHandler()));
    super.getDisplay().setVariableNameFieldUpdater(new VariableNameFieldUpdater());

  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
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
      getDisplay().setTableName(tableDto.getName());
      getDisplay().setEntityType(tableDto.getEntityType());
      getDisplay().setParentName(tableDto.getDatasourceName());
      getDisplay().setPreviousName(previous);
      getDisplay().setNextName(next);

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

  //
  // Interfaces and classes
  //

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
        variables = (resource != null) ? resource : (JsArray<VariableDto>) JsArray.createArray();
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

  class PreviousClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new SiblingTableSelectionEvent(table, Direction.PREVIOUS));
    }
  }

  class NextClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new SiblingTableSelectionEvent(table, Direction.NEXT));
    }
  }

  class ParentClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + table.getDatasourceName()).get().withCallback(new ResourceCallback<DatasourceDto>() {
        @Override
        public void onResource(Response response, DatasourceDto resource) {
          eventBus.fireEvent(new DatasourceSelectionChangeEvent(resource));
        }

      }).send();
    }
  }

  class SpreadSheetClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      downloadMetadata();
    }
  }

  public interface Display extends WidgetDisplay {

    void setVariableSelection(VariableDto variable, int index);

    void beforeRenderRows();

    void renderRows(JsArray<VariableDto> rows);

    void afterRenderRows();

    void clear();

    void setTableName(String name);

    void setEntityType(String text);

    HandlerRegistration addParentLinkClickHandler(ClickHandler handler);

    HandlerRegistration addParentImageClickHandler(ClickHandler handler);

    HandlerRegistration addSpreadSheetClickHandler(ClickHandler handler);

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousClickHandler(ClickHandler handler);

    void setParentName(String name);

    void setPreviousName(String name);

    void setNextName(String name);

    void setVariableNameFieldUpdater(FieldUpdater<VariableDto, String> updater);
  }

}
