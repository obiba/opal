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

import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class DestinationSelectionStepView extends Composite implements DestinationSelectionStepPresenter.Display {

  @UiTemplate("DestinationSelectionStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DestinationSelectionStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  ListBox datasetListBox;

  @UiField
  ListBox tableListBox;

  @UiField
  Label tableListBoxLabel;

  private JsArray<DatasourceDto> datasources;

  public DestinationSelectionStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    addHandlers();
  }

  private void addHandlers() {
    datasetListBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        displayTablesFor(datasetListBox.getValue(datasetListBox.getSelectedIndex()));
      }
    });
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public String getSelectedDatasource() {
    return this.datasetListBox.getValue(this.datasetListBox.getSelectedIndex());
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    this.datasetListBox.clear();
    for(int i = 0; i < datasources.length(); i++) {
      this.datasetListBox.addItem(datasources.get(i).getName(), datasources.get(i).getName());
    }
    this.datasources = datasources;
    if(datasources.length() > 0) displayTablesFor(datasources.get(0).getName());
  }

  @Override
  public String getSelectedTable() {
    return this.tableListBox.getValue(this.tableListBox.getSelectedIndex());
  }

  @Override
  public boolean hasTable() {
    return this.tableListBox.getItemCount() > 0;
  }

  @Override
  public void hideTables() {
    tableListBoxLabel.setVisible(false);
    tableListBox.setVisible(false);
  }

  @Override
  public void showTables() {
    tableListBoxLabel.setVisible(true);
    tableListBox.setVisible(true);
  }

  @Override
  public Widget getStepHelp() {
    // TODO Auto-generated method stub
    return null;
  }

  private void displayTablesFor(String datasourceName) {
    tableListBox.clear();

    DatasourceDto datasource = getDatasource(datasourceName);
    if(datasource != null) {
      List<String> tables = toList(datasource.getTableArray());
      List<String> views = toList(datasource.getViewArray());
      tables.removeAll(views);

      for(String tableName : tables) {
        tableListBox.addItem(tableName, tableName);
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
}
