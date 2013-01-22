/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math.support;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.opal.web.finder.AbstractFinderQuery;

/**
 *
 */
public class CategoricalSummaryStatsQuery extends AbstractFinderQuery {

  private final ValueTable valueTable;

  private final Variable variable;

  private final VectorSource vectorSource;

  private final boolean distinct;

  public CategoricalSummaryStatsQuery(ValueTable valueTable, Variable variable, VectorSource vectorSource,
      boolean distinct) {
    this.valueTable = valueTable;
    this.variable = variable;
    this.vectorSource = vectorSource;
    this.distinct = distinct;

    getTableFilter().add(valueTable);
  }

  public ValueTable getValueTable() {
    return valueTable;
  }

  public Variable getVariable() {
    return variable;
  }

  public VectorSource getVectorSource() {
    return vectorSource;
  }

  public boolean isDistinct() {
    return distinct;
  }
}
