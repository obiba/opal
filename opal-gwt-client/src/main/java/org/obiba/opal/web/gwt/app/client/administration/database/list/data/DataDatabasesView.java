/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.list.data;

import org.obiba.opal.web.gwt.app.client.administration.database.list.DatabaseListColumns;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class DataDatabasesView extends ViewWithUiHandlers<DataDatabasesUiHandlers>
    implements DataDatabasesPresenter.Display {

  interface Binder extends UiBinder<Widget, DataDatabasesView> {}

  @UiField
  Panel databasesPanel;

  @UiField
  SimplePager pager;

  @UiField
  Table<DatabaseDto> table;

  @UiField
  DropdownButton registerDataDatabase;

  private final Translations translations;

  private final DatabaseListColumns columns;

  @Inject
  public DataDatabasesView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));

    columns = new DatabaseListColumns(translations);

    registerDataDatabase.setText(translations.register());
    initTable();
  }

  private void initTable() {
    pager.setDisplay(table);
    table.addColumn(columns.name, translations.nameLabel());
    table.addColumn(columns.url, translations.urlLabel());
    table.addColumn(columns.type, translations.typeLabel());
    table.addColumn(columns.usage, translations.usageLabel());
    table.addColumn(columns.schema, translations.schemaLabel());
    table.addColumn(columns.username, translations.usernameLabel());
    table.addColumn(columns.actions, translations.actionsLabel());
    initColumnsWidth();

    table.setEmptyTableWidget(new Label(translations.dataDatabaseRequiredLabel()));
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

  @UiHandler("createSql")
  public void onCreateSQL(ClickEvent event) {
    getUiHandlers().createSql(true);
  }

  @UiHandler("createMongo")
  public void onCreateMongo(ClickEvent event) {
    getUiHandlers().createMongo(true);
  }

  @UiHandler("table")
  public void onTableChange(RowCountChangeEvent event) {
    pager.setVisible(table.getRowCount() > pager.getPageSize());
  }

  @Override
  public HasActionHandler<DatabaseDto> getActions() {
    return columns.actions;
  }

  @Override
  public HasData<DatabaseDto> getTable() {
    return table;
  }

}
