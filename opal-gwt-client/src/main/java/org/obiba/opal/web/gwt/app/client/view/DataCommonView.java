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

import org.obiba.opal.web.gwt.app.client.presenter.DataCommonPresenter;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

/**
 * View elements common between the export and import dialog.
 */
public abstract class DataCommonView extends Composite implements DataCommonPresenter.Display {

  @UiField
  ListBox datasources;

  @UiField
  ListBox units;

  @UiField
  Button submit;

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

  @Override
  public HandlerRegistration addSubmitClickHandler(ClickHandler handler) {
    return submit.addClickHandler(handler);
  }

}
