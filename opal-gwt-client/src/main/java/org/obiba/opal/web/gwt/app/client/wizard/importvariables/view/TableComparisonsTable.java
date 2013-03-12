/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.view.ComparedDatasourcesReportStepView.TableComparison;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;

/**
 *
 */
public abstract class TableComparisonsTable extends Table<TableComparison> {

  private static Translations translations = GWT.create(Translations.class);

  private Column<TableComparison, String> tableNameColumn;

  public TableComparisonsTable() {
    super();
    initColumns();
  }

  public Column<TableComparison, String> getTableNameColumn() {
    return tableNameColumn;
  }

  private void initColumns() {
    initCheckColumn();
    initTableNameColumn();
    initCountColumns();
  }

  private void initCountColumns() {
    addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        return Integer.toString(object.getUnmodifiedVariablesCount());
      }
    }, translations.unmodifiedVariablesLabel());

    addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        int conflicts = object.getNewVariablesConflictsCount();
        if(conflicts > 0) {
          return Integer.toString(object.getNewVariablesCount()) + " (" + conflicts + ")";
        } else {
          return Integer.toString(object.getNewVariablesCount());
        }
      }
    }, translations.newVariablesLabel());

    addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        int conflicts = object.getModifiedVariablesConflictsCount();
        if(conflicts > 0) {
          return Integer.toString(object.getModifiedVariablesCount()) + " (" + conflicts + ")";
        } else {
          return Integer.toString(object.getModifiedVariablesCount());
        }
      }
    }, translations.modifiedVariablesLabel());

    addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        return Integer.toString(object.getConflictsCount());
      }
    }, translations.conflictedVariablesLabel());
  }

  private void initCheckColumn() {
    Column<TableComparison, Boolean> checkColumn = new Column<TableComparison, Boolean>(new CheckboxCell(true, true) {
      @Override
      public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        // check if forbidden or has conflict
        TableComparison tc = (TableComparison) context.getKey();
        if(tc.isSelectable()) {
          super.render(context, value, sb);
        } else {
          sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" disabled=\"true\" tabindex=\"-1\"/>"));
        }
      }
    }) {

      @Override
      public Boolean getValue(TableComparison object) {
        // Get the value from the selection model.
        return getSelectionModel().isSelected(object);
      }

    };
    checkColumn.setFieldUpdater(new FieldUpdater<ComparedDatasourcesReportStepView.TableComparison, Boolean>() {

      @Override
      public void update(int index, TableComparison object, Boolean value) {
        getSelectionModel().setSelected(object, value);
      }
    });

    addColumn(checkColumn, createTableListCheckColumnHeader());
  }

  private Header<Boolean> createTableListCheckColumnHeader() {
    Header<Boolean> checkHeader = new Header<Boolean>(new CheckboxCell(true, true)) {

      @Override
      public Boolean getValue() {
        if(getTableComparisons().size() == 0) return false;
        boolean allSelected = true;
        boolean hasSelectable = false;
        for(TableComparison tc : getTableComparisons()) {
          if(tc.isSelectable()) {
            hasSelectable = true;
            if(getSelectionModel().isSelected(tc) == false) {
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
        for(TableComparison tc : getTableComparisons()) {
          if(tc.isSelectable()) {
            getSelectionModel().setSelected(tc, value);
          }
        }
      }
    });
    return checkHeader;
  }

  private void initTableNameColumn() {
    addColumn(tableNameColumn = new Column<TableComparison, String>(new ClickableTextCell() {
      @Override
      public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if(value != null) {
          TableComparison tc = (TableComparison) context.getKey();
          sb.appendHtmlConstant("<a class=\"" + tc.getStatusStyle() + "\" title=\"" +
              translations.comparisonResultMap().get(tc.getStatus()) + "\">").append(value).appendHtmlConstant("</a>");
        }
      }
    }) {

      @Override
      public String getValue(TableComparison object) {
        return object.getTableName();
      }
    }, translations.tableLabel());
  }

  protected abstract List<TableComparison> getTableComparisons();

}
