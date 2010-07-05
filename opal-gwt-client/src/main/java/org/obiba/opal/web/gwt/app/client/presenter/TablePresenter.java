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
import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.TableDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class TablePresenter extends WidgetPresenter<TablePresenter.Display> {

  public interface Display extends WidgetDisplay {

    void setVariableSelection(VariableDto variable, int index);

    void renderRows(JsArray<VariableDto> rows);

    void clear();

    HasText getTableName();

    HasText getEntityTypeLabel();

    HasClickHandlers getSpreadsheetIcon();

    void setParentName(String name);

    HasClickHandlers getParentLink();

    HasClickHandlers getNextLink();

    HasClickHandlers getPreviousLink();

    HasFieldUpdater<VariableDto, String> getVariableNameColumn();
  }

  private JsArray<VariableDto> variables;

  private TableDto table;

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
    super.registerHandler(eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionChangeEvent.Handler() {
      @Override
      public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
        if(event.getSelection().getParentItem() != null) {
          String datasource = event.getSelection().getParentItem().getText();
          String table = event.getSelection().getText();
          displayTable(datasource, table);
        }
      }
    }));

    super.registerHandler(getDisplay().getSpreadsheetIcon().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        downloadMetadata();
      }
    }));

    super.registerHandler(getDisplay().getParentLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + table.getDatasourceName()).get().withCallback(new ResourceCallback<DatasourceDto>() {
          @Override
          public void onResource(Response response, DatasourceDto resource) {
            eventBus.fireEvent(new DatasourceSelectionChangeEvent(resource));
          }

        }).send();
      }
    }));

    super.registerHandler(getDisplay().getNextLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new SiblingTableSelectionEvent(table, Direction.NEXT));
      }
    }));

    super.registerHandler(getDisplay().getPreviousLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new SiblingTableSelectionEvent(table, Direction.PREVIOUS));
      }
    }));

    super.getDisplay().getVariableNameColumn().setFieldUpdater(new FieldUpdater<VariableDto, String>() {

      @Override
      public void update(int index, VariableDto variableDto, String value) {
        eventBus.fireEvent(new VariableSelectionChangeEvent(variableDto));
      }

    });

    super.registerHandler(eventBus.addHandler(SiblingVariableSelectionEvent.getType(), new SiblingVariableSelectionEvent.Handler() {

      @Override
      public void onSiblingVariableSelection(SiblingVariableSelectionEvent event) {
        VariableDto siblingSelection = event.getCurrentSelection();

        // Look for the variable and its position in the list by its name.
        // Having an position of the current variable would be more efficient.
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
        eventBus.fireEvent(new VariableSelectionChangeEvent(siblingSelection));
      }
    }));
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

  private void displayTable(TableDto tableDto) {
    if(!table.getDatasourceName().equals(tableDto.getDatasourceName()) || !table.getName().equals(tableDto.getName())) {
      table = tableDto;
      getDisplay().clear();
      getDisplay().getTableName().setText(tableDto.getName());
      getDisplay().setParentName(tableDto.getDatasourceName());
      getDisplay().getEntityTypeLabel().setText(tableDto.getEntityType());
      updateVariables();
    }
  }

  private void displayTable(String datasourceName, String tableName) {
    if(!table.getDatasourceName().equals(datasourceName) || !table.getName().equals(tableName)) {
      getDisplay().clear();
      getDisplay().getTableName().setText(tableName);
      getDisplay().setParentName(datasourceName);

      ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource("/datasource/" + datasourceName + "/table/" + tableName).get().withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto resource) {
          table = resource;
          getDisplay().getEntityTypeLabel().setText(resource.getEntityType());
          updateVariables();
        }

      }).send();
    }
  }

  private void updateVariables() {
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(table.getLink() + "/variables").get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {
      @Override
      public void onResource(Response response, JsArray<VariableDto> resource) {
        variables = resource;
        getDisplay().renderRows(variables);
      }

    }).send();
  }

  private void downloadMetadata() {
    String downloadUrl = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "ws")).append(this.table.getLink()).append("/variables/xlsx").toString();
    eventBus.fireEvent(new FileDownloadEvent(downloadUrl));
  }

}
