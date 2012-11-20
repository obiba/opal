/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support.finder;

import java.util.List;

import org.obiba.magma.ValueTable;
import org.obiba.opal.search.service.OpalSearchService;

/**
 *
 */
public abstract class AbstractElasticSearchFinder<TQuery extends AbstractQuery, TResult> extends AbstractFinder<TQuery,
    TResult> {

  private final OpalSearchService opalSearchService;

  public AbstractElasticSearchFinder(OpalSearchService opalSearchService) {
    this.opalSearchService = opalSearchService;
  }

  public abstract void executeQuery(TQuery query, List<TResult> result, String... indexes);


  @Override
  public void find(TQuery query, List<TResult> result) {
    if(isSearchEnabled()) {
      // iterate on query.getTableFilter() and check if table is indexed

      executeQuery(query, result, "indexed_table_1", "indexed_table_2");

      // remove indexed tables from query.getTableFilter()
      // query.getTableFilter().removeAll(indexedTables);
    }

    next(query, result);
  }

  public boolean isSearchEnabled() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled();
  }
}
