/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.datasource.view;

import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DatasourceSelectorView extends ViewImpl implements DatasourceSelectorPresenter.Display {
  //
  // Instance Variables
  //

  private final Widget uiWidget;

  private final ListBox datasourceListBox;

  private JsArray<DatasourceDto> datasources;

  //
  // Constructors
  //

  public DatasourceSelectorView() {
    datasourceListBox = new ListBox();
    uiWidget = datasourceListBox;
  }

  //
  // DatasourceSelectorPresenter.Display Methods
  //

  @Override
  public void setEnabled(boolean enabled) {
    datasourceListBox.setEnabled(enabled);
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    datasourceListBox.clear();
    this.datasources = datasources;

    for(int i = 0; i < datasources.length(); i++) {
      datasourceListBox.addItem(datasources.get(i).getName());
    }
  }

  @Override
  public void selectFirst() {
    if(datasourceListBox.getItemCount() != 0) {
      datasourceListBox.setSelectedIndex(0);
    }
  }

  @Override
  public void setSelection(String datasourceName) {
    for(int i = 0; i < datasourceListBox.getItemCount(); i++) {
      String item = datasourceListBox.getValue(i);
      if(item.equals(datasourceName)) {
        datasourceListBox.setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  public String getSelection() {
    int selectedIndex = datasourceListBox.getSelectedIndex();
    return selectedIndex == -1 ? null : datasourceListBox.getValue(selectedIndex);
  }

  @Override
  public DatasourceDto getSelectionDto() {
    int selectedIndex = datasourceListBox.getSelectedIndex();
    return selectedIndex == -1 ? null : datasources.get(selectedIndex);
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

}
