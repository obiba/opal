/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.finder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;

/**
 *
 */
public abstract class AccessFilterTablesFinder<TQuery extends AbstractFinderQuery, TResult extends FinderResult<?>>
    extends AbstractFinder<TQuery, TResult> {

  @Override
  public void find(TQuery query, TResult result) {
    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable valueTable : datasource.getValueTables()) {
        if(isTableSearchable(valueTable, query)) {
          query.getTableFilter().add(valueTable);
        }
      }
    }
    next(query, result);
  }

  protected abstract boolean isTableSearchable(ValueTable valueTable, TQuery query);

}
