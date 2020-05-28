/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.view;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.*;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

import java.util.List;

import static org.obiba.opal.web.model.client.opal.ScheduleType.*;
import static org.obiba.opal.web.model.client.opal.TableIndexationStatus.*;

public class IndexAdministrationView extends ViewWithUiHandlers<IndexAdministrationUiHandlers>
    implements IndexAdministrationPresenter.Display {

  private final TranslationMessages translationMessages;

  interface Binder extends UiBinder<Widget, IndexAdministrationView> {
  }

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Button startStopButton;

  @UiField
  Button enableButton;

  @UiField
  Button configureButton;

  @UiField
  Button refreshIndicesButton;

  @UiField
  Controls filterControls;

  @UiField
  TextBoxClearable filter;

  @UiField
  OpalSimplePager indexTablePager;

  @UiField
  Panel indicesPanel;

  @UiField
  Panel indexPanel;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Alert selectAllAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  Table<TableIndexStatusDto> indexTable;

  @UiField
  Panel breadcrumbs;

  @UiField
  IconAnchor indexNow;

  @UiField
  IconAnchor deleteIndex;

  @UiField
  IconAnchor scheduleIndex;

  private final PlaceManager placeManager;

  private final ListDataProvider<TableIndexStatusDto> dataProvider = new ListDataProvider<TableIndexStatusDto>();

  private final CheckboxColumn<TableIndexStatusDto> checkboxColumn;

  private Status status;

  private boolean enabled;

  private List<TableIndexStatusDto> originalIndices;

  @Inject
  public IndexAdministrationView(Binder uiBinder, PlaceManager placeManager, TranslationMessages translationMessages) {
    this.placeManager = placeManager;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    filter.getTextBox().setPlaceholder(translations.filterTables());
    indexTablePager.setDisplay(indexTable);
    checkboxColumn = new CheckboxColumn<TableIndexStatusDto>(new TableIndexStatusDtoDisplay());
    ActionsIndexColumn<TableIndexStatusDto> actionsColumn = new ActionsIndexColumn<TableIndexStatusDto>(
        new ActionsProvider<TableIndexStatusDto>() {

          private final String[] all = new String[]{REMOVE_ACTION, INDEX_ACTION};

          @Override
          public String[] allActions() {
            return all;
          }

          @Override
          public String[] getActions(TableIndexStatusDto value) {
            return allActions();
          }
        }
    );

    indexTable.addColumn(checkboxColumn, checkboxColumn.getCheckColumnHeader());
    indexTable.addColumn(new DatasourceColumn(), translations.projectLabel());
    indexTable.addColumn(new TableColumn(), translations.tableLabel());
    indexTable.addColumn(new TableLastUpdateColumn(), translations.tableLastUpdateLabel());
    indexTable.addColumn(new IndexLastUpdateColumn(), translations.indexLastUpdateLabel());
    indexTable.addColumn(new ScheduleTypeColumn(), translations.scheduleLabel());
    indexTable.addColumn(new StatusColumn(), translations.statusLabel());
    indexTable.addColumn(actionsColumn, translations.actionsLabel());
    indexTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    indexTable.setColumnWidth(checkboxColumn, 1, Style.Unit.PX);

    dataProvider.addDataDisplay(indexTable);

    actionsColumn.setActionHandler(new ActionHandler<TableIndexStatusDto>() {
      @Override
      public void doAction(TableIndexStatusDto statusDto, String actionName) {
        if (actionName.trim().equalsIgnoreCase(REMOVE_ACTION)) {
          getUiHandlers().delete(Lists.newArrayList(statusDto));
        } else if (actionName.trim().equalsIgnoreCase(INDEX_ACTION)) {
          getUiHandlers().indexNow(Lists.newArrayList(statusDto));
        }
      }
    });
  }

  @UiHandler("startStopButton")
  public void onStartStop(ClickEvent event) {
    if (status == Status.Startable) getUiHandlers().start();
    else getUiHandlers().stop();
  }

  @UiHandler("enableButton")
  public void onSuspendResume(ClickEvent event) {
    if (enabled) getUiHandlers().suspend();
    else getUiHandlers().resume();
  }

  @UiHandler("refreshIndicesButton")
  public void onRefresh(ClickEvent event) {
    getUiHandlers().refresh();
  }

  @UiHandler("removeValuesIndexButton")
  public void onValuesRemove(ClickEvent event) {
    getUiHandlers().removeIndices("values");
  }

  @UiHandler("removeVariablesIndexButton")
  public void onVariablesRemove(ClickEvent event) {
    getUiHandlers().removeIndices("variables");
  }

  @UiHandler("filter")
  public void onFilterUpdate(KeyUpEvent event) {
    renderTableIndices(filterTableIndices(filter.getText()));
  }

  private List<TableIndexStatusDto> filterTableIndices(String text) {
    List<TableIndexStatusDto> indices = Lists.newArrayList();
    if (originalIndices == null) return indices;
    List<String> tokens = FilterHelper.tokenize(text);
    for (TableIndexStatusDto index : originalIndices) {
      String indexText = Joiner.on(" ").join(index.getDatasource(), index.getTable(), index.getStatus().getName());
      if (FilterHelper.matches(indexText, tokens)) indices.add(index);
    }
    return indices;
  }

  @UiHandler("configureButton")
  public void onConfigure(ClickEvent event) {
    getUiHandlers().configure();
  }

  @UiHandler("deleteIndex")
  public void onDelete(ClickEvent event) {
    getUiHandlers().delete(checkboxColumn.getSelectedItems());
  }

  @UiHandler("scheduleIndex")
  public void onSchedule(ClickEvent event) {
    getUiHandlers().schedule(checkboxColumn.getSelectedItems());
  }

  @UiHandler("indexNow")
  public void onIndexNow(ClickEvent event) {
    getUiHandlers().indexNow(checkboxColumn.getSelectedItems());
  }

  @Override
  public void showTableIndices(List<TableIndexStatusDto> indices) {
    originalIndices = indices;
    renderTableIndices(indices);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void clear() {
    showTableIndices(null);
    checkboxColumn.clearSelection();
    selectAllAlert.setVisible(false);
  }

  @Override
  public void setServiceStatus(Status status) {
    this.status = status;
    switch (status) {
      case Startable:
        startStopButton.setText(translations.startLabel());
        enableStart(true);
        enableActions(false);
        break;
      case Stoppable:
        startStopButton.setText(translations.stopLabel());
        enableStart(true);
        enableActions(true);
        break;
      case Pending:
        enableStart(false);
        enableActions(false);
        break;
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    enableButton.setText(enabled ? translations.suspendLabel() : translations.resumeLabel());
    indicesPanel.setVisible(enabled);
  }

  @Override
  public List<TableIndexStatusDto> getSelectedIndices() {
    return checkboxColumn.getSelectedItems();
  }

  @Override
  public void unselectIndex(TableIndexStatusDto object) {
    checkboxColumn.setSelected(object, false);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  private void enableStart(boolean enable) {
    startStopButton.setEnabled(enable);
    configureButton.setEnabled(enable);
  }

  private void enableActions(boolean enable) {
    refreshIndicesButton.setEnabled(enable);
  }

  private void renderTableIndices(List<TableIndexStatusDto> indices) {
    checkboxColumn.clearSelection();
    dataProvider.setList(indices);
    indexTablePager.firstPage();
    dataProvider.refresh();
    indexTablePager.setPagerVisible(dataProvider.getList().size() > Table.DEFAULT_PAGESIZE);
  }

  private class TableIndexStatusDtoDisplay implements CheckboxColumn.Display<TableIndexStatusDto> {

    @Override
    public Table<TableIndexStatusDto> getTable() {
      return indexTable;
    }

    @Override
    public Object getItemKey(TableIndexStatusDto item) {
      return item == null ? null : item.getDatasource() + "." + item.getTable();
    }

    @Override
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public HasText getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public void selectAllItems(CheckboxColumn.ItemSelectionHandler<TableIndexStatusDto> handler) {
      for (TableIndexStatusDto item : dataProvider.getList())
        handler.onItemSelection(item);
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nIndicesLabel(nb).toLowerCase();
    }

    @Override
    public Alert getSelectActionsAlert() {
      return selectAllAlert;
    }

    @Override
    public Alert getSelectTipsAlert() {
      return selectItemTipsAlert;
    }
  }

  private class DatasourceColumn extends Column<TableIndexStatusDto, String> {
    private DatasourceColumn() {
      super(new PlaceRequestCell<String>(placeManager) {
        @Override
        public PlaceRequest getPlaceRequest(String value) {
          return ProjectPlacesHelper.getProjectPlace(value);
        }
      });
    }

    @Override
    public String getValue(TableIndexStatusDto object) {
      return object.getDatasource();
    }
  }

  private class TableColumn extends Column<TableIndexStatusDto, TableIndexStatusDto> {

    private TableColumn() {
      super(new PlaceRequestCell<TableIndexStatusDto>(placeManager) {
        @Override
        public PlaceRequest getPlaceRequest(TableIndexStatusDto value) {
          return ProjectPlacesHelper.getTablePlace(value.getDatasource(), value.getTable());
        }

        @Override
        public String getText(TableIndexStatusDto value) {
          return value.getTable();
        }
      });
    }

    @Override
    public TableIndexStatusDto getValue(TableIndexStatusDto object) {
      return object;
    }
  }

  private static class TableLastUpdateColumn extends TextColumn<TableIndexStatusDto> {

    @Override
    public String getValue(TableIndexStatusDto object) {
      return ValueRenderer.DATETIME.render(object.getTableLastUpdate());
    }
  }

  private static class IndexLastUpdateColumn extends TextColumn<TableIndexStatusDto> {

    @Override
    public String getValue(TableIndexStatusDto object) {
      return object.getIndexLastUpdate().isEmpty() ? "-" : ValueRenderer.DATETIME.render(object.getIndexLastUpdate());
    }
  }

  private static class ScheduleTypeColumn extends TextColumn<TableIndexStatusDto> {

    @Override
    public String getValue(TableIndexStatusDto object) {
      if (object.getSchedule().getType().getName().equals(NOT_SCHEDULED.getName())) {
        return translations.manuallyLabel();
      }
      if (object.getSchedule().getType().getName().equals(MINUTES_5.getName())) {
        return translations.minutes5Label();
      }
      if (object.getSchedule().getType().getName().equals(MINUTES_15.getName())) {
        return translations.minutes15Label();
      }
      if (object.getSchedule().getType().getName().equals(MINUTES_30.getName())) {
        return translations.minutes30Label();
      }
      String minutes = object.getSchedule().getMinutes() < 10
          ? "0" + object.getSchedule().getMinutes()
          : String.valueOf(object.getSchedule().getMinutes());
      if (object.getSchedule().getType().getName().equals(HOURLY.getName())) {
        return translations.hourlyAtLabel().replace("{0}", minutes);
      }
      if (object.getSchedule().getType().getName().equals(DAILY.getName())) {
        return translations.dailyAtLabel().replace("{0}", Integer.toString(object.getSchedule().getHours()))
            .replace("{1}", minutes);
      }
      if (object.getSchedule().getType().getName().equals(WEEKLY.getName())) {
        return translations.weeklyAtLabel()
            .replace("{0}", translations.timeMap().get(object.getSchedule().getDay().getName()))
            .replace("{1}", Integer.toString(object.getSchedule().getHours())).replace("{2}", minutes);
      }

      return object.getSchedule().getType().toString();
    }
  }

  private static class StatusColumn extends Column<TableIndexStatusDto, String> {

    private StatusColumn() {
      super(new StatusImageCell());
    }

    @Override
    public String getValue(TableIndexStatusDto dto) {
      // In progress
      if (dto.getStatus().getName().equals(IN_PROGRESS.getName())) {
        return translations.indexInProgress() + ":" + (int) (dto.getProgress() * 100) + "%";
      }
      // Up to date
      if (dto.getStatus().getName().equals(UPTODATE.getName())) {
        return translations.indexUpToDate() + ":" + StatusImageCell.BULLET_GREEN;
      }
      // Out dated but scheduled
      if (dto.getStatus().getName().equals(OUTDATED.getName()) &&
          !dto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)) {
        return translations.indexOutdatedScheduled() + ":" + StatusImageCell.BULLET_ORANGE;
      }
      // Out dated but not scheduled
      if (dto.getStatus().getName().equals(OUTDATED.getName()) &&
          dto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)) {
        return translations.indexOutdatedNotScheduled() + ":" + StatusImageCell.BULLET_RED;
      }
      // Unknown status
      return translations.indexNotScheduled() + ":" + StatusImageCell.BULLET_BLACK;
    }
  }
}
