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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.ListDataProvider;

public class ValueMapGrid extends FlowPanel {

  private static final int DEFAULT_PAGE_SIZE = 100;

  private static final Translations translations = GWT.create(Translations.class);

  private final SimplePager pager;

  protected AbstractCellTable<ValueMapEntry> table;

  private ListDataProvider<ValueMapEntry> dataProvider;

  private List<ValueMapEntry> valueMapEntries;

  @Nullable
  private List<String> valueChoices;

  private int pageSize = DEFAULT_PAGE_SIZE;

  private boolean allowRowDeletion = false;

  private boolean allowFrequencyColumn = false;

  private Column<ValueMapEntry, ValueMapEntry> frequencyColumn;

  private double maxFrequency;

  public ValueMapGrid() {
    pager = new SimplePager();
    pager.setPageSize(DEFAULT_PAGE_SIZE);
    pager.addStyleName("right-aligned");
    pager.setVisible(false);
    add(pager);
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public void populate(@SuppressWarnings("hiding") List<ValueMapEntry> valueMapEntries) {
    populate(valueMapEntries, null);
  }

  public void populate(@SuppressWarnings("hiding") List<ValueMapEntry> valueMapEntries,
      @Nullable List<String> valueChoices) {
    this.valueMapEntries = valueMapEntries;
    this.valueChoices = valueChoices;
    if(hasValueChoices() && !valueChoices.contains("")) {
      valueChoices.add(0, "");
    }

    if(dataProvider == null) {
      initializeTable();
      dataProvider = new ListDataProvider<ValueMapEntry>(valueMapEntries);
      dataProvider.addDataDisplay(table);
    } else {
      dataProvider.setList(valueMapEntries);
    }
    pager.setVisible(valueMapEntries.size() > pager.getPageSize());
    dataProvider.refresh();

    addStyleName("value-map");
  }

  public void refreshValuesMap() {
    dataProvider.refresh();
  }

  private boolean hasValueChoices() {
    return valueChoices != null && !valueChoices.isEmpty();
  }

  private void initializeTable() {
    if(table != null) {
      remove(table);
      table = null;
    }

    table = new Table<ValueMapEntry>(pager.getPageSize());
    // not supposed to be empty except while being populated
    table.setEmptyTableWidget(table.getLoadingIndicator());
    table.addStyleName("clear");
    table.setPageSize(pageSize);
    table.setWidth("100%");
    add(table);
    pager.setPageSize(pageSize);
    pager.setDisplay(table);

    initializeColumns();
  }

  private ActionsColumn<ValueMapEntry> createDeletionColumn() {
    ActionsColumn<ValueMapEntry> deleteColumn = new ActionsColumn<ValueMapEntry>(ActionsColumn.DELETE_ACTION) {
      @Override
      public void render(Context context, ValueMapEntry entry, SafeHtmlBuilder sb) {
        // do not allow removing special rows
        if(entry.getType() != ValueMapEntryType.EMPTY_VALUES && entry.getType() != ValueMapEntryType.OTHER_VALUES) {
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
      if(frequencyColumn == null) {
        frequencyColumn = new ValueMapColumn(new StatCell(maxFrequency));
      }
      table.insertColumn(1, frequencyColumn, translations.frequency());
      table.setColumnWidth(frequencyColumn, "120px");
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

    Column<ValueMapEntry, ValueMapEntry> valueColumn = new ValueMapColumn(cell);
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

    Column<ValueMapEntry, ValueMapEntry> labelColumn = new ValueMapColumn(cell);
    labelColumn.setCellStyleNames("original-label");
    table.addColumn(labelColumn, translations.originalLabelLabel());
  }

  private void initializeNewValueColumn() {

    // New Value
    Cell<String> cell = hasValueChoices() ? new SelectionCell(valueChoices) : new TextInputCell();
    Column<ValueMapEntry, String> newValueColumn = new Column<ValueMapEntry, String>(cell) {
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
      }
    });
  }

  public void enableRowDeletion(boolean enable) {
    allowRowDeletion = enable;
  }

  public void enableFrequencyColumn(boolean enable) {
    if(allowFrequencyColumn == enable) return;
    allowFrequencyColumn = enable;
    if(!allowFrequencyColumn) {
      table.removeColumn(frequencyColumn);
    } else if(table != null) {
      initializeFrequencyColumn();
    }
  }

  public void setMaxFrequency(double maxFrequency) {
    this.maxFrequency = maxFrequency;
    if(frequencyColumn != null) {
      if(allowFrequencyColumn) {
        table.removeColumn(frequencyColumn);
        frequencyColumn = null;
      }
      initializeFrequencyColumn();
    }
  }

  private static class ValueMapColumn extends Column<ValueMapEntry, ValueMapEntry> {

    private ValueMapColumn(Cell<ValueMapEntry> cell) {
      super(cell);
    }

    @Override
    public ValueMapEntry getValue(ValueMapEntry entry) {
      return entry;
    }

  }

}
