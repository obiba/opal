/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.list.identifiers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.list.DatabaseListColumns;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.MongoDbSettingsDto;
import org.obiba.opal.web.model.client.database.SqlSettingsDto;

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class IdentifiersDatabaseView extends ViewWithUiHandlers<IdentifiersDatabaseUiHandlers>
    implements IdentifiersDatabasePresenter.Display {

  interface Binder extends UiBinder<Widget, IdentifiersDatabaseView> {}

  @UiField
  Panel databasePanel;

  @UiField
  Table<DatabaseDto> table;

  @UiField
  IconAnchor edit;

//  @UiField
//  Button deleteDatabase;

  @UiField
  DropdownButton registerIdentifersDB;

  private final Translations translations;

  private final DatabaseListColumns columns;

  private ListDataProvider<DatabaseDto> dataProvider = new ListDataProvider<DatabaseDto>();

  @Inject
  public IdentifiersDatabaseView(Binder uiBinder, Translations translations) {
    this.translations = translations;

    initWidget(uiBinder.createAndBindUi(this));

    columns = new DatabaseListColumns(translations);
    initTable();

    edit.setTitle(translations.editLabel());
    registerIdentifersDB.setText(translations.register());
    databasePanel.setVisible(true);
  }

  private void initTable() {
    table.addColumn(columns.name, translations.nameLabel());
    table.addColumn(columns.url, translations.urlLabel());
    table.addColumn(columns.type, translations.typeLabel());
    table.addColumn(columns.usage, translations.usageLabel());
    table.addColumn(columns.schema, translations.schemaLabel());
    table.addColumn(columns.username, translations.usernameLabel());
    table.addColumn(columns.actions, translations.actionsLabel());
    initColumnsWidth();

    table.setEmptyTableWidget(new Label(translations.identifiersDatabaseRequiredLabel()));
    dataProvider.addDataDisplay(table);

    registerActionsHandlers();
  }

  private void initColumnsWidth() {
    table.setColumnWidth(columns.name, 25, Style.Unit.PCT);
    table.setColumnWidth(columns.url, 25, Style.Unit.PCT);
    table.setColumnWidth(columns.type, 9, Style.Unit.PCT);
    table.setColumnWidth(columns.usage, 9, Style.Unit.PCT);
    table.setColumnWidth(columns.schema, 9, Style.Unit.PCT);
    table.setColumnWidth(columns.username, 9, Style.Unit.PCT);
    table.setColumnWidth(columns.actions, 15, Style.Unit.PCT);
  }

  private void registerActionsHandlers() {
    columns.actions.setActionHandler(new ActionHandler<DatabaseDto>() {
      @Override
      public void doAction(DatabaseDto object, String actionName) {
        if(actionName.equals(DatabaseListColumns.TEST_ACTION)) {
          getUiHandlers().testConnection();
        } else if(actionName.equals(DatabaseListColumns.UNREGISTER_ACTION)) {
          getUiHandlers().deleteDatabase();
        } else if(actionName.equals(ActionsColumn.EDIT_ACTION)) {
          getUiHandlers().edit();
        }
      }
    });
  }

  @UiHandler("createSql")
  void createSql(ClickEvent event) {
    getUiHandlers().createSql();
  }

  @UiHandler("createMongo")
  void createMongo(ClickEvent event) {
    getUiHandlers().createMongo();
  }

  @UiHandler("edit")
  void edit(ClickEvent event) {
    getUiHandlers().edit();
  }

  @Override
  public void setDatabase(@Nullable DatabaseDto database) {
//    properties.clearProperties();
    List<DatabaseDto> list = new ArrayList<DatabaseDto>();
    if(database != null) list.add(database);
    dataProvider.setList(list);

    boolean hasDatabase = database != null;

    edit.setVisible(hasDatabase);
    if(hasDatabase) {
      showSqlProperties(database.getSqlSettings());
      showMongoProperties(database.getMongoDbSettings());
    }
  }

  @Override
  public void enableEditionDeletion(boolean value) {
    edit.setVisible(value);
//    deleteDatabase.setVisible(value);
  }

  private void showSqlProperties(@Nullable SqlSettingsDto sqlDatabase) {
    if(sqlDatabase == null) return;
//    properties.addProperty(translations.typeLabel(), translations.sqlLabel());
//    properties.addProperty(translations.sqlSchemaLabel(),
//        AbstractDatabaseModalPresenter.SqlSchema.valueOf(sqlDatabase.getSqlSchema().getName()).getLabel());
//    properties.addProperty(translations.urlLabel(), sqlDatabase.getUrl());
//    properties.addProperty(translations.driverLabel(), sqlDatabase.getDriverClass());
//    properties.addProperty(translations.usernameLabel(), sqlDatabase.getUsername());
//    properties.addProperty(translations.propertiesLabel(), sqlDatabase.getProperties());
  }

  private void showMongoProperties(@Nullable MongoDbSettingsDto mongoDatabase) {
    if(mongoDatabase == null) return;
//    properties.addProperty(translations.typeLabel(), translations.mongoDbLabel());
//    properties.addProperty(translations.urlLabel(), mongoDatabase.getUrl());
//    properties.addProperty(translations.usernameLabel(), mongoDatabase.getUsername());
//    properties.addProperty(translations.propertiesLabel(), mongoDatabase.getProperties());
  }

}
