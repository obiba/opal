/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.task.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter.Display;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.app.client.ui.celltable.StatusImageCell;
import org.obiba.opal.web.gwt.datetime.client.Duration;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter.CANCEL_ACTION;
import static org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter.LOG_ACTION;

/**
 *
 */
public class TasksView extends ViewImpl implements Display {

  interface Binder extends UiBinder<Widget, TasksView> {}

  @UiField
  InlineLabel noJobs;

  @UiField
  CellTable<CommandStateDto> table;

  @UiField
  Button refreshButton;

  @UiField
  Button clearButton;

  @UiField
  OpalSimplePager pager;

  ListDataProvider<CommandStateDto> dataProvider;

  private static final Translations translations = GWT.create(Translations.class);

  private ActionsColumn<CommandStateDto> actionsColumn;

  private final PlaceManager placeManager;

  //
  // Constructors
  //

  @Inject
  public TasksView(Binder uiBinder, PlaceManager placeManager) {
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // TasksPresenter.Display Methods
  //

  @Override
  public void renderRows(JsArray<CommandStateDto> rows) {
    pager.firstPage();
    dataProvider.setList(JsArrays.toList(rows));
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @Override
  public void showClearJobsButton(boolean show) {
    clearButton.setEnabled(show);
  }

  @Override
  public HasActionHandler<CommandStateDto> getActionsColumn() {
    return actionsColumn;
  }

  @Override
  public HandlerRegistration addClearButtonHandler(ClickHandler handler) {
    return clearButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addRefreshButtonHandler(ClickHandler handler) {
    return refreshButton.addClickHandler(handler);
  }

  @Override
  public void inProject(boolean b) {
    if(b && table.getColumnCount() == 8) table.removeColumn(2);
  }

  //
  // Methods
  //

  private void initTable() {
    table.setEmptyTableWidget(noJobs);
    addTableColumns();
    addTablePager();

    dataProvider = new ListDataProvider<CommandStateDto>();
    dataProvider.addDataDisplay(table);
  }

  private void addTableColumns() {
    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return String.valueOf(object.getId());
      }
    }, translations.idLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getName();
      }
    }, translations.typeLabel());

    table.addColumn(new ProjectColumn(), translations.projectLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getOwner();
      }
    }, translations.userLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        if(!object.hasStartTime()) return "-";
        return Moment.create(object.getStartTime()).format(FormatType.MONTH_NAME_TIME_SHORT);
      }
    }, translations.startLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        if(!object.hasEndTime()) return "-";
        Moment end = Moment.create(object.getEndTime());
        Moment start = Moment.create(object.getStartTime());
        return end.format(FormatType.MONTH_NAME_TIME_SHORT) + " [" + Duration.create(start, end).humanize() + "]";
      }
    }, translations.endLabel());

    table.addColumn(new StatusColumn(), translations.statusLabel());

    actionsColumn = new ActionsColumn<CommandStateDto>(new ActionsProvider<CommandStateDto>() {

      @Override
      public String[] allActions() {
        return new String[] { LOG_ACTION, CANCEL_ACTION };
      }

      @Override
      public String[] getActions(CommandStateDto value) {
        return "NOT_STARTED".equals(value.getStatus()) || "IN_PROGRESS".equals(value.getStatus()) ? new String[] {
            LOG_ACTION, CANCEL_ACTION } : new String[] { LOG_ACTION };
      }
    });
    table.addColumn(actionsColumn, translations.actionsLabel());
  }

  private void addTablePager() {
    table.setPageSize(50);
    pager.setDisplay(table);
  }

  private class ProjectColumn extends Column<CommandStateDto, String> {
    private ProjectColumn() {
      super(new PlaceRequestCell<String>(placeManager) {
        @Override
        public PlaceRequest getPlaceRequest(String value) {
          return ProjectPlacesHelper.getProjectPlace(value);
        }
      });
    }

    @Override
    public String getValue(CommandStateDto object) {
      return object.getProject();
    }
  }

  private static class StatusColumn extends Column<CommandStateDto, String> {

    private StatusColumn() {super(new StatusImageCell());}

    @Override
    public String getValue(CommandStateDto dto) {
      // In progress
      if(dto.getStatus().equals(CommandStateDto.Status.IN_PROGRESS.getName())) {
        if(dto.hasProgress()) return dto.getProgress().getMessage() + ":" + dto.getProgress().getPercent() + "%";
        else return translations.statusMap().get(CommandStateDto.Status.IN_PROGRESS.getName()) + ":" +
            StatusImageCell.BULLET_BLACK;
      }
      // Success
      if(dto.getStatus().equals(CommandStateDto.Status.SUCCEEDED.getName())) {
        return translations.statusMap().get(CommandStateDto.Status.SUCCEEDED.getName()) + ":" +
            StatusImageCell.BULLET_GREEN;
      }
      // Failed
      if(dto.getStatus().equals(CommandStateDto.Status.FAILED.getName())) {
        return translations.statusMap().get(CommandStateDto.Status.FAILED.getName()) + ":" +
            StatusImageCell.BULLET_RED;
      }
      // Cancelled
      if(dto.getStatus().equals(CommandStateDto.Status.CANCELED.getName())) {
        return translations.statusMap().get(CommandStateDto.Status.CANCELED.getName()) + ":" +
            StatusImageCell.BULLET_ORANGE;
      }
      // Cancelled pending
      if(dto.getStatus().equals(CommandStateDto.Status.CANCEL_PENDING.getName())) {
        return translations.statusMap().get(CommandStateDto.Status.CANCEL_PENDING.getName()) + ":" +
            StatusImageCell.BULLET_BLACK;
      }
      // Other
      return translations.statusMap().get(CommandStateDto.Status.NOT_STARTED.getName()) + ":" +
          StatusImageCell.BULLET_BLACK;
    }
  }
}
