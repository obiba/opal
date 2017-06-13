/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.spi.search.ValuesIndexManager;

/**
 * Criterion for filtering values based on a variable field, expressed as a ES query string.
 */
public class ESValueSetVariableCriterionParser implements ValueSetVariableCriterionParser {

  private final ValuesIndexManager valuesIndexManager;

  private final String query;

  private final String field;

  private final ValueTable table;

  public ESValueSetVariableCriterionParser(ValuesIndexManager valuesIndexManager, String query) {
    this.valuesIndexManager = valuesIndexManager;
    this.query = query;
    this.field = extractField();
    String[] tokens = field.split(ValuesIndexManager.FIELD_SEP);
    // verify variable access (exists and allowed)
    Datasource ds = MagmaEngine.get().getDatasource(tokens[0]);
    this.table = ds.getValueTable(tokens[1]);
    this.table.getVariable(tokens[2]);
  }

  public ValueTable getValueTable() {
    return table;
  }

  public String getField() {
    return field;
  }

  @Override
  public String getOriginalQuery() {
    return query;
  }

  @Override
  public QuerySearchJsonBuilder.ChildQuery asChildQuery() {
    return new QuerySearchJsonBuilder.ChildQuery(valuesIndexManager.getIndex(table).getIndexType(), query);
  }

  private String extractField() {
    String nQuery = query.startsWith("NOT ") ? query.substring(4) : query;
    if (nQuery.startsWith("_exists_:")) return nQuery.substring(9);
    return nQuery.substring(0, nQuery.indexOf(":"));
  }
}
