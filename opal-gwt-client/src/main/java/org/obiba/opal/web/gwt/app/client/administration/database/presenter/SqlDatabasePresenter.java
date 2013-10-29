/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredValueValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.JdbcDriverDto;
import org.obiba.opal.web.model.client.database.SqlSettingsDto;
import org.obiba.opal.web.model.client.magma.JdbcDatasourceSettingsDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.administration.database.presenter.SqlDatabasePresenter.Display.FormField;

public class SqlDatabasePresenter extends AbstractDatabasePresenter<SqlDatabasePresenter.Display> {

  @Inject
  public SqlDatabasePresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();

    ResourceRequestBuilderFactory.<JsArray<JdbcDriverDto>>newBuilder() //
        .forResource(DatabaseResources.drivers()) //
        .withCallback(new ResourceCallback<JsArray<JdbcDriverDto>>() {

          @Override
          public void onResource(Response response, JsArray<JdbcDriverDto> drivers) {
            getView().setAvailableDrivers(drivers);
          }
        }) //
        .get().send();

    getView().getUsageChangeHandlers().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        Usage usage = getView().getUsage().getValue();
        getView().toggleDefaultStorage(usage == Usage.STORAGE);
        getView().setAvailableSqlSchemas(usage.getSupportedSqlSchemas());
      }
    });

    getView().getSqlSchemaChangeHandlers().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        SqlSchema sqlSchema = getView().getSqlSchema().getValue();
        getView().toggleJdbcOptions(sqlSchema == SqlSchema.JDBC);
        getView().toggleLimesurveyOptions(sqlSchema == SqlSchema.LIMESURVEY);
      }
    });
  }

  @Override
  protected void displayDatabase(DatabaseDto dto) {
    getView().getName().setText(dto.getName());
    getView().getUsage().setValue(Usage.valueOf(dto.getUsage().getName()));
    getView().getDefaultStorage().setValue(dto.getDefaultStorage());
    SqlSettingsDto sqlSettings = dto.getSqlSettings();
    getView().getUrl().setText(sqlSettings.getUrl());
    getView().getDriver().setText(sqlSettings.getDriverClass());
    getView().getUsername().setText(sqlSettings.getUsername());
    getView().getPassword().setText(sqlSettings.getPassword());
    getView().getProperties().setText(sqlSettings.getProperties());
    getView().getSqlSchema().setValue(SqlSchema.valueOf(sqlSettings.getSqlSchema().getName()));
    JdbcDatasourceSettingsDto jdbcDatasourceSettings = sqlSettings.getJdbcDatasourceSettings();
    if(SqlSettingsDto.SqlSchema.JDBC.getName().equals(sqlSettings.getSqlSchema().getName()) &&
        jdbcDatasourceSettings != null) {
      getView().getDefaultEntityType().setText(jdbcDatasourceSettings.getDefaultEntityType());
      getView().getDefaultCreatedTimestampColumn()
          .setText(jdbcDatasourceSettings.getDefaultCreatedTimestampColumnName());
      getView().getDefaultUpdatedTimestampColumn()
          .setText(jdbcDatasourceSettings.getDefaultUpdatedTimestampColumnName());
      getView().getUseMetadataTables().setValue(jdbcDatasourceSettings.getUseMetadataTables());
    } else if(SqlSettingsDto.SqlSchema.LIMESURVEY.getName().equals(sqlSettings.getSqlSchema().getName()) &&
        sqlSettings.getLimesurveyDatasourceSettings() != null) {
      getView().getTablePrefix().setText(sqlSettings.getLimesurveyDatasourceSettings().getTablePrefix());
    }
  }

  @Override
  protected void hideNonEditableIdentifiersDatabaseFields() {
    getView().getNameGroupVisibility().setVisible(false);
    getView().getUsageGroupVisibility().setVisible(false);
    getView().getDefaultStorageGroupVisibility().setVisible(false);
    getView().getSqlSchemaGroupVisibility().setVisible(false);
  }

  @Override
  protected DatabaseDto getDto() {
    DatabaseDto dto = DatabaseDto.create();
    SqlSettingsDto sqlDto = SqlSettingsDto.create();

    dto.setUsedForIdentifiers(usedForIdentifiers);
    dto.setName(getView().getName().getText());
    dto.setUsage(parseUsage(getView().getUsage().getValue()));
    dto.setDefaultStorage(getView().getDefaultStorage().getValue());

    sqlDto.setUrl(getView().getUrl().getText());
    sqlDto.setDriverClass(getView().getDriver().getText());
    sqlDto.setUsername(getView().getUsername().getText());
    sqlDto.setPassword(getView().getPassword().getText());
    sqlDto.setProperties(getView().getProperties().getText());
    SqlSettingsDto.SqlSchema sqlSchema = parseSqlSchema(getView().getSqlSchema().getValue());
    sqlDto.setSqlSchema(sqlSchema);

    if(SqlSettingsDto.SqlSchema.JDBC.getName().equals(sqlSchema.getName())) {
      sqlDto.setJdbcDatasourceSettings(getJdbcDatasourceSettingsDto());
    } else if(SqlSettingsDto.SqlSchema.LIMESURVEY.getName().equals(sqlSchema.getName())) {
      sqlDto.setLimesurveyDatasourceSettings(getLimesurveyDatasourceSettingsDto());
    }

    dto.setSqlSettings(sqlDto);
    return dto;
  }

  private SqlSettingsDto.LimesurveyDatasourceSettingsDto getLimesurveyDatasourceSettingsDto() {
    SqlSettingsDto.LimesurveyDatasourceSettingsDto limesurveySettings = SqlSettingsDto.LimesurveyDatasourceSettingsDto
        .create();
    limesurveySettings.setTablePrefix(getView().getTablePrefix().getText());
    return limesurveySettings;
  }

  private JdbcDatasourceSettingsDto getJdbcDatasourceSettingsDto() {
    JdbcDatasourceSettingsDto jdbcSettings = JdbcDatasourceSettingsDto.create();
    jdbcSettings.setDefaultEntityType(getView().getDefaultEntityType().getText());
    jdbcSettings.setDefaultCreatedTimestampColumnName(getView().getDefaultCreatedTimestampColumn().getText());
    jdbcSettings.setDefaultUpdatedTimestampColumnName(getView().getDefaultUpdatedTimestampColumn().getText());
    jdbcSettings.setUseMetadataTables(getView().getUseMetadataTables().getValue());
    return jdbcSettings;
  }

  private SqlSettingsDto.SqlSchema parseSqlSchema(@Nonnull SqlSchema sqlSchema) {
    switch(sqlSchema) {
      case HIBERNATE:
        return SqlSettingsDto.SqlSchema.HIBERNATE;
      case JDBC:
        return SqlSettingsDto.SqlSchema.JDBC;
      case LIMESURVEY:
        return SqlSettingsDto.SqlSchema.LIMESURVEY;
      default:
        throw new IllegalArgumentException("Unknown Sql Schema: " + sqlSchema);
    }
  }

  @Override
  protected ViewValidationHandler createValidationHandler() {
    return new SqlDatabaseValidationHandler();
  }

  private class SqlDatabaseValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", FormField.NAME.name()));
        validators.add(new RequiredTextValidator(getView().getUrl(), "UrlIsRequired", FormField.URL.name()));
        validators
            .add(new RequiredTextValidator(getView().getUsername(), "UsernameIsRequired", FormField.USERNAME.name()));
        validators
            .add(new RequiredTextValidator(getView().getPassword(), "PasswordIsRequired", FormField.PASSWORD.name()));
        validators.add(new RequiredValueValidator(getView().getUsage(), "UsageIsRequired", FormField.USAGE.name()));
        validators.add(
            new RequiredValueValidator(getView().getSqlSchema(), "SqlSchemaIsRequired", FormField.SQL_SCHEMA.name()));
        validators.add(new RequiredTextValidator(getView().getDriver(), "DriverIsRequired", FormField.DRIVER.name()));
        validators.add(new DefaultEntityTypeValidator());
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(FormField.valueOf(id), message);
    }

    private class DefaultEntityTypeValidator extends AbstractFieldValidator {

      private DefaultEntityTypeValidator() {
        super("DefaultEntityTypeIsRequired", FormField.DEFAULT_ENTITY_TYPE.name());
      }

      @Override
      protected boolean hasError() {
        return SqlSchema.JDBC == getView().getSqlSchema().getValue() &&
            Strings.nullToEmpty(getView().getDefaultEntityType().getText()).trim().isEmpty();
      }
    }

  }

  public interface Display extends AbstractDatabasePresenter.Display {

    enum FormField {
      NAME,
      DRIVER,
      URL,
      USERNAME,
      PASSWORD,
      USAGE,
      SQL_SCHEMA,
      DEFAULT_ENTITY_TYPE
    }

    void setAvailableDrivers(JsArray<JdbcDriverDto> availableDrivers);

    void setAvailableSqlSchemas(SqlSchema... sqlSchemas);

    void showError(@Nullable FormField formField, String message);

    TakesValue<SqlSchema> getSqlSchema();

    HasText getDriver();

    HasText getTablePrefix();

    HasText getDefaultEntityType();

    HasText getDefaultCreatedTimestampColumn();

    HasText getDefaultUpdatedTimestampColumn();

    HasValue<Boolean> getUseMetadataTables();

    HasChangeHandlers getSqlSchemaChangeHandlers();

    void toggleLimesurveyOptions(boolean show);

    void toggleJdbcOptions(boolean show);

    HasVisibility getNameGroupVisibility();

    HasVisibility getDefaultStorageGroupVisibility();

    HasVisibility getSqlSchemaGroupVisibility();

  }

}
