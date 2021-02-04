/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.r.list;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.ValueRenderingHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.r.RWorkspaceDto;

/**
 *
 */
public class RWorkspacesView extends ViewWithUiHandlers<RWorkspacesUiHandlers> implements RWorkspacesPresenter.Display {

  interface Binder extends UiBinder<Widget, RWorkspacesView> {}

  @UiField
  InlineLabel noRWorkspaces;

  @UiField
  CellTable<RWorkspaceDto> table;

  @UiField
  Button refreshButton;

  @UiField
  OpalSimplePager pager;

  ListDataProvider<RWorkspaceDto> dataProvider;

  private static final Translations translations = GWT.create(Translations.class);

  private ActionsColumn<RWorkspaceDto> actionsColumn;

  private final PlaceManager placeManager;

  //
  // Constructors
  //

  @Inject
  public RWorkspacesView(Binder uiBinder, PlaceManager placeManager) {
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // RWorkspacesPresenter.Display Methods
  //

  @Override
  public void renderRows(JsArray<RWorkspaceDto> rows) {
    pager.firstPage();
    dataProvider.setList(JsArrays.toList(rows));
    dataProvider.refresh();
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
    table.setEmptyTableWidget(noRWorkspaces);
    addTableColumns();
    addTablePager();

    dataProvider = new ListDataProvider<RWorkspaceDto>();
    dataProvider.addDataDisplay(table);
  }

  private void addTableColumns() {
    table.addColumn(new TextColumn<RWorkspaceDto>() {
      @Override
      public String getValue(RWorkspaceDto object) {
        return String.valueOf(object.getName());
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<RWorkspaceDto>() {
      @Override
      public String getValue(RWorkspaceDto object) {
        return object.getContext();
      }
    }, translations.contextLabel());

    table.addColumn(new TextColumn<RWorkspaceDto>() {
      @Override
      public String getValue(RWorkspaceDto object) {
        return object.getUser();
      }
    }, translations.userLabel());

    table.addColumn(new TextColumn<RWorkspaceDto>() {
      @Override
      public String getValue(RWorkspaceDto object) {
        return ValueRenderingHelper
            .getSizeInBytes(object.getSize());
      }
    }, translations.sizeLabel());

    table.addColumn(new TextColumn<RWorkspaceDto>() {
      @Override
      public String getValue(RWorkspaceDto object) {
        if(!object.hasLastAccessDate()) return "-";
        return Moment.create(object.getLastAccessDate()).fromNow();
      }
    }, translations.lastAccessLabel());

    actionsColumn = new ActionsColumn<RWorkspaceDto>(new ActionsProvider<RWorkspaceDto>() {

      @Override
      public String[] allActions() {
        return new String[] { RWorkspacesPresenter.REMOVE_ACTION };
      }

      @Override
      public String[] getActions(RWorkspaceDto value) {
        return allActions();
      }

    });

    actionsColumn.setActionHandler(new ActionHandler<RWorkspaceDto>() {
      @Override
      public void doAction(RWorkspaceDto dto, String actionName) {
        if(actionName != null && RWorkspacesPresenter.REMOVE_ACTION.equals(actionName)) {
          getUiHandlers().onRemove(dto);
        }
      }
    });

    table.addColumn(actionsColumn, translations.actionsLabel());
  }

  private void addTablePager() {
    table.setPageSize(20);
    pager.setDisplay(table);
  }

}
