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

/**
 * Helper class that wraps an IndexManager and provides some utility methods listed below
 */
public class IndexManagerHelper {
  private final IndexManager indexManager;

  private final String datasource;

  private final String table;

  public IndexManagerHelper(IndexManager indexManager, String datasource, String table) {
    this.indexManager = indexManager;
    this.datasource = datasource;
    this.table = table;
  }

  public VariableNature getVariableNature(String variableName) {
    Variable var = getValueTable().getVariable(variableName);

    return VariableNature.getNature(var);
  }

  public String getIndexName() {
    return getValueTableIndex().getName();
  }

  public ValueTableIndex getValueTableIndex() {
    return this.indexManager.getIndex(getValueTable());
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }
}
