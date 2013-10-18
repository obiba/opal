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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.core.magma.math.CategoricalVariableSummaryFactory;
import org.obiba.opal.core.service.VariableStatsService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

/**
 *
 */
public class CategoricalSummaryResource extends AbstractSummaryResource {

  public CategoricalSummaryResource(VariableStatsService variableStatsService, ValueTable valueTable, Variable variable,
      ValueSource vvs) {
    super(variableStatsService, valueTable, variable, vvs);
  }

  @GET
  @POST
  public Response get(@QueryParam("distinct") boolean distinct, @QueryParam("offset") Integer offset,
      @QueryParam("limit") Integer limit) {

    CategoricalVariableSummaryFactory summaryFactory = new CategoricalVariableSummaryFactory.Builder()
        .variable(getVariable()).table(getValueTable()).valueSource(getVariableValueSource()).distinct(distinct)
        .offset(offset).limit(limit).build();

    CategoricalVariableSummary summary = variableStatsService.getCategoricalSummary(summaryFactory);

    SummaryStatisticsDto dto = SummaryStatisticsDto.newBuilder() //
        .setResource(getVariable().getName()) //
        .setExtension(CategoricalSummaryDto.categorical, Dtos.asDto(summary).build()) //
        .build();

    return offset == null && limit == null
        ? TimestampedResponses.ok(getValueTable(), dto).build()
        : Response.ok(dto).build();
  }

}
