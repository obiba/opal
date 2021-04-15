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

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;

public class RestStepView extends ViewImpl implements RestStepPresenter.Display {

  @UiField
  TextBox url;

  @UiField
  Chooser authMethod;

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  PasswordTextBox token;

  @UiField
  TextBox remoteDatasource;

  @UiField
  ControlGroup urlGroup;

  @UiField
  ControlGroup usernameGroup;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  ControlGroup tokenGroup;

  @UiField
  ControlGroup remoteDatasourceGroup;

  private final Translations translations;

  interface Binder extends UiBinder<Widget, RestStepView> {
  }

  @Inject
  public RestStepView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    authMethod.addItem(translations.accessTokenLabel(), "token");
    authMethod.addItem(translations.credentialsLabel(), "credentials");
    onAuthMethodChosen(null);
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
  public HasValue<Boolean> getUseCredentials() {
    return new BooleanHasValue() {
      @Override
      public Boolean getValue() {
        return usernameGroup.isVisible();
      }
    };
  }

  @Override
  public PasswordTextBox getToken() {
    return token;
  }

  @Override
  public HasValue<Boolean> getUseToken() {
    return new BooleanHasValue() {
      @Override
      public Boolean getValue() {
        return tokenGroup.isVisible();
      }
    };
  }

  @UiHandler("authMethod")
  public void onAuthMethodChosen(ChangeEvent e) {
    boolean isToken = "token".equals(authMethod.getSelectedValue());
    tokenGroup.setVisible(isToken);
    usernameGroup.setVisible(!isToken);
    passwordGroup.setVisible(!isToken);
  }

  @Override
  public HasText getUrl() {
    return url;
  }

  @Override
  public HasType<ControlGroupType> getGroupType(String id) {
    RestStepPresenter.Display.FormField field = RestStepPresenter.Display.FormField.valueOf(id);
    switch (field) {
      case URL:
        return urlGroup;
      case USERNAME:
        return usernameGroup;
      case PASSWORD:
        return passwordGroup;
      case TOKEN:
        return tokenGroup;
      case REMOTE_DATESOURCE:
        return remoteDatasourceGroup;
    }

    return null;
  }

  private abstract class BooleanHasValue implements HasValue<Boolean> {

    @Override
    public void setValue(Boolean value) {

    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {

    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
      return null;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {

    }
  }
}
