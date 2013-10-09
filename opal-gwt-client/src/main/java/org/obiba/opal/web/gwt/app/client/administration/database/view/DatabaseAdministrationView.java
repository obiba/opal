/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.view;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.SqlDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.MongoDbDatabaseDto;
import org.obiba.opal.web.model.client.database.SqlDatabaseDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class DatabaseAdministrationView extends ViewImpl implements DatabaseAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, DatabaseAdministrationView> {}

  @UiField
  Button addSQL;

  @UiField
  SimplePager sqlPager;

  @UiField
  Table<DatabaseDto> sqlTable;

  @UiField
  Button addMongo;

  @UiField
  SimplePager mongoPager;

  @UiField
  Table<DatabaseDto> mongoTable;

  @UiField
  Panel breadcrumbs;

  private final Translations translations;

  @Inject
  public DatabaseAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initSqlTable();
    initMongoTable();
  }

  private void initSqlTable() {
    sqlPager.setDisplay(sqlTable);
    sqlTable.addColumn(Columns.NAME, translations.nameLabel());
    sqlTable.addColumn(Columns.SQL_URL, translations.urlLabel());
    sqlTable.addColumn(Columns.USAGE, translations.usageLabel());
    sqlTable.addColumn(Columns.SQL_SCHEMA, translations.sqlSchemaLabel());
    sqlTable.addColumn(Columns.SQL_USERNAME, translations.usernameLabel());
    sqlTable.addColumn(Columns.ACTIONS, translations.actionsLabel());
  }

  private void initMongoTable() {
    mongoPager.setDisplay(mongoTable);
    mongoTable.addColumn(Columns.NAME, translations.nameLabel());
    mongoTable.addColumn(Columns.MONGO_URL, translations.urlLabel());
    mongoTable.addColumn(Columns.USAGE, translations.usageLabel());
    mongoTable.addColumn(Columns.MONGO_USERNAME, translations.usernameLabel());
    mongoTable.addColumn(Columns.ACTIONS, translations.actionsLabel());
  }

  @Override
  public HasClickHandlers getAddSqlButton() {
    return addSQL;
  }

  @Override
  public HasClickHandlers getAddMongoButton() {
    return addMongo;
  }

  @Override
  public HasActionHandler<DatabaseDto> getActions() {
    return Columns.ACTIONS;
  }

  @Override
  public HasData<DatabaseDto> getSqlTable() {
    return sqlTable;
  }

  @Override
  public HasData<DatabaseDto> getMongoTable() {
    return mongoTable;
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  private static final class Columns {

    static final Column<DatabaseDto, String> NAME = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return dto.getName();
      }
    };

    static final Column<DatabaseDto, String> SQL_URL = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getUrl();
      }
    };

    static final Column<DatabaseDto, String> MONGO_URL = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((MongoDbDatabaseDto) dto.getExtension(MongoDbDatabaseDto.DatabaseDtoExtensions.settings)).getUrl();
      }
    };

    static final Column<DatabaseDto, String> USAGE = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return SqlDatabasePresenter.Usage.valueOf(dto.getUsage().getName()).getLabel();
      }
    };

    static final Column<DatabaseDto, String> SQL_SCHEMA = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return SqlDatabasePresenter.SqlSchema.valueOf(
            ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getSqlSchema().getName())
            .getLabel();
      }
    };

    static final Column<DatabaseDto, String> SQL_USERNAME = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getUsername();
      }
    };

    static final Column<DatabaseDto, String> MONGO_USERNAME = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return ((MongoDbDatabaseDto) dto.getExtension(MongoDbDatabaseDto.DatabaseDtoExtensions.settings)).getUsername();
      }
    };

    static final ActionsColumn<DatabaseDto> ACTIONS = new ActionsColumn<DatabaseDto>(
        new ActionsProvider<DatabaseDto>() {

          @Override
          public String[] allActions() {
            return new String[] { TEST_ACTION, EDIT_ACTION, DELETE_ACTION };
          }

          @Override
          public String[] getActions(DatabaseDto dto) {
            return dto.getEditable() ? allActions() : new String[] { TEST_ACTION };
          }
        });
  }

}
