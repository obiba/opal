/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.web.gwt.app.client.cart;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.cart.service.CartVariableItem;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.*;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import java.util.List;

public class CartVariableTable extends Table<CartVariableItem> {

  private CheckboxColumn<CartVariableItem> checkColumn;

  public CartVariableTable() {
  }

  public void initialize(PlaceManager placeManager, CartVariableCheckDisplay checkDisplay, ActionHandler<CartVariableItem> actionHandler) {
    while (getColumnCount() > 0) {
      removeColumn(0);
    }
    initColumns(placeManager, checkDisplay, actionHandler);
  }

  private void initColumns(PlaceManager placeManager, CartVariableCheckDisplay checkDisplay, ActionHandler<CartVariableItem> actionHandler) {
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
      addColumn(new TextColumn<CartVariableItem>() {
        @Override
        public String getValue(CartVariableItem item) {
          return item.getVariable().getName();
        }
      }, translations.variableLabel());
      addColumn(new TextColumn<CartVariableItem>() {
        @Override
        public String getValue(CartVariableItem item) {
          return item.getDatasource() + "." + item.getTable();
        }
      }, translations.tableLabel());
    }

    addColumn(new VariableItemLabelColumn(), translations.labelLabel());
    addColumn(new TextColumn<CartVariableItem>() {
      @Override
      public String getValue(CartVariableItem item) {
        return item.getEntityType();
      }
    }, translations.entityTypeLabel());

    ActionsColumn<CartVariableItem> actionColumn = new ActionsColumn<CartVariableItem>(ActionsColumn.REMOVE_ACTION);
    if (actionHandler != null) actionColumn.setActionHandler(actionHandler);
    addColumn(actionColumn, translations.actionsLabel());
  }

  private void addCheckColumn(CheckboxColumn.Display<CartVariableItem> checkboxDisplay) {
    checkColumn = new CheckboxColumn<>(checkboxDisplay);
    addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    setColumnWidth(checkColumn, 1, com.google.gwt.dom.client.Style.Unit.PX);
  }

  public List<CartVariableItem> getSelectedItems() {
    return checkColumn.getSelectedItems();
  }

  public void clearSelectedItems() {
    checkColumn.clearSelection();
  }

  private static class VariableItemLabelColumn extends AttributeColumn<CartVariableItem> {

    public VariableItemLabelColumn() {
      super("label");
    }

    @Override
    protected boolean isMarkdown() {
      return true;
    }

    @Override
    protected JsArray<AttributeDto> getAttributes(CartVariableItem object) {
      return object.getVariable().getAttributesArray();
    }
  }

  private static class VariableItemColumn extends Column<CartVariableItem, CartVariableItem> {
    public VariableItemColumn(Cell<CartVariableItem> cell) {
      super(cell);
    }

    @Override
    public CartVariableItem getValue(CartVariableItem item) {
      return item;
    }
  }

  private static class ProjectTableLinkCell extends PlaceRequestCell<CartVariableItem> {

    private ProjectTableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(CartVariableItem item) {
      return ProjectPlacesHelper.getTablePlace(item.getDatasource(), item.getTable());
    }

    @Override
    public String getText(CartVariableItem item) {
      return item.getDatasource() + "." + item.getTable();
    }

    @Override
    public String getIcon(CartVariableItem value) {
      return "icon-table";
    }
  }

  private static class VariableLinkCell extends PlaceRequestCell<CartVariableItem> {

    private VariableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(CartVariableItem item) {
      return ProjectPlacesHelper.getVariablePlace(item.getDatasource(), item.getTable(), item.getVariable().getName());
    }

    @Override
    public String getText(CartVariableItem item) {
      return item.getVariable().getName();
    }
  }

  public static abstract class CartVariableCheckDisplay implements CheckboxColumn.Display<CartVariableItem> {

    private CartVariableTable table;

    public void setTable(CartVariableTable table) {
      this.table = table;
    }

    @Override
    public Table<CartVariableItem> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(CartVariableItem item) {
      return item.getIdentifier();
    }

  }
}
