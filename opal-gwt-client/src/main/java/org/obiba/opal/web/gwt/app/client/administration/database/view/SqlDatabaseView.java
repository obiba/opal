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

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.AbstractDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseUiHandlers;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.SqlDatabasePresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;
import org.obiba.opal.web.model.client.database.JdbcDriverDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.administration.database.presenter.AbstractDatabasePresenter.Mode.CREATE;
import static org.obiba.opal.web.gwt.app.client.administration.database.presenter.AbstractDatabasePresenter.SqlSchema;
import static org.obiba.opal.web.gwt.app.client.administration.database.presenter.AbstractDatabasePresenter.Usage;

/**
 *
 */
public class SqlDatabaseView extends ModalPopupViewWithUiHandlers<DatabaseUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, SqlDatabaseView> {}

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
  ControlGroup usageGroup;

  @UiField
  ControlGroup sqlSchemaGroup;

  @UiField
  ControlGroup driverGroup;

  @UiField
  TextBox name;

  @UiField
  TextBox url;

  @UiField
  ListBox driver;

  @UiField
  ListBox usage;

  @UiField
  ListBox sqlSchema;

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  TextArea properties;

  @UiField
  CheckBox defaultStorage;

  @UiField
  DisclosurePanel limesurveyOptions;

  @UiField
  TextBox tablePrefix;

  @UiField
  DisclosurePanel jdbcOptions;

  @UiField
  DisclosurePanel advancedOptions;

  @UiField
  TextBox defaultEntityType;

  @UiField
  TextBox defaultCreatedTimestampColumn;

  @UiField
  TextBox defaultUpdatedTimestampColumn;

  @UiField
  CheckBox useMetadataTables;

  @UiField
  ControlGroup defaultEntityTypeGroup;

  private JsArray<JdbcDriverDto> availableDrivers;

  private final Translations translations;

  @Inject
  public SqlDatabaseView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    modal.hide();
    properties.getElement().setAttribute("placeholder", translations.keyValueLabel());
    setAvailableUsages();

    driver.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        JdbcDriverDto jdbcDriver = getDriver(getDriver().getText());
        if(jdbcDriver != null) {
          url.getElement().setAttribute("placeholder", jdbcDriver.getJdbcUrlTemplate());
        }
      }
    });

    // used to support ConstraintViolation exceptions
    ConstrainedModal constrainedModal = new ConstrainedModal(modal);
    constrainedModal.registerWidget("name", translations.nameLabel(), nameGroup);
    constrainedModal.registerWidget("url", translations.urlLabel(), urlGroup);
    constrainedModal.registerWidget("driver", translations.driverLabel(), driverGroup);
    constrainedModal.registerWidget("username", translations.usernameLabel(), usernameGroup);
    constrainedModal.registerWidget("password", translations.passwordLabel(), passwordGroup);
    constrainedModal.registerWidget("usage", translations.usageLabel(), usageGroup);
    constrainedModal.registerWidget("sqlSchema", translations.sqlSchemaLabel(), sqlSchemaGroup);
    constrainedModal
        .registerWidget("sqlSchema.jdbcDatasourceSettings.defaultEntityType", translations.defaultEntityTypeLabel(),
            defaultEntityTypeGroup);
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
  public void setDialogMode(AbstractDatabasePresenter.Mode dialogMode) {
    name.setEnabled(dialogMode == CREATE);
    modal.setTitle(dialogMode == CREATE ? translations.addDatabase() : translations.editDatabase());
  }

  @Override
  public void clearErrors() {
    modal.clearAlert();
  }

  @Override
  public void showError(String message) {
    showError(null, message);
  }

  @Override
  @SuppressWarnings("OverlyLongMethod")
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
        case DRIVER:
          group = driverGroup;
          break;
        case USERNAME:
          group = usernameGroup;
          break;
        case PASSWORD:
          group = passwordGroup;
          break;
        case USAGE:
          group = usageGroup;
          break;
        case SQL_SCHEMA:
          group = sqlSchemaGroup;
          break;
        case DEFAULT_ENTITY_TYPE:
          group = defaultEntityTypeGroup;
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
  public TakesValue<SqlSchema> getSqlSchema() {
    return new TakesValue<SqlSchema>() {

      @Override
      public void setValue(SqlSchema selectedSqlSchema) {
        if(selectedSqlSchema == null) return;
        int count = sqlSchema.getItemCount();
        for(int i = 0; i < count; i++) {
          if(sqlSchema.getValue(i).equals(selectedSqlSchema.name())) {
            sqlSchema.setSelectedIndex(i);
            toggleLimesurveyOptions(selectedSqlSchema == SqlSchema.LIMESURVEY);
            toggleJdbcOptions(selectedSqlSchema == SqlSchema.JDBC);
            break;
          }
        }
      }

      @Override
      public SqlSchema getValue() {
        int selectedIndex = sqlSchema.getSelectedIndex();
        return selectedIndex < 0 ? null : SqlSchema.valueOf(sqlSchema.getValue(selectedIndex));
      }
    };
  }

  private void setAvailableUsages() {
    for(Usage usageType : Usage.values()) {
      usage.addItem(usageType.getLabel(), usageType.name());
    }
    getUsage().setValue(Usage.STORAGE);
  }

  @Override
  public void setAvailableSqlSchemas(SqlSchema... sqlSchemas) {
    sqlSchema.clear();
    if(sqlSchemas != null) {
      for(SqlSchema datasourceType : sqlSchemas) {
        sqlSchema.addItem(datasourceType.getLabel(), datasourceType.name());
      }
      getSqlSchema().setValue(sqlSchemas[0]);
    }
  }

  @Override
  public TakesValue<Usage> getUsage() {
    return new TakesValue<Usage>() {
      @Override
      public Usage getValue() {
        int selectedIndex = usage.getSelectedIndex();
        return selectedIndex < 0 ? null : Usage.valueOf(usage.getValue(selectedIndex));
      }

      @Override
      public void setValue(Usage selectedUsage) {
        if(selectedUsage == null) return;
        int count = usage.getItemCount();
        for(int i = 0; i < count; i++) {
          if(usage.getValue(i).equals(selectedUsage.name())) {
            usage.setSelectedIndex(i);
            setAvailableSqlSchemas(selectedUsage.getSupportedSqlSchemas());
            toggleDefaultStorage(selectedUsage == Usage.STORAGE);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasChangeHandlers getUsageChangeHandlers() {
    return usage;
  }

  @Override
  public HasChangeHandlers getSqlSchemaChangeHandlers() {
    return sqlSchema;
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
        int selectedIndex = driver.getSelectedIndex();
        return selectedIndex < 0 ? null : driver.getValue(selectedIndex);
      }

      @Override
      public void setText(@Nullable String text) {
        if(Strings.isNullOrEmpty(text)) return;
        int count = driver.getItemCount();
        for(int i = 0; i < count; i++) {
          if(driver.getValue(i).equals(text)) {
            driver.setSelectedIndex(i);
            break;
          }
        }
        if(Strings.isNullOrEmpty(url.getText())) {
          JdbcDriverDto dto = getDriver(text);
          if(dto != null) {
            url.getElement().setAttribute("placeholder", dto.getJdbcUrlTemplate());
          }
        }
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
    return new HasText() {
      @Override
      public String getText() {
        return properties.getText();
      }

      @Override
      public void setText(String text) {
        properties.setText(text);
        if(!Strings.isNullOrEmpty(text)) {
          advancedOptions.setOpen(true);
        }
      }
    };
  }

  @Override
  public HasText getTablePrefix() {
    return tablePrefix;
  }

  @Override
  public HasText getDefaultEntityType() {
    return defaultEntityType;
  }

  @Override
  public HasText getDefaultCreatedTimestampColumn() {
    return defaultCreatedTimestampColumn;
  }

  @Override
  public HasText getDefaultUpdatedTimestampColumn() {
    return defaultUpdatedTimestampColumn;
  }

  @Override
  public HasValue<Boolean> getDefaultStorage() {
    return defaultStorage;
  }

  @Override
  public HasValue<Boolean> getUseMetadataTables() {
    return useMetadataTables;
  }

  @Override
  public void setAvailableDrivers(JsArray<JdbcDriverDto> availableDrivers) {
    this.availableDrivers = availableDrivers;
    for(JdbcDriverDto driverDto : JsArrays.toIterable(availableDrivers)) {
      driver.addItem(driverDto.getDriverName(), driverDto.getDriverClass());
    }
    getDriver().setText(null);
  }

  @Nullable
  private JdbcDriverDto getDriver(@Nullable String driverClass) {
    if(Strings.isNullOrEmpty(driverClass)) return null;
    for(JdbcDriverDto driverDto : JsArrays.toIterable(availableDrivers)) {
      if(driverDto.getDriverClass().equalsIgnoreCase(driverClass)) {
        return driverDto;
      }
    }
    return null;
  }

  @Override
  public void toggleDefaultStorage(boolean show) {
    if(!show) defaultStorage.setValue(false);
    defaultStorage.setEnabled(show);
  }

  @Override
  public void toggleLimesurveyOptions(boolean show) {
    if(!show) tablePrefix.setValue(null);
    limesurveyOptions.setVisible(show);
    limesurveyOptions.setOpen(show);
  }

  @Override
  public void toggleJdbcOptions(boolean show) {
    if(!show) {
      defaultCreatedTimestampColumn.setValue(null);
      defaultUpdatedTimestampColumn.setValue(null);
      useMetadataTables.setValue(false);
    }
    jdbcOptions.setVisible(show);
    jdbcOptions.setOpen(show);
  }

}
