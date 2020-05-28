/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.github.gwtbootstrap.client.ui.DropdownButton;
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

import static org.obiba.opal.web.gwt.app.client.administration.database.list.DatabaseListColumns.TEST_ACTION;
import static org.obiba.opal.web.gwt.app.client.administration.database.list.DatabaseListColumns.UNREGISTER_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class IdentifiersDatabaseView extends ViewWithUiHandlers<IdentifiersDatabaseUiHandlers>
    implements IdentifiersDatabasePresenter.Display {

  private ActionsColumn<DatabaseDto> actions;

  interface Binder extends UiBinder<Widget, IdentifiersDatabaseView> {}

  @UiField
  Panel databasePanel;

  @UiField
  Table<DatabaseDto> table;

  @UiField
  DropdownButton registerIdentifersDB;

  private final Translations translations;

  private final DatabaseListColumns columns;

  private final ListDataProvider<DatabaseDto> dataProvider = new ListDataProvider<DatabaseDto>();

  @Inject
  public IdentifiersDatabaseView(Binder uiBinder, Translations translations) {
    this.translations = translations;

    initWidget(uiBinder.createAndBindUi(this));

    columns = new DatabaseListColumns(translations);
    initTable();

    registerIdentifersDB.setText(translations.register());
    databasePanel.setVisible(true);
  }

  private void initTable() {

    initActionsColumn();

    table.addColumn(columns.url, translations.urlLabel());
    table.addColumn(columns.type, translations.typeLabel());
    table.addColumn(columns.usage, translations.usageLabel());
    table.addColumn(columns.schema, translations.schemaLabel());
    table.addColumn(columns.username, translations.usernameLabel());
    table.addColumn(actions, translations.actionsLabel());
    initColumnsWidth();

    table.setEmptyTableWidget(new Label(translations.identifiersDatabaseRequiredLabel()));
    dataProvider.addDataDisplay(table);

    registerActionsHandlers();
  }

  private void initActionsColumn() {
    actions = new ActionsColumn<>(new ActionsProvider<DatabaseDto>() {
      @Override
      public String[] allActions() {
        return new String[] { TEST_ACTION, EDIT_ACTION, UNREGISTER_ACTION };
      }

      @Override
      public String[] getActions(DatabaseDto dto) {
        return dto.getHasDatasource() ? new String[] { EDIT_ACTION, TEST_ACTION } : allActions();

      }
    });
  }

  private void initColumnsWidth() {
    table.setColumnWidth(columns.url, 50, Style.Unit.PCT);
    table.setColumnWidth(columns.type, 9, Style.Unit.PCT);
    table.setColumnWidth(columns.usage, 9, Style.Unit.PCT);
    table.setColumnWidth(columns.schema, 9, Style.Unit.PCT);
    table.setColumnWidth(columns.username, 9, Style.Unit.PCT);
    table.setColumnWidth(actions, 15, Style.Unit.PCT);
  }

  private void registerActionsHandlers() {
    actions.setActionHandler(new ActionHandler<DatabaseDto>() {
      @Override
      public void doAction(DatabaseDto object, String actionName) {
        switch(actionName) {
          case TEST_ACTION:
            getUiHandlers().testConnection();
            break;
          case UNREGISTER_ACTION:
            getUiHandlers().deleteDatabase();
            break;
          case EDIT_ACTION:
            getUiHandlers().edit();
            break;
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

  @Override
  public void setDatabase(@Nullable DatabaseDto database) {
    List<DatabaseDto> list = new ArrayList<>();
    if(database != null) list.add(database);
    dataProvider.setList(list);
    registerIdentifersDB.setVisible(list.isEmpty());
  }

  @Override
  public void enableEditionDeletion(final boolean value) {
    // Expose ActionsProvider so we could inject a new one when the value of enableEditionDeletion changes...
    actions = new ActionsColumn<>(new ActionsProvider<DatabaseDto>() {
      @Override
      public String[] allActions() {
        return new String[] { TEST_ACTION, EDIT_ACTION, UNREGISTER_ACTION };
      }

      @Override
      public String[] getActions(DatabaseDto dto) {
        return value ? allActions() : new String[] { TEST_ACTION };
      }
    });

    table.removeColumn(5);
    table.addColumn(actions, translations.actionsLabel());
    registerActionsHandlers();
  }

}
