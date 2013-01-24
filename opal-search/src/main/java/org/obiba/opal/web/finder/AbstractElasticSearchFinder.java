/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.finder;

import org.obiba.opal.search.service.OpalSearchService;

/**
 *
 */
public abstract class AbstractElasticSearchFinder<TQuery extends AbstractFinderQuery, TResult extends FinderResult<?>> extends
    AbstractFinder<TQuery, TResult> {

  private final OpalSearchService opalSearchService;

  public AbstractElasticSearchFinder(OpalSearchService opalSearchService) {
    this.opalSearchService = opalSearchService;
  }

  public abstract Boolean executeQuery(TQuery query, TResult result, String... indexes);

  @Override
  public void find(TQuery query, TResult result) {

    if(isSearchEnabled()) {
      if (executeQuery(query, result, "indexed_table_1", "indexed_table_2")) {
        // No need to go further, the query succeeded!
        return;
      }
    }

    next(query, result);
  }

  public boolean isSearchEnabled() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled();
  }
}
