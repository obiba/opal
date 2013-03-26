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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.FrequencyDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

/**
 *
 */
public class CategoricalSummaryStatisticsResource extends AbstractSummaryStatisticsResource {

  final static String NULL_NAME = "N/A";

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

    CategoricalVariableSummary summary = new CategoricalVariableSummary.Builder(getVariable()) //
        .distinct(distinct) //
        .addTable(getValueTable()) //
        .build();

    CategoricalSummaryDto.Builder dtoBuilder = CategoricalSummaryDto.newBuilder();
    dtoBuilder.setMode(summary.getMode()).setN(summary.getN());
    for(CategoricalVariableSummary.Frequency frequency : summary.getFrequencies()) {
      dtoBuilder.addFrequencies(FrequencyDto.newBuilder().setValue(frequency.getValue()).setFreq(frequency.getFreq())
          .setPct(frequency.getPct()));
    }

    CategoricalSummaryDto summaryDto = dtoBuilder.build();
    return TimestampedResponses.ok(getValueTable(),
        SummaryStatisticsDto.newBuilder().setResource(getVariable().getName())
            .setExtension(CategoricalSummaryDto.categorical, summaryDto).build()).build();
  }

}
