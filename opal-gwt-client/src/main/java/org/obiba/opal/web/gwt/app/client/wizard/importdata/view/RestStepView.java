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

import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.RestStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class RestStepView extends ViewImpl implements RestStepPresenter.Display {

  private final Widget widget;

  @UiField
  TextBox url;

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  TextBox remoteDatasource;

  @UiField
  HTMLPanel help;

  @UiTemplate("RestStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, RestStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @Inject
  public RestStepView() {
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

  @Override
  public String getRemoteDatasource() {
    return remoteDatasource.getText();
  }

  @Override
  public String getPassword() {
    return password.getText();
  }

  @Override
  public String getUsername() {
    return username.getText();
  }

  @Override
  public String getUrl() {
    return url.getText();
  }
}
