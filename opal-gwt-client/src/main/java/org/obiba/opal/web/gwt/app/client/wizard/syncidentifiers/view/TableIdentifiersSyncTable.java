/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.syncidentifiers.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.TableIdentifiersSync;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;

/**
 *
 */
public abstract class TableIdentifiersSyncTable extends Table<TableIdentifiersSync> {

  private static Translations translations = GWT.create(Translations.class);

  private Column<TableIdentifiersSync, String> tableNameColumn;

  public TableIdentifiersSyncTable() {
    initColumns();
  }

  public Column<TableIdentifiersSync, String> getTableNameColumn() {
    return tableNameColumn;
  }

  private void initColumns() {
    initCheckColumn();
    initTableNameColumn();
    initCountColumns();
  }

  private void initCountColumns() {
    addColumn(new TextColumn<TableIdentifiersSync>() {

      @Override
      public String getValue(TableIdentifiersSync object) {
        return object.getCount() + " / " + object.getTotal();
      }
    }, translations.identifiersSyncCountLabel());
  }

  private void initCheckColumn() {
    Column<TableIdentifiersSync, Boolean> checkColumn = new Column<TableIdentifiersSync, Boolean>(
        new CheckboxCell(true, true) {
          @Override
          public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
            // check if forbidden or has conflict
            TableIdentifiersSync ts = (TableIdentifiersSync) context.getKey();
            if(ts.getCount() > 0) {
              super.render(context, value, sb);
            } else {
              sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" disabled=\"true\" tabindex=\"-1\"/>"));
            }
          }
        }) {

      @Override
      public Boolean getValue(TableIdentifiersSync object) {
        // Get the value from the selection model.
        return getSelectionModel().isSelected(object);
      }

    };
    checkColumn.setFieldUpdater(new FieldUpdater<TableIdentifiersSync, Boolean>() {

      @Override
      public void update(int index, TableIdentifiersSync object, Boolean value) {
        getSelectionModel().setSelected(object, value);
      }
    });

    addColumn(checkColumn, createTableListCheckColumnHeader());
  }

  private Header<Boolean> createTableListCheckColumnHeader() {
    Header<Boolean> checkHeader = new Header<Boolean>(new CheckboxCell(true, true)) {

      @Override
      public Boolean getValue() {
        if(getTableSyncs().isEmpty()) return false;
        boolean allSelected = true;
        boolean hasSelectable = false;
        for(TableIdentifiersSync ts : getTableSyncs()) {
          if(ts.getCount() > 0) {
            hasSelectable = true;
            if(getSelectionModel().isSelected(ts) == false) {
              return false;
            }
          }
        }
        return hasSelectable == false ? false : allSelected;
      }
    };
    checkHeader.setUpdater(new ValueUpdater<Boolean>() {

      @Override
      public void update(Boolean value) {
        for(TableIdentifiersSync ts : getTableSyncs()) {
          if(ts.getCount() > 0) {
            getSelectionModel().setSelected(ts, value);
          }
        }
      }
    });
    return checkHeader;
  }

  private void initTableNameColumn() {
    addColumn(tableNameColumn = new TextColumn<TableIdentifiersSync>() {

      @Override
      public String getValue(TableIdentifiersSync object) {
        return object.getTable();
      }
    }, translations.tableLabel());
  }

  protected abstract List<TableIdentifiersSync> getTableSyncs();

}
