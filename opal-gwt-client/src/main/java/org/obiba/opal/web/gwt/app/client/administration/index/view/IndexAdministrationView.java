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
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsIndexColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.model.client.opal.ScheduleType.DAILY;
import static org.obiba.opal.web.model.client.opal.ScheduleType.HOURLY;
import static org.obiba.opal.web.model.client.opal.ScheduleType.MINUTES_15;
import static org.obiba.opal.web.model.client.opal.ScheduleType.MINUTES_30;
import static org.obiba.opal.web.model.client.opal.ScheduleType.MINUTES_5;
import static org.obiba.opal.web.model.client.opal.ScheduleType.NOT_SCHEDULED;
import static org.obiba.opal.web.model.client.opal.ScheduleType.WEEKLY;
import static org.obiba.opal.web.model.client.opal.TableIndexationStatus.OUTDATED;
import static org.obiba.opal.web.model.client.opal.TableIndexationStatus.UPTODATE;

public class IndexAdministrationView extends ViewImpl implements IndexAdministrationPresenter.Display {

  @UiTemplate("IndexAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, IndexAdministrationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  com.github.gwtbootstrap.client.ui.Button startButton;

  @UiField
  com.github.gwtbootstrap.client.ui.Button stopButton;

  @UiField
  com.github.gwtbootstrap.client.ui.Button refreshIndicesButton;

  @UiField
  com.github.gwtbootstrap.client.ui.DropdownButton actionsDropdown;

  @UiField
  SimplePager indexTablePager;

  @UiField
  Table<TableIndexStatusDto> indexTable;

  final static MultiSelectionModel<TableIndexStatusDto> selectedIndices = new MultiSelectionModel<TableIndexStatusDto>(
      new ProvidesKey<TableIndexStatusDto>() {
        @Override
        public Object getKey(TableIndexStatusDto item) {
          return item == null ? null : item.getTable();
        }
      });

  ActionsIndexColumn<TableIndexStatusDto> actionsColumn = new ActionsIndexColumn<TableIndexStatusDto>(
      new ActionsProvider<TableIndexStatusDto>() {

        private final String[] all = new String[] {CLEAR_ACTION};//{INDEX_ACTION, CLEAR_ACTION, CANCEL_ACTION};

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

    indexTable
        .setSelectionModel(selectedIndices, DefaultSelectionEventManager.<TableIndexStatusDto>createCheckboxManager());

    indexTable.addColumn(Columns.select, ""); //translations.allLabel()
    indexTable.addColumn(Columns.datasource, translations.datasourceLabel());
    indexTable.addColumn(Columns.table, translations.tableLabel());
    indexTable.addColumn(Columns.tableLastUpdate, translations.tableLastUpdateLabel());
    indexTable.addColumn(Columns.indexLastUpdate, translations.indexLastUpdateLabel());
    indexTable.addColumn(Columns.scheduleType, translations.scheduleLabel());
    indexTable.addColumn(Columns.status, translations.statusLabel());
    indexTable.addColumn(actionsColumn, translations.actionsLabel());
    indexTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
  }

  private boolean createCheckboxManager() {
    return false;
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public com.github.gwtbootstrap.client.ui.Button getStartButton() {
    return startButton;
  }

  @Override
  public com.github.gwtbootstrap.client.ui.Button getStopButton() {
    return stopButton;
  }

  @Override
  public HasClickHandlers getRefreshButton() {
    return refreshIndicesButton;
  }

  @Override
  public DropdownButton getActionsDropdown() {
    return actionsDropdown;
  }

  public void as() {

  }

  @Override
  public MultiSelectionModel getSelectedIndices() {
    return selectedIndices;
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

    static Column<TableIndexStatusDto, Boolean> select = new Column<TableIndexStatusDto, Boolean>(
        new CheckboxCell(true, false)) {
      @Override
      public Boolean getValue(TableIndexStatusDto object) {
        return selectedIndices.isSelected(object);
      }
    };

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
        return object.getIndexLastUpdate().isEmpty() ? "-" : object.getIndexLastUpdate();
      }
    };

    static Column<TableIndexStatusDto, String> scheduleType = new TextColumn<TableIndexStatusDto>() {

      @Override
      public String getValue(TableIndexStatusDto object) {
        if(object.getSchedule().getType().getName().equals(NOT_SCHEDULED.getName())) {
          return "Manual";
        }
        if(object.getSchedule().getType().getName().equals(MINUTES_5.getName())) {
          return "Every 5 minutes";
        }
        if(object.getSchedule().getType().getName().equals(MINUTES_15.getName())) {
          return "Every 15 minutes";
        }
        if(object.getSchedule().getType().getName().equals(MINUTES_30.getName())) {
          return "Every 30 minutes";
        }
        String minutes = object.getSchedule().getMinutes() < 10 ? "0" + object.getSchedule().getMinutes() : String
            .valueOf(object.getSchedule().getMinutes());
        if(object.getSchedule().getType().getName().equals(HOURLY.getName())) {
          return "Every hour at " + minutes + " minutes";
        }
        if(object.getSchedule().getType().getName().equals(DAILY.getName())) {
          return "Every day at " + object.getSchedule().getHours() + ":" + minutes;
        }
        if(object.getSchedule().getType().getName().equals(WEEKLY.getName())) {
          return "Every week on " + object.getSchedule().getDay().getName().toLowerCase() + " at " + object
              .getSchedule().getHours() + ":" + minutes;
        }

        return object.getSchedule().getType().toString();
      }
    };

    static Column<TableIndexStatusDto, String> status = new Column<TableIndexStatusDto, String>(new ImageCell()) {

      @Override
      public String getValue(TableIndexStatusDto tableIndexStatusDto) {
        // Up to date: green
        if(tableIndexStatusDto.getStatus().getName().equals(UPTODATE.getName())) {
          return "image/16/bullet_green.png";
        }
        // Out dated but scheduled
        if(tableIndexStatusDto.getStatus().getName().equals(OUTDATED.getName()) && !tableIndexStatusDto.getSchedule()
            .getType().isScheduleType(NOT_SCHEDULED)) {
          return "image/16/bullet_orange.png";
        }
        // out dated but not scheduled
        if(tableIndexStatusDto.getStatus().getName().equals(OUTDATED.getName()) && tableIndexStatusDto.getSchedule()
            .getType().isScheduleType(NOT_SCHEDULED)) {
          return "image/16/bullet_red.png";
        }
        // notify() scheduled
        if(tableIndexStatusDto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)) {
          return "image/16/bullet_black.png";
        }

        // When in progress...
        return "image/in-progress.gif";
      }
    };
  }

  /**
   * The key provider that provides the unique ID of a contact.
   */
//  public static final ProvidesKey<TableIndexStatusDto> KEY_PROVIDER =

//  @Override
//  public HasAuthorization getPermissionsAuthorizer() {
//    return new WidgetAuthorizer(permissionsPanel);
//  }
}
