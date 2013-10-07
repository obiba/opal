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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.SqlDatabaseDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class DatabaseAdministrationView extends ViewImpl implements DatabaseAdministrationPresenter.Display {

  @UiTemplate("DatabaseAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DatabaseAdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

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

  public DatabaseAdministrationView() {
    uiWidget = uiBinder.createAndBindUi(this);
    sqlPager.setDisplay(sqlTable);
    sqlTable.addColumn(Columns.NAME, translations.nameLabel());
    sqlTable.addColumn(Columns.URL, translations.urlLabel());
    sqlTable.addColumn(Columns.DRIVER, translations.driverLabel());
    sqlTable.addColumn(Columns.USERNAME, translations.usernameLabel());
    sqlTable.addColumn(Columns.ACTIONS, translations.actionsLabel());
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public HasClickHandlers getAddButton() {
    return addSQL;
  }

  @Override
  public HasActionHandler<DatabaseDto> getActions() {
    return Columns.ACTIONS;
  }

  @Override
  public HasData<DatabaseDto> getDatabaseTable() {
    return sqlTable;
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

    static final Column<DatabaseDto, String> URL = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getUrl();
      }
    };

    static final Column<DatabaseDto, String> DRIVER = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getDriverClass();
      }
    };

    static final Column<DatabaseDto, String> USERNAME = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getUsername();
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
