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

import org.obiba.opal.web.gwt.app.client.workbench.view.Table;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

public class ValueMapGrid extends Table<ValueMapEntry> {

  private ListDataProvider<ValueMapEntry> dataProvider;

  private ListHandler<ValueMapEntry> sortHandler;

  private List<ValueMapEntry> valueMapEntries;

  public ValueMapGrid() {
    super();
  }

  public ValueMapGrid(int pageSize) {
    super(pageSize);
  }

  public void populate(List<ValueMapEntry> valueMapEntries) {
    this.valueMapEntries = valueMapEntries;
    dataProvider = new ListDataProvider<ValueMapEntry>(valueMapEntries);
    dataProvider.addDataDisplay(this);
    dataProvider.refresh();

    sortHandler = new ListHandler<ValueMapEntry>(valueMapEntries);
    addColumnSortHandler(sortHandler);

    initializeColumns();
  }

  private void initializeColumns() {
    // Value
    Column<ValueMapEntry, String> valueColumn = new TextColumn<ValueMapEntry>() {

      @Override
      public String getValue(ValueMapEntry entry) {
        return entry.getValue();
      }
    };
    addColumn(valueColumn, "Value");

    // New Value
    Column<ValueMapEntry, String> newValueColumn = new Column<ValueMapEntry, String>(new TextInputCell()) {
      @Override
      public String getValue(ValueMapEntry entry) {
        return entry.getNewValue();
      }
    };
    addColumn(newValueColumn, "New Value");
    newValueColumn.setFieldUpdater(new FieldUpdater<ValueMapEntry, String>() {
      public void update(int index, ValueMapEntry entry, String value) {
        entry.setNewValue(value);
        debug();
      }
    });

    // Missing
    Column<ValueMapEntry, Boolean> missingColumn = new Column<ValueMapEntry, Boolean>(new CheckboxCell()) {

      @Override
      public Boolean getValue(ValueMapEntry entry) {
        return entry.isMissing();
      }
    };
    addColumn(missingColumn, "Missing");
    missingColumn.setFieldUpdater(new FieldUpdater<ValueMapEntry, Boolean>() {
      @Override
      public void update(int index, ValueMapEntry entry, Boolean value) {
        entry.setMissing(value);
        debug();

      }
    });
  }

  private void debug() {
    int i = 1;
    for(ValueMapEntry entry : valueMapEntries) {
      GWT.log((i++) + ": " + entry.getValue() + ", " + entry.getNewValue() + ", " + entry.isMissing());
    }
  }

}
