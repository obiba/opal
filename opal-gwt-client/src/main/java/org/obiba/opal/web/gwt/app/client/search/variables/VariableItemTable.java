/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.search.ItemFieldsDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;

import java.util.List;

public class VariableItemTable extends Table<ItemResultDto> {

  private CheckboxColumn<ItemResultDto> checkColumn;

  public VariableItemTable() {
  }

  public void initialize(PlaceManager placeManager, ItemResultCheckDisplay checkDisplay) {
    while (getColumnCount() > 0) {
      removeColumn(0);
    }
    initColumns(placeManager, checkDisplay);
  }

  public List<ItemResultDto> getSelectedItems() {
    return checkColumn.getSelectedItems();
  }

  public void clearSelectedItems() {
    checkColumn.clearSelection();
  }

  private void initColumns(PlaceManager placeManager, ItemResultCheckDisplay checkDisplay) {
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
      addColumn(new TextColumn<ItemResultDto>() {
        @Override
        public String getValue(ItemResultDto item) {
          MagmaPath.Parser parser = MagmaPath.Parser.parse(item.getIdentifier());
          return parser.getVariable();
        }
      }, translations.variableLabel());
      addColumn(new TextColumn<ItemResultDto>() {
        @Override
        public String getValue(ItemResultDto item) {
          MagmaPath.Parser parser = MagmaPath.Parser.parse(item.getIdentifier());
          return parser.getDatasource() + "." + parser.getTable();
        }
      }, translations.tableLabel());
    }

    addColumn(new ItemFieldColumn("label", true), translations.labelLabel());
    addColumn(new ItemFieldColumn("entityType", false), translations.entityTypeLabel());
  }

  private void addCheckColumn(CheckboxColumn.Display<ItemResultDto> checkboxDisplay) {
    checkColumn = new CheckboxColumn<>(checkboxDisplay);
    addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    setColumnWidth(checkColumn, 1, com.google.gwt.dom.client.Style.Unit.PX);
  }

  private static class VariableItemColumn extends Column<ItemResultDto, ItemResultDto> {
    public VariableItemColumn(Cell<ItemResultDto> cell) {
      super(cell);
    }

    @Override
    public ItemResultDto getValue(ItemResultDto item) {
      return item;
    }
  }

  private static class ProjectTableLinkCell extends PlaceRequestCell<ItemResultDto> {

    private ProjectTableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(ItemResultDto item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item.getIdentifier());
      return ProjectPlacesHelper.getTablePlace(parser.getDatasource(), parser.getTable());
    }

    @Override
    public String getText(ItemResultDto item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item.getIdentifier());
      return parser.getDatasource() + "." + parser.getTable();
    }

    @Override
    public String getIcon(ItemResultDto value) {
      return "icon-table";
    }
  }

  private static class VariableLinkCell extends PlaceRequestCell<ItemResultDto> {

    private VariableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(ItemResultDto item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item.getIdentifier());
      return ProjectPlacesHelper.getVariablePlace(parser.getDatasource(), parser.getTable(), parser.getVariable());
    }

    @Override
    public String getText(ItemResultDto item) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item.getIdentifier());
      return parser.getVariable();
    }
  }

  private static class ItemFieldColumn extends Column<ItemResultDto, String> {

    private final String name;

    private final boolean markdown;

    ItemFieldColumn(String name, boolean markdown) {
      super(new TextCell(new SafeHtmlRenderer<String>() {

        @Override
        public SafeHtml render(String object) {
          return object == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromTrustedString(object);
        }

        @Override
        public void render(String object, SafeHtmlBuilder appendable) {
          appendable.append(SafeHtmlUtils.fromTrustedString(object));
        }
      }));
      this.name = name;
      this.markdown = markdown;
    }

    @Override
    public String getValue(ItemResultDto item) {
      StringBuilder fieldsHtml = new StringBuilder();
      ItemFieldsDto fields = (ItemFieldsDto) item.getExtension("Search.ItemFieldsDto.item");
      for (EntryDto entry : JsArrays.toIterable(fields.getFieldsArray())) {
        boolean toAppend = false;
        String locale = null;
        if (entry.getKey().equals(name)) {
          toAppend = true;
        } else if (entry.getKey().startsWith(name + "-")) {
          toAppend = true;
          locale = entry.getKey().substring(name.length() + 1);
        }
        if (toAppend) {
          fieldsHtml.append("<div class=\"attribute-value\">");
          if (!Strings.isNullOrEmpty(locale))
            fieldsHtml.append("<span class=\"label\">").append(locale).append("</span> ");
          String value = entry.getValue();
          if (markdown)
            fieldsHtml.append(Markdown.parse(value));
          else {
            String safeValue = SafeHtmlUtils.fromString(value).asString().replaceAll("\\n", "<br />");
            fieldsHtml.append(safeValue);
          }
          fieldsHtml.append("</div>");
        }
      }
      return fieldsHtml.toString();
    }
  }

  public static abstract class ItemResultCheckDisplay implements CheckboxColumn.Display<ItemResultDto> {

    private VariableItemTable table;

    public ItemResultCheckDisplay() {
    }

    public void setTable(VariableItemTable table) {
      this.table = table;
    }

    @Override
    public Table<ItemResultDto> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(ItemResultDto item) {
      return item.getIdentifier();
    }

    @Override
    public void selectAllItems(CheckboxColumn.ItemSelectionHandler<ItemResultDto> handler) {
      if (table.getVisibleItemCount() == table.getRowCount()) {
        for (ItemResultDto item : table.getVisibleItems())
          handler.onItemSelection(item);
      }
      else {
        // TODO query all items
      }
    }

  }
}
