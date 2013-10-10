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
import org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter.Display;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.datetime.client.Duration;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewImpl;

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
  SimplePager pager;

  ListDataProvider<CommandStateDto> dataProvider;

  private static final Translations translations = GWT.create(Translations.class);

  private ActionsColumn<CommandStateDto> actionsColumn;

  //
  // Constructors
  //

  @Inject
  public TasksView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // TasksPresenter.Display Methods
  //

  @Override
  public void renderRows(JsArray<CommandStateDto> rows) {
    pager.setVisible(rows.length() > 50); // OPAL-901
    pager.firstPage();
    dataProvider.setList(JsArrays.toList(rows));
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
    if (b && table.getColumnCount() == 8) table.removeColumn(2);
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
        return object.getCommand();
      }
    }, translations.typeLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getProject();
      }
    }, translations.projectLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getOwner();
      }
    }, translations.userLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        if (!object.hasStartTime()) return "-";
        return Moment.create(object.getStartTime()).format(FormatType.MONTH_NAME_TIME_SHORT);
      }
    }, translations.startLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        if (!object.hasEndTime()) return "-";
        Moment end = Moment.create(object.getEndTime());
        Moment start = Moment.create(object.getStartTime());
        return end.format(FormatType.MONTH_NAME_TIME_SHORT) + " [" + Duration.create(start,end).humanize() + "]";
      }
    }, translations.endLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        String status = object.getStatus();
        if(translations.statusMap().containsKey(status)) {
          return translations.statusMap().get(status);
        }
        if(translations.userMessageMap().containsKey(status)) {
          return translations.userMessageMap().get(status);
        }
        return status;
      }
    }, translations.statusLabel());

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

}
