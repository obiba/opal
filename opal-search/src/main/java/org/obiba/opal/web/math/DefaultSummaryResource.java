/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

/**
 *
 */
public class DefaultSummaryResource extends AbstractSummaryResource {

  public DefaultSummaryResource(OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, ValueTable valueTable, Variable variable, @Nullable VectorSource vectorSource) {
    super(opalSearchService, statsIndexManager, esProvider, valueTable, variable, vectorSource);
  }

  @GET
  @POST
  public SummaryStatisticsDto compute() {
    return SummaryStatisticsDto.newBuilder().setResource(getVariable().getName()).build();
  }

}
