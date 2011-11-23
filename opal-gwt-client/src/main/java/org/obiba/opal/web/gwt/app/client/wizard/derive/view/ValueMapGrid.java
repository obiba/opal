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

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.ListDataProvider;

public class ValueMapGrid extends FlowPanel {

  private static final int DEFAULT_PAGE_SIZE_MIN = 10;

  private static final int DEFAULT_PAGE_SIZE_MAX = 100;

  private static Translations translations = GWT.create(Translations.class);

  private SimplePager pager;

  protected AbstractCellTable<ValueMapEntry> table;

  private ListDataProvider<ValueMapEntry> dataProvider;

  private List<ValueMapEntry> valueMapEntries;

  private int pageSizeMin = DEFAULT_PAGE_SIZE_MIN;

  private int pageSizeMax = DEFAULT_PAGE_SIZE_MAX;

  private String gridHeight = "30em";

  private boolean allowRowDeletion = false;

  private boolean allowFrequencyColumn = false;

  private Column<ValueMapEntry, ValueMapEntry> frequencyColumn;

  private double maxFrequency;

  private Image loading = new Image("image/loading.gif");

  public ValueMapGrid() {
    super();
    this.pager = new SimplePager();
    this.pager.setPageSize(DEFAULT_PAGE_SIZE_MAX);
    this.pager.addStyleName("right-aligned");
    this.pager.setVisible(false);
    add(pager);
    this.table = null;
    add(loading);
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

  public void populate(@SuppressWarnings("hiding") List<ValueMapEntry> valueMapEntries) {
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
    remove(loading);
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
    ActionsColumn<ValueMapEntry> deleteColumn = new ActionsColumn<ValueMapEntry>(ActionsColumn.DELETE_ACTION) {
      @Override
      public void render(Context context, ValueMapEntry entry, SafeHtmlBuilder sb) {
        // do not allow removing special rows
        if(!entry.getType().equals(ValueMapEntryType.EMPTY_VALUES) //
            && !entry.getType().equals(ValueMapEntryType.OTHER_VALUES)) {
          super.render(context, entry, sb);
        }
      }
    };
    deleteColumn.setActionHandler(new ActionHandler<ValueMapEntry>() {

      @Override
      public void doAction(ValueMapEntry entry, String actionName) {
        valueMapEntries.remove(entry);
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
    initializeFrequencyColumn();
    initializeLabelColumn();
    initializeNewValueColumn();
    initializeMissingColumn();
    initializeDeleteColumn();
  }

  private void initializeFrequencyColumn() {
    if(allowFrequencyColumn) {
      frequencyColumn = new ValueMapColum(new StatCell(maxFrequency));
      table.addColumn(frequencyColumn, translations.frequency());
    }
  }

  private void initializeDeleteColumn() {
    if(allowRowDeletion) {
      ActionsColumn<ValueMapEntry> deleteColumn = createDeletionColumn();
      table.addColumn(deleteColumn, translations.actionsLabel());
    }
  }

  protected void initializeValueColumn() {

    ValueMapCell cell = new ValueMapCell() {

      @Override
      protected String getText(ValueMapEntry entry) {
        return entry.getValue();
      }
    };

    Column<ValueMapEntry, ValueMapEntry> valueColumn = new ValueMapColum(cell);
    valueColumn.setCellStyleNames("original-value");
    table.addColumn(valueColumn, translations.originalValueLabel());
  }

  private void initializeLabelColumn() {

    ValueMapCell cell = new ValueMapCell() {

      @Override
      protected String getText(ValueMapEntry entry) {
        return entry.getLabel();
      }
    };

    Column<ValueMapEntry, ValueMapEntry> labelColumn = new ValueMapColum(cell);
    labelColumn.setCellStyleNames("original-label");
    table.addColumn(labelColumn, translations.originalLabelLabel());
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
      @Override
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
    missingColumn.setCellStyleNames("new-missing");
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

  public void enableFrequencyColumn(boolean enable) {
    allowFrequencyColumn = enable;
  }

  private void debug() {
    int i = 1;
    for(ValueMapEntry entry : valueMapEntries) {
      GWT.log((i++) + ": " + entry.getValue() + ", " + entry.getNewValue() + ", " + entry.isMissing());
    }
  }

  public void setMaxFrequency(double maxFrequency) {
    this.maxFrequency = maxFrequency;
  }

  private static class ValueMapColum extends Column<ValueMapEntry, ValueMapEntry> {

    public ValueMapColum(Cell<ValueMapEntry> cell) {
      super(cell);
    }

    @Override
    public ValueMapEntry getValue(ValueMapEntry entry) {
      return entry;
    }

  }

}
