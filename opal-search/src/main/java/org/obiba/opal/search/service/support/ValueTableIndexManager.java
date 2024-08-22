/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.service.support;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.support.VariableNature;
import org.obiba.opal.search.service.ValueTableValuesIndex;
import org.obiba.opal.search.service.ValuesIndexManager;

/**
 * Helper class that wraps an IndexManager and provides some utility methods.
 */
public class ValueTableIndexManager {

  private final ValuesIndexManager valuesIndexManager;

  private final String datasource;

  private final String table;

  public ValueTableIndexManager(ValuesIndexManager valuesIndexManager, String datasource, String table) {
    this.valuesIndexManager = valuesIndexManager;
    this.datasource = datasource;
    this.table = table;
  }

  public ValueTableIndexManager copy(String datasourceAlt, String tableAlt) {
    return new ValueTableIndexManager(valuesIndexManager, datasourceAlt, tableAlt);
  }

  public String getReference() {
    return datasource + "." + table;
  }

  public String getQuery() {
    return "reference:(" + getReference().replaceAll(" ", "+") + ")";
  }

  public VariableNature getVariableNature(String variableName) {
    Variable var = getValueTable().getVariable(variableName);

    return VariableNature.getNature(var);
  }

  public String getIndexFieldName(String variable) {
    return getValueTableValuesIndex().getFieldName(variable);
  }

  public ValueTableValuesIndex getValueTableValuesIndex() {
    return valuesIndexManager.getIndex(getValueTable());
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }
}