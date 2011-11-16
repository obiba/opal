/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.gwt.app.client.workbench.view.Grid;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.ListDataProvider;

public class ValueMapGrid extends FlowPanel {

  private static final int DEFAULT_PAGE_SIZE_MIN = 10;

  private static final int DEFAULT_PAGE_SIZE_MAX = 100;

  private static Translations translations = GWT.create(Translations.class);

  private SimplePager pager;

  private AbstractCellTable<ValueMapEntry> table;

  private ListDataProvider<ValueMapEntry> dataProvider;

  private List<ValueMapEntry> valueMapEntries;

  private int pageSizeMin = DEFAULT_PAGE_SIZE_MIN;

  private int pageSizeMax = DEFAULT_PAGE_SIZE_MAX;

  private String gridHeight = "30em";

  private boolean allowRowDeletion = false;

  public ValueMapGrid() {
    super();
    this.pager = new SimplePager();
    this.pager.setPageSize(DEFAULT_PAGE_SIZE_MAX);
    this.pager.addStyleName("right-aligned");
    this.pager.setVisible(false);
    add(pager);
    this.table = null;
  }

  public void setPageSizeMin(int pageSizeMin) {
    this.pageSizeMin = pageSizeMin;
  }

  public void setPageSizeMax(int pageSizeMax) {
    this.pageSizeMax = pageSizeMax;
  }

  public void setGridHeight(String gridHeight) {
    this.gridHeight = gridHeight;
  }

  public void populate(List<ValueMapEntry> valueMapEntries) {
    this.valueMapEntries = valueMapEntries;

    initializeTable();

    dataProvider = new ListDataProvider<ValueMapEntry>(valueMapEntries);
    dataProvider.addDataDisplay(table);
    dataProvider.refresh();

    addStyleName("value-map");
  }

  public void refreshValuesMap() {
    dataProvider.refresh();
  }

  private void initializeTable() {
    if(table != null) {
      remove(table);
      table = null;
    }

    if(valueMapEntries.size() > pageSizeMin) {
      table = new Grid<ValueMapEntry>(pager.getPageSize());
      table.setHeight(gridHeight);
    } else {
      table = new Table<ValueMapEntry>(pager.getPageSize());
    }
    table.addStyleName("clear");
    table.setPageSize(pageSizeMax);
    table.setWidth("100%");
    add(table);
    pager.setPageSize(pageSizeMax);
    pager.setDisplay(table);
    pager.setVisible((valueMapEntries.size() > pager.getPageSize()));

    initializeColumns();
  }

  private ActionsColumn<ValueMapEntry> createDeletionColumn() {
    ActionsColumn<ValueMapEntry> deleteColumn = new ActionsColumn<ValueMapEntry>(ActionsColumn.DELETE_ACTION);
    deleteColumn.setActionHandler(new ActionHandler<ValueMapEntry>() {

      @Override
      public void doAction(ValueMapEntry object, String actionName) {
        valueMapEntries.remove(object);
        refresh();
      }

    });
    return deleteColumn;
  }

  public void entryAdded() {
    populate(valueMapEntries);
  }

  private void refresh() {
    table.setRowCount(valueMapEntries.size());
    dataProvider.refresh();
  }

  private void initializeColumns() {
    initializeValueColumn();
    initializeLabelColumn();
    initializeNewValueColumn();
    initializeMissingColumn();
    initializeDeleteColumn();
  }

  private void initializeDeleteColumn() {
    if(allowRowDeletion) {
      ActionsColumn<ValueMapEntry> deleteColumn = createDeletionColumn();
      table.addColumn(deleteColumn, translations.actionsLabel());
    }
  }

  private void initializeValueColumn() {
    // Value
    Column<ValueMapEntry, String> valueColumn = new WrappedTextColumn() {

      @Override
      protected String getText(ValueMapEntry entry) {
        return entry.getValue();
      }

    };
    valueColumn.setCellStyleNames("value");
    table.addColumn(valueColumn, translations.valueLabel());
  }

  private void initializeLabelColumn() {
    // Label
    Column<ValueMapEntry, String> labelColumn = new WrappedTextColumn() {

      @Override
      protected String getText(ValueMapEntry entry) {
        return entry.getLabel();
      }

    };
    labelColumn.setCellStyleNames("value-label");
    table.addColumn(labelColumn, translations.labelLabel());
  }

  private void initializeNewValueColumn() {

    // New Value
    Column<ValueMapEntry, String> newValueColumn = new Column<ValueMapEntry, String>(new TextInputCell()) {
      @Override
      public String getValue(ValueMapEntry entry) {
        return entry.getNewValue();
      }
    };
    newValueColumn.setCellStyleNames("new-value");
    table.addColumn(newValueColumn, translations.newValueLabel());
    table.setColumnWidth(newValueColumn, "10em");
    newValueColumn.setFieldUpdater(new FieldUpdater<ValueMapEntry, String>() {
      public void update(int index, ValueMapEntry entry, String value) {
        entry.setNewValue(value);
        debug();
      }
    });
  }

  private void initializeMissingColumn() {
    // Missing
    Column<ValueMapEntry, Boolean> missingColumn = new Column<ValueMapEntry, Boolean>(new CheckboxCell()) {

      @Override
      public Boolean getValue(ValueMapEntry entry) {
        return entry.isMissing();
      }
    };
    missingColumn.setCellStyleNames("missing");
    table.addColumn(missingColumn, translations.missingLabel());
    table.setColumnWidth(missingColumn, "8em");
    missingColumn.setFieldUpdater(new FieldUpdater<ValueMapEntry, Boolean>() {
      @Override
      public void update(int index, ValueMapEntry entry, Boolean value) {
        entry.setMissing(value);
        debug();

      }
    });
  }

  public void enableRowDeletion(boolean enable) {
    allowRowDeletion = enable;
  }

  private void debug() {
    int i = 1;
    for(ValueMapEntry entry : valueMapEntries) {
      GWT.log((i++) + ": " + entry.getValue() + ", " + entry.getNewValue() + ", " + entry.isMissing());
    }
  }

  private abstract class WrappedTextColumn extends Column<ValueMapEntry, String> {

    public WrappedTextColumn() {
      super(new TextCell(new TrustedHtmlRenderer()));
    }

    protected abstract String getText(ValueMapEntry entry);

    @Override
    public String getValue(ValueMapEntry entry) {
      return wrapText(entry.getType(), getText(entry));
    }

    private String wrapText(ValueMapEntryType type, String text) {
      switch(type) {
      case CATEGORY_NAME:
        return "<span class='category'>" + text + "</span>";
      case DISTINCT_VALUE:
        return "<span class='distinct'>" + text + "</span>";
      case RANGE:
        return "<span class='range'>" + text + "</span>";
      case OTHER_VALUES:
        return "<span class='special others'>" + text + "</span>";
      case EMPTY_VALUES:
        return "<span class='special empties'>" + text + "</span>";
      default:
        return text;
      }
    }
  }

  /**
   * Escape user string, not ours.
   */
  private static final class TrustedHtmlRenderer extends AbstractSafeHtmlRenderer<String> {

    private final RegExp rg = RegExp.compile("(^<span class='[\\w\\s]+'>)(.*)(</span>$)");

    @Override
    public SafeHtml render(String object) {
      if(object == null) return SafeHtmlUtils.EMPTY_SAFE_HTML;

      MatchResult res = rg.exec(object);
      if(res.getGroupCount() == 4) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(SafeHtmlUtils.fromTrustedString(res.getGroup(1)));
        builder.appendEscaped(res.getGroup(2));
        builder.append(SafeHtmlUtils.fromTrustedString(res.getGroup(3)));
        return builder.toSafeHtml();
      } else {
        return SafeHtmlUtils.fromString(object);
      }

    }
  }

}
