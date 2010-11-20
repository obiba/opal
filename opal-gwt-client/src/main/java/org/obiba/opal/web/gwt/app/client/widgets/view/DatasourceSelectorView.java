/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DatasourceSelectorView extends Composite implements DatasourceSelectorPresenter.Display {
  //
  // Instance Variables
  //

  private ListBox datasourceListBox;

  private JsArray<DatasourceDto> datasources;

  //
  // Constructors
  //

  public DatasourceSelectorView() {
    datasourceListBox = new ListBox();
    initWidget(datasourceListBox);
  }

  //
  // DatasourceSelectorPresenter.Display Methods
  //

  public void setEnabled(boolean enabled) {
    datasourceListBox.setEnabled(enabled);
  }

  public void setDatasources(JsArray<DatasourceDto> datasources) {
    datasourceListBox.clear();
    this.datasources = datasources;

    for(int i = 0; i < datasources.length(); i++) {
      datasourceListBox.addItem(datasources.get(i).getName());
    }
  }

  public void selectFirst() {
    if(datasourceListBox.getItemCount() != 0) {
      datasourceListBox.setSelectedIndex(0);
    }
  }

  public void setSelection(String datasourceName) {
    for(int i = 0; i < datasourceListBox.getItemCount(); i++) {
      String item = datasourceListBox.getValue(i);
      if(item.equals(datasourceName)) {
        datasourceListBox.setSelectedIndex(i);
        break;
      }
    }
  }

  public String getSelection() {
    int selectedIndex = datasourceListBox.getSelectedIndex();
    return selectedIndex != -1 ? datasourceListBox.getValue(selectedIndex) : null;
  }

  @Override
  public DatasourceDto getSelectionDto() {
    int selectedIndex = datasourceListBox.getSelectedIndex();
    return selectedIndex != -1 ? datasources.get(selectedIndex) : null;
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }
}
