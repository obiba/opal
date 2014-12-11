/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.AbstractCsvOptionsViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.CsvFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.CsvFormatStepUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.EditableListBox;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CsvFormatStepView extends AbstractCsvOptionsViewWithUiHandlers<CsvFormatStepUiHandlers>
    implements CsvFormatStepPresenter.Display {

  //
  // Instance Variables
  //

  @UiField
  SimplePanel selectFilePanel;

  @UiField
  ControlGroup selectFileGroup;

  @UiField
  EditableListBox tableListBox;

  @UiField
  ControlGroup tableGroup;

  @UiField
  EditableListBox entityTypeListBox;

  @UiField
  ControlGroup entityTypeGroup;

  @UiField
  CsvOptionsView csvOptions;

  private DatasourceDto datasource;

  //
  // Constructors
  //

  @Inject
  public CsvFormatStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // AbstractCsvOptionsView Methods
  //

  @Override
  protected CsvOptionsView getCsvOptions() {
    return csvOptions;
  }

  @Override
  public HasType<ControlGroupType> getGroupType(String id) {
    try {
      FormField field = FormField.valueOf(id);
      switch(field) {
        case FILE:
          return selectFileGroup;
        case TABLE:
          return tableGroup;
        case ENTITY_TYPE:
          return entityTypeGroup;
      }
    } catch(Exception ignored) {
    }

    return getCsvOptions().getGroupType(id);
  }

  @Override
  public HasText getSelectedTable() {
    return tableListBox;
  }

  @Override
  public HasText getSelectedEntityType() {
    return entityTypeListBox;
  }

  @Override
  public void setCsvFileSelectorWidgetDisplay(FileSelectionPresenter.Display display) {
    selectFilePanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

  @Override
  public void setCsvFileSelectorVisible(boolean value) {
    csvOptions.setCsvFileSelectorVisible(value);
  }

  @UiHandler("tableListBox")
  public void onTableChanged(ValueChangeEvent<String> event) {
    boolean knownTable = tableListBox.hasItem(tableListBox.getText());
    entityTypeListBox.setEnabled(!knownTable);
    if(knownTable) {
      getUiHandlers().selectTable(tableListBox.getText());
    }
  }

  @Override
  public void updateTables(DatasourceDto datasource) {
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
  public void setTable(String tableName) {
    tableListBox.setValue(tableName, true);
  }

  @Override
  public void setEntityType(String entityType) {
    entityTypeListBox.setText(entityType);
  }

  private List<String> toList(JsArrayString jsArrayString) {
    List<String> list = new ArrayList<String>();
    for(int i = 0; i < jsArrayString.length(); i++) {
      list.add(jsArrayString.get(i));
    }
    return list;
  }

  //
  // Inner Classes / Interfaces
  //

  interface Binder extends UiBinder<Widget, CsvFormatStepView> {}

}
