/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.FunctionalUnitDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;


public class DataExportView extends Composite implements DataExportPresenter.Display {

  @UiTemplate("DataExportView.ui.xml")
  interface DataExportUiBinder extends UiBinder<Widget, DataExportView> {
  }

  private static DataExportUiBinder uiBinder = GWT.create(DataExportUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  Label errors;

  @UiField
  Tree tableTree;

  @UiField
  ListBox datasources;

  @UiField
  Button cancel;

  @UiField
  ListBox units;

  SelectionModel<VariableDto> selectionModel = new SingleSelectionModel<VariableDto>();

  public DataExportView() {
    uiBinder.createAndBindUi(this);
    getDialog().setGlassEnabled(true);
    tableTree.setAnimationEnabled(true);
    cancel.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDialog().hide();
      }

    });
  }

  @Override
  public Widget asWidget() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void startProcessing() {
    // TODO Auto-generated method stub

  }

  @Override
  public void stopProcessing() {
    // TODO Auto-generated method stub

  }

  public DialogBox getDialog() {
    return dialog;
  }

  @Override
  public void showDialog() {
    getDialog().center();
    getDialog().show();
  }

  @Override
  public void setItems(List<TreeItem> items) {
    tableTree.clear();
    for(TreeItem item : items) {
      tableTree.addItem(item);
    }
  }

  @Override
  public HasSelectionHandlers<TreeItem> getTableTree() {
    return tableTree;
  }

  @Override
  public SelectionModel<VariableDto> getTableSelection() {
    return selectionModel;
  }

  @Override
  public void renderRows(final JsArray<VariableDto> rows) {
    GWT.log("item selected");
  }

  @Override
  public String getSelectedDatasource() {
    return this.datasources.getValue(this.datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    this.datasources.clear();
    for(int i = 0; i < datasources.length(); i++) {
      this.datasources.addItem(datasources.get(i).getName(), datasources.get(i).getName());
    }
  }

  @Override
  public String getSelectedUnit() {
    return this.units.getValue(this.units.getSelectedIndex());
  }

  @Override
  public void setUnits(JsArray<FunctionalUnitDto> units) {
    this.units.clear();
    for(int i = 0; i < units.length(); i++) {
      this.units.addItem(units.get(i).getName());
    }
  }
}
