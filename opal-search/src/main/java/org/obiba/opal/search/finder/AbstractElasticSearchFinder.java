/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.finder;

import org.obiba.opal.search.service.OpalSearchService;

/**
 *
 */
public abstract class AbstractElasticSearchFinder<TQuery extends AbstractFinderQuery, TResult extends FinderResult<?>>
    extends AbstractFinder<TQuery, TResult> {

  protected final OpalSearchService opalSearchService;

  public AbstractElasticSearchFinder(OpalSearchService opalSearchService) {
    this.opalSearchService = opalSearchService;
  }

  public abstract void executeQuery(TQuery query, TResult result);

  @Override
  public void find(TQuery query, TResult result) {
    if(isSearchEnabled()) {
      executeQuery(query, result);
    }
    next(query, result);
  }

  public boolean isSearchEnabled() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled();
  }

}
