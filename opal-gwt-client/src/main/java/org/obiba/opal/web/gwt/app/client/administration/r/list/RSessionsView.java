/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.r.list;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.StatusImageCell;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.r.RSessionDto;
import org.obiba.opal.web.model.client.opal.r.RSessionStatus;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 *
 */
public class RSessionsView extends ViewWithUiHandlers<RSessionsUiHandlers> implements RSessionsPresenter.Display {

  interface Binder extends UiBinder<Widget, RSessionsView> {}

  @UiField
  InlineLabel noRSessions;

  @UiField
  CellTable<RSessionDto> table;

  @UiField
  Button refreshButton;

  @UiField
  OpalSimplePager pager;

  ListDataProvider<RSessionDto> dataProvider;

  private static final Translations translations = GWT.create(Translations.class);

  private ActionsColumn<RSessionDto> actionsColumn;

  private final PlaceManager placeManager;

  //
  // Constructors
  //

  @Inject
  public RSessionsView(Binder uiBinder, PlaceManager placeManager) {
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // RSessionsPresenter.Display Methods
  //

  @Override
  public void renderRows(JsArray<RSessionDto> rows) {
    pager.firstPage();
    dataProvider.setList(JsArrays.toList(rows));
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @UiHandler("refreshButton")
  void onRefresh(ClickEvent event) {
    getUiHandlers().onRefresh();
  }

  //
  // Methods
  //

  private void initTable() {
    table.setEmptyTableWidget(noRSessions);
    addTableColumns();
    addTablePager();

    dataProvider = new ListDataProvider<RSessionDto>();
    dataProvider.addDataDisplay(table);
  }

  private void addTableColumns() {
    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return String.valueOf(object.getId());
      }
    }, translations.idLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return object.getUser();
      }
    }, translations.userLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        if(!object.hasCreationDate()) return "-";
        return Moment.create(object.getCreationDate()).format(FormatType.MONTH_NAME_TIME_SHORT);
      }
    }, translations.startLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        if(!object.hasLastAccessDate()) return "-";
        return Moment.create(object.getLastAccessDate()).fromNow();
      }
    }, translations.lastAccessLabel());

    table.addColumn(new StatusColumn(), translations.statusLabel());

    actionsColumn = new ActionsColumn<RSessionDto>(new ActionsProvider<RSessionDto>() {

      @Override
      public String[] allActions() {
        return new String[] { RSessionsPresenter.TERMINATE_ACTION };
      }

      @Override
      public String[] getActions(RSessionDto value) {
        return allActions();
      }

    });

    actionsColumn.setActionHandler(new ActionHandler<RSessionDto>() {
      @Override
      public void doAction(RSessionDto dto, String actionName) {
        if(actionName != null && RSessionsPresenter.TERMINATE_ACTION.equals(actionName)) {
          getUiHandlers().onTerminate(dto);
        }
      }
    });

    table.addColumn(actionsColumn, translations.actionsLabel());
  }

  private void addTablePager() {
    table.setPageSize(20);
    pager.setDisplay(table);
  }

  private static class StatusColumn extends Column<RSessionDto, String> {

    private StatusColumn() {super(new StatusImageCell());}

    @Override
    public String getValue(RSessionDto dto) {
      // Waiting
      if(dto.getStatus().getName().equals(RSessionStatus.WAITING.getName())) {
        return translations.statusMap().get(RSessionStatus.WAITING.getName()) + ":" +
            StatusImageCell.BULLET_GREEN;
      }
      // Busy
      if(dto.getStatus().getName().equals(RSessionStatus.BUSY.getName())) {
        return translations.statusMap().get(RSessionStatus.BUSY.getName()) + ":" +
            StatusImageCell.BULLET_ORANGE;
      }
      // Other
      return "?:" + StatusImageCell.BULLET_BLACK;
    }
  }
}
