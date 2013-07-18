/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.job.view;

import java.util.Date;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter.Display;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.DateTimeColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.WorkbenchLayout;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter.CANCEL_ACTION;
import static org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter.LOG_ACTION;

/**
 *
 */
public class JobListView extends Composite implements Display {

  @UiTemplate("JobListView.ui.xml")
  interface JobListViewUiBinder extends UiBinder<WorkbenchLayout, JobListView> {}

  private static final JobListViewUiBinder uiBinder = GWT.create(JobListViewUiBinder.class);

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

  @UiField
  Panel breadcrumbs;

  ListDataProvider<CommandStateDto> dataProvider;

  private static final Translations translations = GWT.create(Translations.class);

  private ActionsColumn<CommandStateDto> actionsColumn;

  //
  // Constructors
  //

  public JobListView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
  }

  //
  // JobListPresenter.Display Methods
  //

  @Nullable
  @Override
  public SelectionModel<CommandStateDto> getTableSelection() {
    return null;
  }

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
        return object.getOwner();
      }
    }, translations.userLabel());

    table.addColumn(new DateTimeColumn<CommandStateDto>() {
      @Override
      public Date getValue(CommandStateDto object) {
        return object.getStartTime() > 0 ? new Date((long) object.getStartTime()) : null;
      }
    }, translations.startLabel());

    table.addColumn(new DateTimeColumn<CommandStateDto>() {
      @Override
      public Date getValue(CommandStateDto object) {
        return object.getEndTime() > 0 ? new Date((long) object.getEndTime()) : null;
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

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

}
