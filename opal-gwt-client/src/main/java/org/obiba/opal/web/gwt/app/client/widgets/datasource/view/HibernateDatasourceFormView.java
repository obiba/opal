/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.datasource.view;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class HibernateDatasourceFormView extends ViewImpl implements HibernateDatasourceFormPresenter.Display {

  @UiTemplate("HibernateDatasourceFormView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, HibernateDatasourceFormView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  ListBox database;

  @UiField
  CheckBox binaries;

  public HibernateDatasourceFormView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setDatabases(JsArray<JdbcDataSourceDto> databases) {
    database.clear();
    for(JdbcDataSourceDto d : JsArrays.toIterable(databases)) {
      database.addItem(d.getName());
    }
    binaries.setValue(true);
  }

  @Override
  public String getSelectedDatabase() {
    int selectedIndex = database.getSelectedIndex();
    return selectedIndex == 0 ? null : database.getItemText(selectedIndex);
  }

  @Override
  public boolean getBinaries() {
    return binaries.getValue();
  }
}
