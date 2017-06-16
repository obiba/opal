/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.web.gwt.app.client.cart;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;

import java.util.List;

public class CartVariableTable extends Table<String> {

  private CheckboxColumn<String> checkColumn;

  public CartVariableTable() {
  }

  public void initialize(PlaceManager placeManager, CartVariableCheckDisplay checkDisplay, ActionHandler<String> actionHandler) {
    while (getColumnCount() > 0) {
      removeColumn(0);
    }
    initColumns(placeManager, checkDisplay, actionHandler);
  }

  private void initColumns(PlaceManager placeManager, CartVariableCheckDisplay checkDisplay, ActionHandler<String> actionHandler) {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noVariablesLabel()));

    if (checkDisplay != null) {
      checkDisplay.setTable(this);
      addCheckColumn(checkDisplay);
    }

    if (placeManager != null) {
      addColumn(new VariableItemColumn(new VariableLinkCell(placeManager)), translations.variableLabel());
      addColumn(new VariableItemColumn(new ProjectTableLinkCell(placeManager)), translations.tableLabel());
    } else {
      addColumn(new TextColumn<String>() {
        @Override
        public String getValue(String item) {
          MagmaPath.Parser parser = MagmaPath.Parser.parse(item);
          return parser.getVariable();
        }
      }, translations.variableLabel());
      addColumn(new TextColumn<String>() {
        @Override
        public String getValue(String item) {
          MagmaPath.Parser parser = MagmaPath.Parser.parse(item);
          return parser.getDatasource() + "." + parser.getTable();
        }
      }, translations.tableLabel());
    }

    ActionsColumn<String> actionColumn = new ActionsColumn<String>(ActionsColumn.REMOVE_ACTION);
    if (actionHandler != null) actionColumn.setActionHandler(actionHandler);
    addColumn(actionColumn, translations.actionsLabel());
  }

  private void addCheckColumn(CheckboxColumn.Display<String> checkboxDisplay) {
    checkColumn = new CheckboxColumn<>(checkboxDisplay);
    addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    setColumnWidth(checkColumn, 1, com.google.gwt.dom.client.Style.Unit.PX);
  }

  public List<String> getSelectedItems() {
    return checkColumn.getSelectedItems();
  }

  private static class VariableItemColumn extends Column<String, String> {
    public VariableItemColumn(Cell<String> cell) {
      super(cell);
    }

    @Override
    public String getValue(String item) {
      return item;
    }
  }

  private static class ProjectTableLinkCell extends PlaceRequestCell<String> {

    private ProjectTableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(String item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item);
      return ProjectPlacesHelper.getTablePlace(parser.getDatasource(), parser.getTable());
    }

    @Override
    public String getText(String item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item);
      return parser.getDatasource() + "." + parser.getTable();
    }

    @Override
    public String getIcon(String value) {
      return "icon-table";
    }
  }

  private static class VariableLinkCell extends PlaceRequestCell<String> {

    private VariableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(String item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item);
      return ProjectPlacesHelper.getVariablePlace(parser.getDatasource(), parser.getTable(), parser.getVariable());
    }

    @Override
    public String getText(String item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item);
      return parser.getVariable();
    }
  }

  public static abstract class CartVariableCheckDisplay implements CheckboxColumn.Display<String> {

    private CartVariableTable table;

    public void setTable(CartVariableTable table) {
      this.table = table;
    }

    @Override
    public Table<String> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(String item) {
      return item;
    }

  }
}
