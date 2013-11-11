/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DestinationSelectionStepPresenter.TableSelectionHandler;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.EditableListBox;
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
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class DestinationSelectionStepView extends ViewImpl implements DestinationSelectionStepPresenter.Display {

  interface Binder extends UiBinder<Widget, DestinationSelectionStepView> {}

  @UiField
  EditableListBox tableListBox;

  @UiField
  Panel tableInput;

  @UiField
  EditableListBox entityTypeListBox;

  @UiField
  Panel entityTypeInput;

  private DatasourceDto datasource;

  private TableSelectionHandler tableSelectionHandler;

  @Inject
  public DestinationSelectionStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    entityTypeListBox.addItem("Participant");
    entityTypeListBox.setText("Participant");

    addHandlers();
  }

  private void addHandlers() {
    tableListBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        boolean knownTable = tableListBox.hasItem(tableListBox.getText());
        entityTypeListBox.setEnabled(!knownTable);
        if(knownTable) {
          tableSelectionHandler.onTableSelected(datasource.getName(), getSelectedTable());
        }
      }
    });
  }

  @Override
  public void setDatasource(DatasourceDto datasource) {
    this.datasource = datasource;
    tableListBox.clear();

    if(datasource != null) {
      List<String> tables = toList(datasource.getTableArray());
      List<String> views = toList(datasource.getViewArray());
      tables.removeAll(views);

      for(String tableName : tables) {
        tableListBox.addItem(tableName);
      }
    }
  }

  @Override
  public String getSelectedTable() {
    return tableListBox.getValue();
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
    tableSelectionHandler = handler;
  }
}
