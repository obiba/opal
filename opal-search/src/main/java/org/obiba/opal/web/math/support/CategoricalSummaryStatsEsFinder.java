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

import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.finder.AbstractElasticSearchFinder;
import org.obiba.opal.web.finder.FinderResult;
import org.obiba.opal.web.model.Math;

/**
 *
 */
public class CategoricalSummaryStatsEsFinder extends
    AbstractElasticSearchFinder<CategoricalSummaryStatsQuery, FinderResult<Math.CategoricalSummaryDto>> {

  public CategoricalSummaryStatsEsFinder(OpalSearchService opalSearchService) {
    super(opalSearchService);
  }

  @Override
  public Boolean executeQuery(CategoricalSummaryStatsQuery query, FinderResult<Math.CategoricalSummaryDto> result,
      String... indexes) {

    // TODO build CategoricalSummaryDto with Elastic Search query
    return true;
  }

}
