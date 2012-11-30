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

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsIndexColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.client.opal.ScheduleType;
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
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;

import static org.obiba.opal.web.model.client.opal.ScheduleType.*;
import static org.obiba.opal.web.model.client.opal.TableIndexationStatus.*;

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

    ActionsIndexColumn<TableIndexStatusDto> actionsColumn = new ActionsIndexColumn<TableIndexStatusDto>(
      new ActionsProvider<TableIndexStatusDto>() {

        private final String[] all = new String[] {INDEX_ACTION, CLEAR_ACTION, CANCEL_ACTION};

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
    indexTable.addColumn(Columns.datasource, translations.datasourceLabel());
    indexTable.addColumn(Columns.table, translations.tableLabel());
    indexTable.addColumn(Columns.tableLastUpdate, translations.tableLastUpdateLabel());
    indexTable.addColumn(Columns.indexLastUpdate, translations.indexLastUpdateLabel());
    indexTable.addColumn(Columns.scheduleType, translations.scheduleLabel());
    indexTable.addColumn(Columns.status, translations.statusLabel());
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
          if (object.getSchedule().getType().getName().equals(NOT_SCHEDULED.getName())){
              return "Manual";
          }
          if (object.getSchedule().getType().getName().equals(MINUTES_5.getName())){
              return "Every 5 minutes";
          }
          if (object.getSchedule().getType().getName().equals(MINUTES_15.getName())){
              return "Every 15 minutes";
          }
          if (object.getSchedule().getType().getName().equals(MINUTES_30.getName())){
              return "Every 30 minutes";
          }
          String minutes = object.getSchedule().getMinutes() < 10 ? "0" + object.getSchedule().getMinutes() : String.valueOf(object.getSchedule().getMinutes());
          if (object.getSchedule().getType().getName().equals(HOURLY.getName())){
              return "Every hour at " + minutes + " minutes";
          }
          if (object.getSchedule().getType().getName().equals(DAILY.getName())){
              return "Every day at " + object.getSchedule().getHours() + ":" + minutes;
          }
          if (object.getSchedule().getType().getName().equals(WEEKLY.getName())){
              return "Every week on " + object.getSchedule().getDay().getName().toLowerCase() + " at " + object.getSchedule().getHours() + ":" + minutes;
          }

        return object.getSchedule().getType().toString();
      }
    };

    static Column<TableIndexStatusDto, String> status = new Column<TableIndexStatusDto, String>(new ImageCell()) {

        @Override
        public String getValue(TableIndexStatusDto tableIndexStatusDto) {
            // Up to date: green
            if (tableIndexStatusDto.getStatus().getName().equals(UPTODATE.getName())){
                return "image/16/bullet_green.png";
            }
            // Out dated but scheduled
            if (tableIndexStatusDto.getStatus().getName().equals(OUTDATED.getName()) &&
                    !tableIndexStatusDto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)){
                return "image/16/bullet_orange.png";
            }
            // out dated but not scheduled
            if (tableIndexStatusDto.getStatus().getName().equals(OUTDATED.getName()) &&
                    tableIndexStatusDto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)){
                return "image/16/bullet_red.png";
            }
            // notify() scheduled
            if (tableIndexStatusDto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)){
                return "image/16/bullet_black.png";
            }

            // When in progress...
            return "image/in-progress.gif";
        }
    };
  }

//  @Override
//  public HasAuthorization getPermissionsAuthorizer() {
//    return new WidgetAuthorizer(permissionsPanel);
//  }
}
