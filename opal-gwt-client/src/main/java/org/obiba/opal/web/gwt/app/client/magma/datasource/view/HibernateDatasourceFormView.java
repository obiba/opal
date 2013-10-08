/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.datasource.view;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class HibernateDatasourceFormView extends ViewImpl implements HibernateDatasourceFormPresenter.Display {

  @UiTemplate("HibernateDatasourceFormView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, HibernateDatasourceFormView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  ListBox database;

  public HibernateDatasourceFormView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setDatabases(JsArray<DatabaseDto> databases) {
    database.clear();
    for(DatabaseDto d : JsArrays.toIterable(databases)) {
      database.addItem(d.getName());
    }
  }

  @Override
  public String getSelectedDatabase() {
    int selectedIndex = database.getSelectedIndex();
    return selectedIndex == 0 ? null : database.getItemText(selectedIndex);
  }

}
