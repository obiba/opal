/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.r.list;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.*;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.r.RSessionDto;
import org.obiba.opal.web.model.client.opal.r.RSessionStatus;

/**
 *
 */
public class RSessionsView extends ViewWithUiHandlers<RSessionsUiHandlers> implements RSessionsPresenter.Display {

  interface Binder extends UiBinder<Widget, RSessionsView> {
  }

  @UiField
  InlineLabel noRSessions;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  Table<RSessionDto> table;

  @UiField
  OpalSimplePager pager;

  ListDataProvider<RSessionDto> dataProvider;

  private final TranslationMessages translationMessages;

  private static final Translations translations = GWT.create(Translations.class);

  private ActionsColumn<RSessionDto> actionsColumn;

  private CheckboxColumn<RSessionDto> checkColumn;

  //
  // Constructors
  //

  @Inject
  public RSessionsView(Binder uiBinder, TranslationMessages translationMessages) {
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // RSessionsPresenter.Display Methods
  //

  @Override
  public void renderRows(JsArray<RSessionDto> rows) {
    dataProvider.setList(JsArrays.toList(rows));
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @UiHandler("refreshButton")
  void onRefresh(ClickEvent event) {
    getUiHandlers().onRefresh();
  }

  @UiHandler("deleteSessions")
  void onDeleteSessions(ClickEvent event) {
    getUiHandlers().onTerminate(checkColumn.getSelectedItems());
    checkColumn.clearSelection();
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
    checkColumn = new CheckboxColumn<RSessionDto>(new RSessionCheckStatusDisplay());
    table.addColumn(checkColumn, checkColumn.getCheckColumnHeader());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return String.valueOf(object.getId());
      }
    }, translations.idLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return object.getProfile();
      }
    }, translations.profileLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return object.getCluster();
      }
    }, translations.clusterLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return object.getServer();
      }
    }, translations.rServerLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return object.getContext();
      }
    }, translations.contextLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        return object.getUser();
      }
    }, translations.userLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        if (!object.hasCreationDate()) return "-";
        return Moment.create(object.getCreationDate()).format(FormatType.MONTH_NAME_TIME_SHORT);
      }
    }, translations.startLabel());

    table.addColumn(new TextColumn<RSessionDto>() {
      @Override
      public String getValue(RSessionDto object) {
        if (!object.hasLastAccessDate()) return "-";
        return Moment.create(object.getLastAccessDate()).fromNow();
      }
    }, translations.lastAccessLabel());

    table.addColumn(new StatusColumn(), translations.statusLabel());

    actionsColumn = new ActionsColumn<RSessionDto>(new ActionsProvider<RSessionDto>() {

      @Override
      public String[] allActions() {
        return new String[]{RSessionsPresenter.TERMINATE_ACTION};
      }

      @Override
      public String[] getActions(RSessionDto value) {
        return allActions();
      }

    });

    actionsColumn.setActionHandler(new ActionHandler<RSessionDto>() {
      @Override
      public void doAction(RSessionDto dto, String actionName) {
        if (actionName != null && RSessionsPresenter.TERMINATE_ACTION.equals(actionName)) {
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

    private StatusColumn() {
      super(new StatusImageCell());
    }

    @Override
    public String getValue(RSessionDto dto) {
      // Waiting
      if (dto.getStatus().getName().equals(RSessionStatus.WAITING.getName())) {
        return translations.statusMap().get(RSessionStatus.WAITING.getName()) + "::" +
            StatusImageCell.BULLET_GREEN;
      }
      // Busy
      if (dto.getStatus().getName().equals(RSessionStatus.BUSY.getName())) {
        return translations.statusMap().get(RSessionStatus.BUSY.getName()) + "::" +
            StatusImageCell.BULLET_ORANGE;
      }
      // Other
      return "?::" + StatusImageCell.BULLET_BLACK;
    }
  }

  private class RSessionCheckStatusDisplay implements CheckboxColumn.Display<RSessionDto> {

    @Override
    public Table<RSessionDto> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(RSessionDto item) {
      return item.getId();
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
    public void selectAllItems(CheckboxColumn.ItemSelectionHandler<RSessionDto> handler) {
      for (RSessionDto session : dataProvider.getList())
        handler.onItemSelection(session);
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nTablesLabel(nb).toLowerCase();
    }

    @Override
    public Alert getSelectActionsAlert() {
      return selectAllItemsAlert;
    }

    @Override
    public Alert getSelectTipsAlert() {
      return selectItemTipsAlert;
    }
  }
}
