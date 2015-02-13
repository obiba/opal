/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.edit.sql;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.DatabaseUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
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
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.Mode.CREATE;
import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.SqlSchema;
import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.Usage;

/**
 *
 */
public class SqlDatabaseModalView extends ModalPopupViewWithUiHandlers<DatabaseUiHandlers>
    implements SqlDatabaseModalPresenter.Display {

  interface Binder extends UiBinder<Widget, SqlDatabaseModalView> {}

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
  Panel defaultStorageGroup;

  @UiField
  CheckBox defaultStorage;

  @UiField
  CollapsiblePanel limesurveyOptions;

  @UiField
  TextBox tablePrefix;

  @UiField
  CollapsiblePanel jdbcOptions;

  @UiField
  CollapsiblePanel advancedOptions;

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

  private final Translations translations;

  private boolean isIdentifiers;

  private String selectedDriver;

  @Inject
  public SqlDatabaseModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
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
        initUrl();
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

    limesurveyOptions.setText(translations.limesurveyOptionsLabel());
    jdbcOptions.setText(translations.jdbcOptionsLabel());
  }

  @Override
  public void setIsIdentifiers(boolean isIdentifiers) {this.isIdentifiers = isIdentifiers;}

  @Override
  public void initUrl() {
    String defaultName = isIdentifiers ? "opal_ids" : "opal_data";
    if(getDriver().getText() == null || "com.mysql.jdbc.Driver".equals(getDriver().getText())) {
      url.setText("jdbc:mysql://localhost:3306/" + defaultName);
    }
  }

  @Override
  public void onShow() {
    name.setFocus(true);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void setDialogMode(AbstractDatabaseModalPresenter.Mode dialogMode) {
    name.setEnabled(dialogMode == CREATE);
    modal.setTitle(dialogMode == CREATE ? translations.addSQLDatabase() : translations.editSQLDatabase());
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  @Override
  public void showError(String message) {
    showError(null, message);
  }

  @Override
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
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
  public HasEnabled getNameEnabled() {
    return name;
  }

  @Override
  public HasVisibility getNameGroupVisibility() {
    return nameGroup;
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

  @Override
  public HasEnabled getSqlSchemaEnabled() {
    return sqlSchema;
  }

  @Override
  public HasVisibility getSqlSchemaGroupVisibility() {
    return sqlSchemaGroup;
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
      for(SqlSchema schema : sqlSchemas) {
        sqlSchema.addItem(schema.getLabel(), schema.name());
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
  public HasEnabled getUsageEnabled() {
    return usage;
  }

  @Override
  public HasVisibility getUsageGroupVisibility() {
    return usageGroup;
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
        selectedDriver = text;
        int count = driver.getItemCount();
        for(int i = 0; i < count; i++) {
          if(driver.getValue(i).equals(text)) {
            driver.setSelectedIndex(i);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasEnabled getDriverEnabled() {
    return driver;
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
  public HasEnabled getPropertiesEnabled() {
    return properties;
  }

  @Override
  public HasText getTablePrefix() {
    return tablePrefix;
  }

  @Override
  public HasEnabled getTablePrefixEnabled() {
    return tablePrefix;
  }

  @Override
  public HasText getDefaultEntityType() {
    return defaultEntityType;
  }

  @Override
  public HasEnabled getDefaultEntityTypeEnabled() {
    return defaultEntityType;
  }

  @Override
  public HasText getDefaultCreatedTimestampColumn() {
    return defaultCreatedTimestampColumn;
  }

  @Override
  public HasEnabled getDefaultCreatedTimestampColumnEnabled() {
    return defaultCreatedTimestampColumn;
  }

  @Override
  public HasText getDefaultUpdatedTimestampColumn() {
    return defaultUpdatedTimestampColumn;
  }

  @Override
  public HasEnabled getDefaultUpdatedTimestampColumnEnabled() {
    return defaultUpdatedTimestampColumn;
  }

  @Override
  public HasValue<Boolean> getDefaultStorage() {
    return defaultStorage;
  }

  @Override
  public HasVisibility getDefaultStorageGroupVisibility() {
    return defaultStorage;
  }

  @Override
  public HasValue<Boolean> getUseMetadataTables() {
    return useMetadataTables;
  }

  @Override
  public HasEnabled getUseMetadataTablesEnabled() {
    return useMetadataTables;
  }

  @Override
  public void setAvailableDrivers(JsArray<JdbcDriverDto> availableDrivers) {
    for(JdbcDriverDto driverDto : JsArrays.toIterable(availableDrivers)) {
      driver.addItem(driverDto.getDriverName(), driverDto.getDriverClass());
    }
    // select MySQL by default but do not override previously selected driver if any
    getDriver().setText(selectedDriver == null ? "com.mysql.jdbc.Driver" : selectedDriver);
  }

  @Override
  public void toggleDefaultStorage(boolean show) {
    if(!show) defaultStorage.setValue(false);
    defaultStorageGroup.setVisible(show);
  }

  @Override
  public void toggleLimesurveyOptions(boolean show) {
    if(!show) tablePrefix.setValue(null);
    limesurveyOptions.setVisible(show);
  }

  @Override
  public void toggleJdbcOptions(boolean show) {
    if(!show) {
      defaultCreatedTimestampColumn.setValue(null);
      defaultUpdatedTimestampColumn.setValue(null);
      useMetadataTables.setValue(false);
    }
    jdbcOptions.setVisible(show);
    jdbcOptions.setOpen(true);
  }

}
