/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class JdbcStepView extends ViewImpl implements JdbcStepPresenter.Display {

  @UiField
  ListBox database;

  interface Binder extends UiBinder<Widget, JdbcStepView> {}

  @Override
  public void setDatabases(JsArray<DatabaseDto> resource) {
    database.clear();
    for(DatabaseDto dto : JsArrays.toIterable(resource)) {
      database.addItem(dto.getName());
    }
  }

  @Override
  public String getSelectedDatabase() {
    return database.getItemText(database.getSelectedIndex());
  }

  @Inject
  public JdbcStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }
}
