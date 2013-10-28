/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.view;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DataDatabasesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DataDatabasesUiHandlers;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.SqlDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class DataDatabasesView extends ViewWithUiHandlers<DataDatabasesUiHandlers>
    implements DataDatabasesPresenter.Display {

  interface Binder extends UiBinder<Widget, DataDatabasesView> {}

  @UiField
  Panel createPanel;

  @UiField
  Panel databasesPanel;

  @UiField
  SimplePager sqlPager;

  @UiField
  Table<DatabaseDto> sqlTable;

  @UiField
  SimplePager mongoPager;

  @UiField
  Table<DatabaseDto> mongoTable;

  private final Translations translations;

  private final Columns columns = new Columns();

  @Inject
  public DataDatabasesView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    databasesPanel.setVisible(false);
    initSqlTable();
    initMongoTable();
  }

  private void initSqlTable() {
    sqlPager.setDisplay(sqlTable);
    sqlTable.addColumn(columns.name, translations.nameLabel());
    sqlTable.addColumn(columns.sqlUrl, translations.urlLabel());
    sqlTable.addColumn(columns.usage, translations.usageLabel());
    sqlTable.addColumn(columns.sqlSchema, translations.sqlSchemaLabel());
    sqlTable.addColumn(columns.sqlUsername, translations.usernameLabel());
    sqlTable.addColumn(columns.actions, translations.actionsLabel());
  }

  private void initMongoTable() {
    mongoPager.setDisplay(mongoTable);
    mongoTable.addColumn(columns.name, translations.nameLabel());
    mongoTable.addColumn(columns.mongoUrl, translations.urlLabel());
    mongoTable.addColumn(columns.usage, translations.usageLabel());
    mongoTable.addColumn(columns.mongoUsername, translations.usernameLabel());
    mongoTable.addColumn(columns.actions, translations.actionsLabel());
  }

  @UiHandler("addSQL")
  public void onAddSQL(ClickEvent event) {
    getUiHandlers().createSql(false);
  }

  @UiHandler("createSql")
  public void onCreateSQL(ClickEvent event) {
    getUiHandlers().createSql(true);
  }

  @UiHandler("addMongo")
  public void onAddMongo(ClickEvent event) {
    getUiHandlers().createMongo(false);
  }

  @UiHandler("createMongo")
  public void onCreateMongo(ClickEvent event) {
    getUiHandlers().createMongo(true);
  }

  @UiHandler("sqlTable")
  public void onSqlTableChange(RowCountChangeEvent event) {
    createPanel.setVisible(!isDatabasesVisible());
    databasesPanel.setVisible(isDatabasesVisible());
  }

  @UiHandler("mongoTable")
  public void onMongoTableChange(RowCountChangeEvent event) {
    createPanel.setVisible(!isDatabasesVisible());
    databasesPanel.setVisible(isDatabasesVisible());
  }

  private boolean isDatabasesVisible() {
    return sqlTable.getRowCount() > 0 || mongoTable.getRowCount() > 0;
  }

  @Override
  public HasActionHandler<DatabaseDto> getActions() {
    return columns.actions;
  }

  @Override
  public HasData<DatabaseDto> getSqlTable() {
    return sqlTable;
  }

  @Override
  public HasData<DatabaseDto> getMongoTable() {
    return mongoTable;
  }

  private final class Columns {

    final Column<DatabaseDto, String> name = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        String value = dto.getName();
        if(dto.getDefaultStorage()) value += " (" + translations.defaultStorage().toLowerCase() + ")";
        return value;
      }
    };

    final Column<DatabaseDto, String> sqlUrl = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return dto.getSqlSettings().getUrl();
      }
    };

    final Column<DatabaseDto, String> mongoUrl = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return dto.getMongoDbSettings().getUrl();
      }
    };

    final Column<DatabaseDto, String> usage = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return SqlDatabasePresenter.Usage.valueOf(dto.getUsage().getName()).getLabel();
      }
    };

    final Column<DatabaseDto, String> sqlSchema = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return SqlDatabasePresenter.SqlSchema.valueOf(dto.getSqlSettings().getSqlSchema().getName()).getLabel();
      }
    };

    final Column<DatabaseDto, String> sqlUsername = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return dto.getSqlSettings().getUsername();
      }
    };

    final Column<DatabaseDto, String> mongoUsername = new TextColumn<DatabaseDto>() {
      @Override
      public String getValue(DatabaseDto dto) {
        return dto.getMongoDbSettings().getUsername();
      }
    };

    final ActionsColumn<DatabaseDto> actions = new ActionsColumn<DatabaseDto>(new ActionsProvider<DatabaseDto>() {

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
