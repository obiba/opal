/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.math;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.finder.FinderResult;
import org.obiba.opal.web.math.support.CategoricalSummaryStatsEsFinder;
import org.obiba.opal.web.math.support.CategoricalSummaryStatsMagmaFinder;
import org.obiba.opal.web.math.support.CategoricalSummaryStatsQuery;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class CategoricalSummaryStatisticsResource extends AbstractSummaryStatisticsResource {

  final static String NULL_NAME = "N/A";

  @Autowired
  private OpalSearchService opalSearchService;

  /**
   * @param valueTable
   * @param variable
   * @param vectorSource
   */
  public CategoricalSummaryStatisticsResource(ValueTable valueTable, Variable variable, VectorSource vectorSource) {
    super(valueTable, variable, vectorSource);
  }

  @GET
  @POST
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public Response compute(@QueryParam("distinct") boolean distinct) {

    CategoricalSummaryStatsQuery query = new CategoricalSummaryStatsQuery(getValueTable(), getVariable(),
        getVectorSource(), distinct);

    FinderResult<CategoricalSummaryDto> result = new FinderResult<CategoricalSummaryDto>();
    CategoricalSummaryStatsEsFinder finder = new CategoricalSummaryStatsEsFinder(opalSearchService);
    finder.nextFinder(new CategoricalSummaryStatsMagmaFinder());

    finder.find(query, result);

    return TimestampedResponses.ok(getValueTable(),
        SummaryStatisticsDto.newBuilder().setResource(getVariable().getName())
            .setExtension(CategoricalSummaryDto.categorical, result.getValue()).build()).build();
  }

}
