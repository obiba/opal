/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.RestStepPresenter;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class RestStepView extends ViewImpl implements RestStepPresenter.Display {

  @UiField
  TextBox url;

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  TextBox remoteDatasource;

  @UiField
  ControlGroup urlGroup;

  @UiField
  ControlGroup usernameGroup;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  ControlGroup remoteDatasourceGroup;

  interface Binder extends UiBinder<Widget, RestStepView> {}

  @Inject
  public RestStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public HasText getRemoteDatasource() {
    return remoteDatasource;
  }

  @Override
  public HasText getPassword() {
    return password;
  }

  @Override
  public HasText getUsername() {
    return username;
  }

  @Override
  public HasText getUrl() {
    return url;
  }

  @Override
  public HasType<ControlGroupType> getGroupType(String id) {
    RestStepPresenter.Display.FormField field = RestStepPresenter.Display.FormField.valueOf(id);
    switch(field) {
      case URL:
        return urlGroup;
      case USERNAME:
        return usernameGroup;
      case PASSWORD:
        return passwordGroup;
      case REMOTE_DATESOURCE:
        return remoteDatasourceGroup;
    }

    return null;
  }
}
