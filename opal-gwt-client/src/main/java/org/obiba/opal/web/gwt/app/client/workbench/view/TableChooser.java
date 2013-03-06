/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.watopi.chosen.client.ChosenOptions;

/**
 * Selector of tables.
 */
public class TableChooser extends Chooser {

  private static final Translations translations = GWT.create(Translations.class);

  private Map<String, TableDto> tableDtoMap = new HashMap<String, TableDto>();

  public TableChooser() {
    initWidget();
  }

  public TableChooser(ChosenOptions options) {
    super(options);
    initWidget();
  }

  public TableChooser(boolean isMultipleSelect) {
    super(isMultipleSelect);
    initWidget();
  }

  public TableChooser(boolean isMultipleSelect, ChosenOptions options) {
    super(isMultipleSelect, options);
    initWidget();
  }

  public TableChooser(Element element) {
    super(element);
    initWidget();
  }

  private void initWidget() {
    setPlaceholderText(translations.selectSomeTables());
  }

  public void addTableSelections(JsArray<TableDto> tables) {
    tableDtoMap.clear();
    HashMap<String, List<TableDto>> datasourceMap = new LinkedHashMap<String, List<TableDto>>();
    for(TableDto table : JsArrays.toIterable(tables)) {
      if(datasourceMap.containsKey(table.getDatasourceName()) == false) {
        datasourceMap.put(table.getDatasourceName(), new ArrayList<TableDto>());
      }
      datasourceMap.get(table.getDatasourceName()).add(table);
    }
    if(datasourceMap.keySet().size() > 1) {
      addDatasourceTableSelections(datasourceMap);
    } else {
      addTableSelections(datasourceMap.get(datasourceMap.keySet().iterator().next()));
    }
    update();
  }

  @Override
  public void clear() {
    super.clear();
    tableDtoMap.clear();
  }

  public void selectTable(TableDto table) {
    for(int i = 0; i < getItemCount(); i++) {
      if(getItemText(i).equals(table.getName())) {
        setSelectedIndex(i);
        break;
      }
    }
  }

  public void selectAllTables() {
    for(int i = 0; i < getItemCount(); i++) {
      setItemSelected(i, true);
    }
  }

  public void selectTables(JsArrayString tableFullNames) {
    for(String tableFullName : JsArrays.toIterable(tableFullNames)) {
      for(int i = 0; i < getItemCount(); i++) {
        if(getItemText(i).equals(tableFullName)) {
          setItemSelected(i, true);
          break;
        }
      }
    }
  }

  public List<TableDto> getSelectedTables() {
    List<TableDto> tables = new ArrayList<TableDto>();
    for(int i = 0; i < getItemCount(); i++) {
      if(isItemSelected(i)) {
        tables.add(tableDtoMap.get(getValue(i)));
      }
    }
    return tables;
  }

  private void addDatasourceTableSelections(HashMap<String, List<TableDto>> datasourceMap) {
    for(String ds : datasourceMap.keySet()) {
      addGroup(ds);
      for(TableDto table : datasourceMap.get(ds)) {
        String fullName = table.getDatasourceName() + "." + table.getName();
        addItemToGroup(fullName, fullName);
        tableDtoMap.put(fullName, table);
      }
    }
  }

  private void addTableSelections(List<TableDto> tables) {
    int i = 0;
    for(TableDto table : tables) {
      String fullName = table.getDatasourceName() + "." + table.getName();
      insertItem(table.getName(), fullName, i++);
      tableDtoMap.put(fullName, table);
    }
  }
}
