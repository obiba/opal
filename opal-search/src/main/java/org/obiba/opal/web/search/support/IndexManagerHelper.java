/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableValuesIndex;
import org.obiba.opal.search.ValuesIndexManager;

/**
 * Helper class that wraps an IndexManager and provides some utility methods listed below
 */
public class IndexManagerHelper {

  private final ValuesIndexManager indexManager;

  private String datasource;

  private String table;

  public IndexManagerHelper(ValuesIndexManager indexManager) {
    this.indexManager = indexManager;
  }

  public IndexManagerHelper setDatasource(String datasource) {
    this.datasource = datasource;
    return this;
  }

  public IndexManagerHelper setTable(String table) {
    this.table = table;
    return this;
  }

  public VariableNature getVariableNature(String variableName) {
    Variable var = getValueTable().getVariable(variableName);

    return VariableNature.getNature(var);
  }

  public String getIndexName() {
    return getValueTableIndex().getIndexName();
  }

  public String getIndexFieldName(String variable) {
    return getValueTableIndex().getFieldName(variable);
  }

  public ValueTableValuesIndex getValueTableIndex() {
    return indexManager.getIndex(getValueTable());
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }
}