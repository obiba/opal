/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.LimesurveyStepPresenter;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class LimesurveyStepView extends ViewImpl implements LimesurveyStepPresenter.Display {

  private final Widget widget;

  @UiField
  ListBox database;

  @UiField
  TextBox tablePrefix;

  @UiField
  HTMLPanel help;

  @UiTemplate("LimesurveyStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, LimesurveyStepView> {}

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

  @Override
  public String getTablePrefix() {
    return tablePrefix.getValue();
  }

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @Inject
  public LimesurveyStepView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public Widget getStepHelp() {
    help.removeFromParent();
    return help;
  }
}
