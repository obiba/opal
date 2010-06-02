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
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataExportView extends DataCommonView implements DataExportPresenter.Display {

  @UiTemplate("DataExportView.ui.xml")
  interface DataExportUiBinder extends UiBinder<Widget, DataExportView> {
  }

  private static DataExportUiBinder uiBinder = GWT.create(DataExportUiBinder.class);

  @UiField
  Tree tableTree;

  @UiField
  TextBox file;

  @UiField
  RadioButton destinationDataSource;

  @UiField
  RadioButton destinationFile;

  @UiField
  CheckBox incremental;

  @UiField
  CheckBox withVariables;

  @UiField
  CheckBox useAlias;

  @UiField
  RadioButton opalId;

  @UiField
  RadioButton unitId;

  private JsArrayString selectedFiles = JavaScriptObject.createArray().cast();

  SelectionModel<VariableDto> selectionModel = new SingleSelectionModel<VariableDto>();

  public DataExportView() {
    initWidget(uiBinder.createAndBindUi(this));
    tableTree.setAnimationEnabled(true);
    destinationDataSource.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        datasources.setEnabled(true);
        file.setEnabled(false);
      }
    });
    destinationFile.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        datasources.setEnabled(false);
        file.setEnabled(true);
      }
    });
    destinationDataSource.setValue(true);
    file.setEnabled(false);

    opalId.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        units.setEnabled(false);
      }
    });
    unitId.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        units.setEnabled(true);
      }
    });
    opalId.setValue(true);
    units.setEnabled(false);
    incremental.setValue(true);
    withVariables.setValue(true);
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
  public void addTable(String datasource, String table) {
    selectedFiles.push(datasource + "." + table);
  }

  @Override
  public HasValue<String> getFile() {
    return file;
  }

  @Override
  public RadioButton getDestinationFile() {
    return destinationFile;
  }

  @Override
  public JsArrayString getSelectedFiles() {
    return selectedFiles;
  }

  @Override
  public HasValue<Boolean> isIncremental() {
    return incremental;
  }

  @Override
  public HasValue<Boolean> isUseAlias() {
    return useAlias;
  }

  @Override
  public HasValue<Boolean> isWithVariables() {
    return withVariables;
  }

  @Override
  public HasValue<Boolean> isUnitId() {
    return unitId;
  }

  @Override
  public HasValue<Boolean> isDestinationDataSource() {
    return destinationDataSource;
  }

  @Override
  public String getOutFile() {
    return file.getValue();
  }

}
