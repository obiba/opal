/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.widgets.view.EditableListBox;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter.TableSelectionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.Chooser;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class DestinationSelectionStepView extends ViewImpl implements DestinationSelectionStepPresenter.Display {

  @UiTemplate("DestinationSelectionStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DestinationSelectionStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  Chooser datasourceListBox;

  @UiField
  EditableListBox tableListBox;

  @UiField
  Panel tableInput;

  @UiField
  EditableListBox entityTypeListBox;

  @UiField
  Panel entityTypeInput;

  private JsArray<DatasourceDto> datasources;

  private TableSelectionHandler tableSelectionHandler;

  public DestinationSelectionStepView() {
    widget = uiBinder.createAndBindUi(this);

    entityTypeListBox.addItem("Participant");
    entityTypeListBox.setText("Participant");

    addHandlers();
  }

  private void addHandlers() {
    datasourceListBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        displayTablesFor(datasourceListBox.getSelectedValue());
      }
    });

    tableListBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        boolean knownTable = tableListBox.hasItem(tableListBox.getText());
        entityTypeListBox.setEnabled(knownTable == false);
        if(knownTable) {
          tableSelectionHandler.onTableSelected(getSelectedDatasource(), getSelectedTable());
        }
      }
    });
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public String getSelectedDatasource() {
    return datasourceListBox.getSelectedValue();
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    datasourceListBox.clear();
    for(int i = 0; i < datasources.length(); i++) {
      DatasourceDto dto = datasources.get(i);
      if(!"null".equals(dto.getType())) {
        datasourceListBox.addItem(dto.getName(), dto.getName());
      }
    }
    this.datasources = datasources;
    if(datasources.length() > 0) displayTablesFor(datasources.get(0).getName());
  }

  @Override
  public String getSelectedTable() {
    return this.tableListBox.getValue();
  }

  @Override
  public String getSelectedEntityType() {
    return entityTypeListBox.getText();
  }

  @Override
  public void setTable(String name) {
    tableListBox.setValue(name, true);
  }

  @Override
  public void showTables(boolean visible) {
    tableInput.setVisible(visible);
    entityTypeInput.setVisible(visible);
    entityTypeListBox.setEnabled(false);
    entityTypeListBox.setText("Participant");
  }

  private void displayTablesFor(String datasourceName) {
    tableListBox.clear();

    DatasourceDto datasource = getDatasource(datasourceName);
    if(datasource != null) {
      List<String> tables = toList(datasource.getTableArray());
      List<String> views = toList(datasource.getViewArray());
      tables.removeAll(views);

      for(String tableName : tables) {
        tableListBox.addItem(tableName);
      }
    }
  }

  private DatasourceDto getDatasource(String datasourceName) {
    for(int i = 0; i < datasources.length(); i++) {
      DatasourceDto datasource = datasources.get(i);
      if(datasource.getName().equals(datasourceName)) {
        return datasource;
      }
    }
    return null;
  }

  private List<String> toList(JsArrayString jsArrayString) {
    List<String> list = new ArrayList<String>();
    for(int i = 0; i < jsArrayString.length(); i++) {
      list.add(jsArrayString.get(i));
    }
    return list;
  }

  @Override
  public void setEntityType(String entityType) {
    entityTypeListBox.setText(entityType);
  }

  @Override
  public void setTableSelectionHandler(TableSelectionHandler handler) {
    this.tableSelectionHandler = handler;
  }
}
