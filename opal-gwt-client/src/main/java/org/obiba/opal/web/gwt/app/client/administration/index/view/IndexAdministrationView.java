/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.view;

import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.gwtplatform.mvp.client.ViewImpl;

public class IndexAdministrationView extends ViewImpl implements IndexAdministrationPresenter.Display {

  @UiTemplate("IndexAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, IndexAdministrationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  SimplePager indexTablePager;

  @UiField
  Table<TableIndexStatusDto> indexTable;

  ActionsColumn<TableIndexStatusDto> actionsColumn = new ActionsColumn<TableIndexStatusDto>(
      new ActionsProvider<TableIndexStatusDto>() {

        private final String[] all = new String[] {TEST_ACTION};//{INDEX_ACTION, CLEAR_ACTION, CANCEL_ACTION};

//        private final String[] immutable = new String[] {TEST_ACTION};

        @Override
        public String[] allActions() {
          return all;
        }

        @Override
        public String[] getActions(TableIndexStatusDto value) {
//          return value.getEditable() ? allActions() : immutable;
          return allActions();
        }
      });

  public IndexAdministrationView() {
    super();
    uiWidget = uiBinder.createAndBindUi(this);
    indexTablePager.setDisplay(indexTable);
    indexTable.addColumn(Columns.datasource, translations.nameLabel());
    indexTable.addColumn(Columns.table, translations.urlLabel());
    indexTable.addColumn(Columns.tableLastUpdate, translations.nameLabel());
    indexTable.addColumn(Columns.indexLastUpdate, translations.urlLabel());
    indexTable.addColumn(Columns.scheduleType, translations.nameLabel());
    indexTable.addColumn(Columns.status, translations.urlLabel());

    indexTable.addColumn(actionsColumn, translations.actionsLabel());
    indexTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public HasActionHandler<TableIndexStatusDto> getActions() {
    return actionsColumn;
  }

  @Override
  public HasData<TableIndexStatusDto> getIndexTable() {
    return indexTable;
  }

  private static final class Columns {

    static Column<TableIndexStatusDto, String> datasource = new TextColumn<TableIndexStatusDto>() {

      @Override
      public String getValue(TableIndexStatusDto object) {
        return object.getDatasource();
      }
    };

    static Column<TableIndexStatusDto, String> table = new TextColumn<TableIndexStatusDto>() {

      @Override
      public String getValue(TableIndexStatusDto object) {
        return object.getTable();
      }
    };

    static Column<TableIndexStatusDto, String> tableLastUpdate = new TextColumn<TableIndexStatusDto>() {

      @Override
      public String getValue(TableIndexStatusDto object) {
        return object.getTableLastUpdate();
      }
    };

    static Column<TableIndexStatusDto, String> indexLastUpdate = new TextColumn<TableIndexStatusDto>() {

      @Override
      public String getValue(TableIndexStatusDto object) {
        return object.getIndexLastUpdate();
      }
    };

    static Column<TableIndexStatusDto, String> scheduleType = new TextColumn<TableIndexStatusDto>() {

      @Override
      public String getValue(TableIndexStatusDto object) {
        return object.getSchedule().getType().toString();
      }
    };

    static Column<TableIndexStatusDto, String> status = new TextColumn<TableIndexStatusDto>() {

      @Override
      public String getValue(TableIndexStatusDto object) {
        if(object.getProgress() > 0) {
          return "<a href=''>assad</a>";
        }
        return "";
      }
    };

//
//    static Column<Opal.TableIndexStatusDto, String> driver = new TextColumn<Opal.TableIndexStatusDto>() {
//
//      @Override
//      public String getValue(Opal.TableIndexStatusDto object) {
//        return object.getDriverClass();
//      }
//    };
//
//    static Column<Opal.TableIndexStatusDto, String> username = new TextColumn<Opal.TableIndexStatusDto>() {
//
//      @Override
//      public String getValue(Opal.TableIndexStatusDto object) {
//        return object.getUsername();
//      }
//    };

  }

//  @Override
//  public HasAuthorization getPermissionsAuthorizer() {
//    return new WidgetAuthorizer(permissionsPanel);
//  }
}
