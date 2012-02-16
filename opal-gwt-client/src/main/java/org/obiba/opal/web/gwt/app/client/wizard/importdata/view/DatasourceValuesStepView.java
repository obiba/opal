/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DatasourceValuesStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DatasourceValuesStepPresenter.TableSelectionHandler;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DatasourceValuesStepView extends ViewImpl implements DatasourceValuesStepPresenter.Display {
  @UiTemplate("DatasourceValuesStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DatasourceValuesStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  private TableSelectionHandler tableSelectionHandler;

  @UiField
  ListBox tableList;

  @UiField
  SimplePanel tableValuesPanel;

  private JsArray<TableDto> tables;

  public DatasourceValuesStepView() {
    widget = uiBinder.createAndBindUi(this);
    tableList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        tableSelectionHandler.onTableSelection(tables.get(tableList.getSelectedIndex()));
      }
    });
  }

  //
  // Display methods
  //

  @Override
  public void setTables(JsArray<TableDto> tables) {
    tableList.clear();
    this.tables = tables;
    for(int i = 0; i < tables.length(); i++) {
      tableList.addItem(tables.get(i).getName());
    }
    tableSelectionHandler.onTableSelection(tables.get(0));
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == Slots.Values) {
      tableValuesPanel.clear();
      tableValuesPanel.add(content);
    }
  }

  @Override
  public Widget getStepHelp() {
    return null;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTableSelectionHandler(TableSelectionHandler handler) {
    this.tableSelectionHandler = handler;
  }
}
