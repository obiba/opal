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
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseAdministrationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.EDIT_ACTION;

public class DatabaseAdministrationView extends ViewImpl implements DatabaseAdministrationPresenter.Display {

  @UiTemplate("DatabaseAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DatabaseAdministrationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  Button addDatabaseButton;

  @UiField
  SimplePager databaseTablePager;

  @UiField
  Table<JdbcDataSourceDto> databaseTable;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel permissions;

  ActionsColumn<JdbcDataSourceDto> actionsColumn = new ActionsColumn<JdbcDataSourceDto>(
      new ActionsProvider<JdbcDataSourceDto>() {

        private final String[] all = new String[] {TEST_ACTION, EDIT_ACTION, DELETE_ACTION};

        private final String[] immutable = new String[] {TEST_ACTION};

        @Override
        public String[] allActions() {
          return all;
        }

        @Override
        public String[] getActions(JdbcDataSourceDto value) {
          return value.getEditable() ? allActions() : immutable;
        }
      });

  public DatabaseAdministrationView() {
    super();
    uiWidget = uiBinder.createAndBindUi(this);
    databaseTablePager.setDisplay(databaseTable);
    databaseTable.addColumn(Columns.name, translations.nameLabel());
    databaseTable.addColumn(Columns.url, translations.urlLabel());
    databaseTable.addColumn(Columns.driver, translations.driverLabel());
    databaseTable.addColumn(Columns.username, translations.usernameLabel());
    databaseTable.addColumn(actionsColumn, translations.actionsLabel());
    databaseTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == Display.Slots.Permissions) {
      permissions.clear();
      permissions.add(content);
    }
  }

  @Override
  public HasClickHandlers getAddButton() {
    return addDatabaseButton;
  }

  @Override
  public HasActionHandler<JdbcDataSourceDto> getActions() {
    return actionsColumn;
  }

  @Override
  public HasData<JdbcDataSourceDto> getDatabaseTable() {
    return databaseTable;
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  private static final class Columns {

    static Column<JdbcDataSourceDto, String> name = new TextColumn<JdbcDataSourceDto>() {

      @Override
      public String getValue(JdbcDataSourceDto object) {
        return object.getName();
      }
    };

    static Column<JdbcDataSourceDto, String> url = new TextColumn<JdbcDataSourceDto>() {

      @Override
      public String getValue(JdbcDataSourceDto object) {
        return object.getUrl();
      }
    };

    static Column<JdbcDataSourceDto, String> driver = new TextColumn<JdbcDataSourceDto>() {

      @Override
      public String getValue(JdbcDataSourceDto object) {
        return object.getDriverClass();
      }
    };

    static Column<JdbcDataSourceDto, String> username = new TextColumn<JdbcDataSourceDto>() {

      @Override
      public String getValue(JdbcDataSourceDto object) {
        return object.getUsername();
      }
    };

  }

}
