/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.edit.sql;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.ListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.DatabaseUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.TabPanelAuthorizer;
import org.obiba.opal.web.model.client.database.JdbcDriverDto;
import org.obiba.opal.web.model.client.magma.JdbcDatasourceSettingsDto;
import org.obiba.opal.web.model.client.magma.JdbcValueTableSettingsDto;
import org.obiba.opal.web.model.client.magma.JdbcValueTableSettingsFactoryDto;

import javax.annotation.Nullable;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.Mode.CREATE;
import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.SqlSchema;
import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.Usage;

/**
 *
 */
public class SqlDatabaseModalView extends ModalPopupViewWithUiHandlers<DatabaseUiHandlers>
  implements SqlDatabaseModalPresenter.Display {

  interface Binder extends UiBinder<Widget, SqlDatabaseModalView> {
  }

  private static final int JDBC_TABLES_TAB_INDEX = 1;

  private static final int JDBC_TABLE_PARTITIONS_TAB_INDEX = 2;

  private static final int MAX_BATCH_SIZE = 1000;

  private static final int DEFAULT_BATCH_SIZE = 100;

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
  CollapsiblePanel jdbcOptions;

  @UiField
  TabPanel jdbcOptionsTabPanel;

  @UiField
  CollapsiblePanel advancedOptions;

  @UiField
  TextBox defaultEntityType;

  @UiField
  TextBox defaultEntityIdColumn;

  @UiField
  TextBox defaultUpdatedTimestampColumn;

  @UiField
  NumericTextBox batchSize;

  @UiField
  ControlGroup useMetadataTablesGroup;

  @UiField
  CheckBox useMetadataTables;

  @UiField
  ControlGroup defaultEntityTypeGroup;

  @UiField
  JdbcTableSettingsEditor jdbcTableSettingsEditor;

  @UiField
  JdbcTableSettingsFactoriesEditor jdbcTableSettingsFactoriesEditor;

  private final Translations translations;

  private boolean isIdentifiers;

  private String selectedDriver;

  private JsArray<JdbcDriverDto> availableDrivers;

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
        initExistingUrl();
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

    jdbcOptions.setText(translations.jdbcOptionsLabel());

    sqlSchema.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        initDrivers();
      }
    });

    batchSize.setMax(MAX_BATCH_SIZE);
    batchSize.setMin(1);
    batchSize.setValue(DEFAULT_BATCH_SIZE);
  }

  @Override
  public void setIsIdentifiers(boolean isIdentifiers) {
    this.isIdentifiers = isIdentifiers;
  }

  @Override
  public void initUrl() {
    String defaultName = isIdentifiers ? "opal_ids" : "opal_data";
    if (getDriver().getText() == null || "com.mysql.jdbc.Driver".equals(getDriver().getText())) {
      url.setText("jdbc:mysql://localhost:3306/" + defaultName);
    } else if (getDriver().getText() == null || "org.mariadb.jdbc.Driver".equals(getDriver().getText())) {
      url.setText("jdbc:mariadb://localhost:3306/" + defaultName);
    } else if ("org.postgresql.Driver".equals(getDriver().getText())) {
      url.setText("jdbc:postgresql://localhost:5432/" + defaultName);
    }
  }

  private void initExistingUrl() {
    String urlTxt = url.getText();
    int start = urlTxt.indexOf(':');
    if (start == -1) {
      initUrl();
    } else {
      int end = urlTxt.indexOf(':', start + 1);
      if (getDriver().getText().contains("mysql")) {
        urlTxt = "jdbc:mysql" + urlTxt.substring(end);
        urlTxt = urlTxt.replaceAll( "5432", "3306");
      } else if (getDriver().getText().contains("mariadb")) {
        urlTxt = "jdbc:mariadb" + urlTxt.substring(end);
        urlTxt = urlTxt.replaceAll("5432", "3306");
      }else if (getDriver().getText().contains("postgresql")) {
        urlTxt = "jdbc:postgresql" + urlTxt.substring(end);
        urlTxt = urlTxt.replaceAll("3306", "5432");
      }
      url.setText(urlTxt);
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
  @SuppressWarnings({"OverlyLongMethod", "PMD.NcssMethodCount"})
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if (formField != null) {
      switch (formField) {
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
    if (group == null) {
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
        if (selectedSqlSchema == null) return;
        int count = sqlSchema.getItemCount();
        for (int i = 0; i < count; i++) {
          if (sqlSchema.getValue(i).equals(selectedSqlSchema.name())) {
            sqlSchema.setSelectedIndex(i);
            initDrivers();
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
    for (Usage usageType : Usage.values()) {
      usage.addItem(usageType.getLabel(), usageType.name());
    }
    getUsage().setValue(Usage.STORAGE);
  }

  @Override
  public void setSupportedSqlSchemas(SqlSchema... sqlSchemas) {
    sqlSchema.clear();
    if (sqlSchemas != null) {
      for (SqlSchema schema : sqlSchemas) {
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
        if (selectedUsage == null) return;
        int count = usage.getItemCount();
        for (int i = 0; i < count; i++) {
          if (usage.getValue(i).equals(selectedUsage.name())) {
            usage.setSelectedIndex(i);
            setSupportedSqlSchemas(selectedUsage.getSupportedSqlSchemas());
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
        for (int i = 0; i < count; i++) {
          if (driver.getValue(i).equals(text)) {
            driver.setSelectedIndex(i);
            break;
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
        if (!Strings.isNullOrEmpty(text)) {
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
  public void setJdbcDatasourceSettings(JdbcDatasourceSettingsDto jdbcDatasourceSettings) {
    defaultEntityType.setText(jdbcDatasourceSettings.getDefaultEntityType());
    defaultEntityIdColumn.setText(jdbcDatasourceSettings.getDefaultEntityIdColumnName());
    defaultUpdatedTimestampColumn.setText(jdbcDatasourceSettings.getDefaultUpdatedTimestampColumnName());
    useMetadataTables.setValue(jdbcDatasourceSettings.getUseMetadataTables());
    batchSize.setValue(jdbcDatasourceSettings.getBatchSize());
    jdbcTableSettingsEditor.addJdbcTableSettings(JsArrays.toList(jdbcDatasourceSettings.getTableSettingsArray()));
    jdbcTableSettingsFactoriesEditor.addJdbcTableSettingsFactory(JsArrays.toList(jdbcDatasourceSettings.getTableSettingsFactoriesArray()));
  }

  @Override
  public JdbcDatasourceSettingsDto getJdbcDatasourceSettings() {
    JdbcDatasourceSettingsDto jdbcSettings = JdbcDatasourceSettingsDto.create();
    jdbcSettings.setDefaultEntityType(defaultEntityType.getText());
    jdbcSettings.setDefaultEntityIdColumnName(defaultEntityIdColumn.getText());
    if (getUsageValue() == Usage.STORAGE) jdbcSettings.setDefaultCreatedTimestampColumnName("opal_created");
    jdbcSettings.setDefaultUpdatedTimestampColumnName(defaultUpdatedTimestampColumn.getText());
    jdbcSettings.setUseMetadataTables(useMetadataTables.getValue());
    jdbcSettings.setMultipleDatasources(getUsageValue() == Usage.STORAGE);
    // multilines are detected at import/storage and forced at export
    jdbcSettings.setMultilines(getUsageValue() == Usage.EXPORT);

    List<String> mappedTables = Lists.newArrayList();
    jdbcSettings.setTableSettingsArray(jdbcTableSettingsEditor.getJdbcTableSettings());
    jdbcSettings.setTableSettingsFactoriesArray(jdbcTableSettingsFactoriesEditor.getJdbcTableSettingsFactories());

    for (JdbcValueTableSettingsDto dto : JsArrays.toIterable(jdbcSettings.getTableSettingsArray()))
      if (!mappedTables.contains(dto.getSqlTable())) mappedTables.add(dto.getSqlTable());
    for (JdbcValueTableSettingsFactoryDto dto : JsArrays.toIterable(jdbcSettings.getTableSettingsFactoriesArray()))
      if (!mappedTables.contains(dto.getSqlTable())) mappedTables.add(dto.getSqlTable());

    if (!mappedTables.isEmpty())
      jdbcSettings.setMappedTablesArray(JsArrays.fromIterable(mappedTables));

    if (!batchSize.getText().isEmpty())
      jdbcSettings.setBatchSize(batchSize.getNumberValue().intValue());

    return jdbcSettings;
  }

  @Override
  public boolean hasJdbcTableSettingsError() {
    try {
      jdbcTableSettingsEditor.getJdbcTableSettings();
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  @Override
  public boolean hasJdbcTableSettingsFactoriesError() {
    try {
      jdbcTableSettingsFactoriesEditor.getJdbcTableSettingsFactories();
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  @Override
  public void disableFieldsForDatabaseWithDatasource() {
    sqlSchema.setEnabled(false);
    //driver.setEnabled(false);
    defaultEntityType.setEnabled(false);
    defaultUpdatedTimestampColumn.setEnabled(false);
    useMetadataTables.setEnabled(false);
  }

  @Override
  public HasText getDefaultEntityType() {
    return defaultEntityType;
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
  public void setAvailableDrivers(JsArray<JdbcDriverDto> availableDrivers) {
    this.availableDrivers = availableDrivers;
    initDrivers();
  }

  @Override
  public void toggleDefaultStorage(boolean show) {
    if (!show) defaultStorage.setValue(false);
    defaultStorageGroup.setVisible(show);
  }

  @Override
  public void toggleJdbcOptions(boolean show) {
    Usage usageValue = getUsageValue();
    boolean storage = usageValue == Usage.STORAGE;
    defaultEntityType.setValue("Participant");
    // set default jdbc options when usage is storage
    defaultEntityIdColumn.setValue(show && storage ? "opal_id" : "id");
    defaultUpdatedTimestampColumn.setValue(show && storage ? "opal_updated" : usageValue == Usage.EXPORT ? "updated" : "");
    useMetadataTables.setValue(show && storage);
    useMetadataTablesGroup.setVisible(show && usageValue == Usage.EXPORT);
    // do not show jdbc options when usage is storage
    jdbcOptions.setVisible(show && !storage);
    jdbcOptions.setOpen(true);
    if (show && !storage) {
      HasAuthorization authorizer = new CompositeAuthorizer(new TabPanelAuthorizer(jdbcOptionsTabPanel, JDBC_TABLES_TAB_INDEX),
        new TabPanelAuthorizer(jdbcOptionsTabPanel, JDBC_TABLE_PARTITIONS_TAB_INDEX));
      if (usageValue == Usage.IMPORT) authorizer.authorized();
      else authorizer.unauthorized();
    }
  }

  private Usage getUsageValue() {
    int selectedIndex = usage.getSelectedIndex();
    return selectedIndex < 0 ? null : Usage.valueOf(usage.getValue(selectedIndex));
  }

  private void initDrivers() {
    String selectedSchema = getSqlSchema().getValue().name().toLowerCase();
    driver.clear();
    for (JdbcDriverDto driverDto : JsArrays.toIterable(availableDrivers)) {
      if (JsArrays.toList(driverDto.getSupportedSchemasArray()).contains(selectedSchema))
        driver.addItem(driverDto.getDriverName(), driverDto.getDriverClass());
    }
    // select MySQL by default but do not override previously selected driver if any
    getDriver().setText(selectedDriver == null ? "com.mysql.jdbc.Driver" : selectedDriver);
  }
}
