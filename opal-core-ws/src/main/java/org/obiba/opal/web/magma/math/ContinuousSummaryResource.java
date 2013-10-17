/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.math;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary.Distribution;
import org.obiba.opal.core.magma.math.ContinuousVariableSummaryFactory;
import org.obiba.opal.core.service.VariableStatsService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.ContinuousSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

public class ContinuousSummaryResource extends AbstractSummaryResource {

  public ContinuousSummaryResource(VariableStatsService variableStatsService, ValueTable valueTable, Variable variable,
      VariableValueSource vvs) {
    super(variableStatsService, valueTable, variable, vvs);
  }

  @GET
  @POST
  public Response compute(@QueryParam("d") @DefaultValue("normal") Distribution distribution,
      @QueryParam("p") List<Double> percentiles, @QueryParam("intervals") @DefaultValue("10") int intervals,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {

    ContinuousVariableSummaryFactory summaryFactory = new ContinuousVariableSummaryFactory.Builder()
        .variable(getVariable()).table(getValueTable()).valueSource(getVariableValueSource()).distribution(distribution)
        .percentiles(percentiles).intervals(intervals).offset(offset).limit(limit).build();

    ContinuousVariableSummary summary = variableStatsService.getContinuousSummary(summaryFactory);

    SummaryStatisticsDto dto = SummaryStatisticsDto.newBuilder() //
        .setResource(getVariable().getName()) //
        .setExtension(ContinuousSummaryDto.continuous, Dtos.asDto(summary).build()) //
        .build();

    return offset == null && limit == null
        ? TimestampedResponses.ok(getValueTable(), dto).build()
        : Response.ok(dto).build();

  }

}
