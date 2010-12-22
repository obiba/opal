/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.view;

import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitListPresenter;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class FunctionalUnitListView extends Composite implements FunctionalUnitListPresenter.Display {

  @UiTemplate("FunctionalUnitListView.ui.xml")
  interface FunctionalUnitListViewUiBinder extends UiBinder<Widget, FunctionalUnitListView> {
  }

  private static FunctionalUnitListViewUiBinder uiBinder = GWT.create(FunctionalUnitListViewUiBinder.class);

  @UiField
  CellTable<FunctionalUnitDto> functionalUnitTable;

  SingleSelectionModel<FunctionalUnitDto> selectionModel;

  JsArrayDataProvider<FunctionalUnitDto> dataProvider = new JsArrayDataProvider<FunctionalUnitDto>();

  public FunctionalUnitListView() {
    selectionModel = new SingleSelectionModel<FunctionalUnitDto>();
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
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
  public void setFunctionalUnits(JsArray<FunctionalUnitDto> templates) {
    clearSelection();
    int templateCount = templates.length();
    functionalUnitTable.setPageSize(templateCount);
    dataProvider.setArray(templates);
    dataProvider.refresh();
    // Select the first element in the list.
    if(templates.length() > 0) {
      selectionModel.setSelected(templates.get(0), true);
    }
  }

  private void clearSelection() {
    if(getSelectedFunctionalUnit() != null) {
      selectionModel.setSelected(getSelectedFunctionalUnit(), false);
      functionalUnitTable.redraw();
    }
  }

  public FunctionalUnitDto getSelectedFunctionalUnit() {
    return selectionModel.getSelectedObject();
  }

  private void initTable() {

    functionalUnitTable.setStyleName("selection-list");
    functionalUnitTable.addColumn(new TextColumn<FunctionalUnitDto>() {
      @Override
      public String getValue(FunctionalUnitDto dto) {
        return dto.getName();
      }
    });
    functionalUnitTable.setSelectionModel(selectionModel);
    dataProvider.addDataDisplay(functionalUnitTable);
  }

  @Override
  public HandlerRegistration addSelectFunctionalUnitHandler(SelectionChangeEvent.Handler handler) {
    return selectionModel.addSelectionChangeHandler(handler);
  }
}
