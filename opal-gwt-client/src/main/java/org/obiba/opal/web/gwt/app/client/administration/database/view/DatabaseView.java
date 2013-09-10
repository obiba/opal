/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabasePresenter.Display;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabasePresenter.Mode;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class DatabaseView extends ModalPopupViewWithUiHandlers<DatabaseUiHandlers> implements Display {

  interface ViewUiBinder extends UiBinder<Widget, DatabaseView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Modal modal;

  @UiField
  ControlGroup nameGroup;

  @UiField
  ControlGroup urlGroup;

  @UiField
  ControlGroup usernameGroup;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  TextBox name;

  @UiField
  TextBox url;

  @UiField
  ListBox driver;

  @UiField
  ListBox usage;

  @UiField
  ListBox magmaDatasourceType;

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  TextArea properties;

  @UiField
  CheckBox defaultStorage;

  private JsArray<JdbcDriverDto> availableDrivers;

  @Inject
  public DatabaseView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
  }

  private void initWidgets() {
    modal.hide();
    properties.getElement().setAttribute("placeholder", translations.keyValueLabel());

    driver.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        int index = driver.getSelectedIndex();
        JdbcDriverDto jdbcDriver = getDriver(driver.getValue(index));
        if(jdbcDriver != null) {
          url.setText(jdbcDriver.getJdbcUrlTemplate());
        }
      }
    });
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void show() {
    name.setFocus(true);
    super.show();
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void setDialogMode(Mode dialogMode) {
    name.setEnabled(Mode.CREATE == dialogMode);
    modal.setTitle(Mode.CREATE == dialogMode ? translations.addDatabase() : translations.editDatabase());
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = nameGroup;
          break;
        case URL:
          group = urlGroup;
          break;
        case USERNAME:
          group = usernameGroup;
          break;
        case PASSWORD:
          group = passwordGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasText getSQLSchema() {
    return new HasText() {

      @Override
      public String getText() {
        return magmaDatasourceType.getValue(magmaDatasourceType.getSelectedIndex());
      }

      @Override
      public void setText(String text) {
        for(int i = 0; i < magmaDatasourceType.getItemCount(); i++) {
          if(magmaDatasourceType.getValue(i).equals(text)) {
            magmaDatasourceType.setSelectedIndex(i);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasText getUsage() {
    return new HasText() {

      @Override
      public String getText() {
        return usage.getValue(usage.getSelectedIndex());
      }

      @Override
      public void setText(String text) {
        for(int i = 0; i < usage.getItemCount(); i++) {
          if(usage.getValue(i).equals(text)) {
            usage.setSelectedIndex(i);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasText getUrl() {
    return url;
  }

  @Override
  public HasText getDriver() {
    return new HasText() {

      @Override
      public String getText() {
        return driver.getValue(driver.getSelectedIndex());
      }

      @Override
      public void setText(String text) {
        updateDriverSelection(text);
      }
    };
  }

  @Override
  public HasText getUsername() {
    return username;
  }

  @Override
  public HasText getPassword() {
    return password;
  }

  @Override
  public HasText getProperties() {
    return properties;
  }

  @Override
  public HasValue<Boolean> getDefaultStorage() {
    return defaultStorage;
  }

  @Override
  public void setAvailableDrivers(JsArray<JdbcDriverDto> resource) {
    availableDrivers = resource;
    for(JdbcDriverDto driverDto : JsArrays.toIterable(resource)) {
      driver.addItem(driverDto.getDriverName(), driverDto.getDriverClass());
    }
    updateDriverSelection(null);
  }

  @Nullable
  private JdbcDriverDto getDriver(@SuppressWarnings("ParameterHidesMemberVariable") String driverClass) {
    for(JdbcDriverDto driverDto : JsArrays.toIterable(availableDrivers)) {
      if(driverDto.getDriverClass().equals(driverClass)) {
        return driverDto;
      }
    }
    return null;
  }

  private void updateDriverSelection(String driverClass) {
    for(int i = 0; i < driver.getItemCount(); i++) {
      if(driver.getValue(i).equals(driverClass)) {
        driver.setSelectedIndex(i);
        break;
      }
    }
    if(Strings.isNullOrEmpty(getUrl().getText())) {
      JdbcDriverDto dto = getDriver(getDriver().getText());
      if(dto != null) {
        getUrl().setText(dto.getJdbcUrlTemplate());
      }
    }
  }

}
