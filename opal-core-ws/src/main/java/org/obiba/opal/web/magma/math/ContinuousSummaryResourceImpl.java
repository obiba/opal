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

import javax.ws.rs.core.Response;

import org.obiba.magma.math.summary.ContinuousVariableSummary;
import org.obiba.magma.math.summary.ContinuousVariableSummaryFactory;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.ContinuousSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.obiba.magma.math.summary.ContinuousVariableSummary.Distribution;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@Transactional
public class ContinuousSummaryResourceImpl extends AbstractSummaryResource implements ContinuousSummaryResource {

  @Override
  public Response get(Distribution distribution, List<Double> percentiles, int intervals, Integer offset, Integer limit,
      boolean resetCache) {
    ContinuousVariableSummaryFactory summaryFactory = getBuilder(distribution, percentiles, intervals, offset)
        .limit(limit).build();
    return toResponse(variableSummaryService.getSummary(summaryFactory, resetCache));

  }

  @Override
  public Response getCachedFullOrComputeLimit(Distribution distribution, List<Double> percentiles, int intervals,
      Integer offset, Integer limit) {

    ContinuousVariableSummaryFactory summaryFactory = getBuilder(distribution, percentiles, intervals, offset).build();
    return variableSummaryService.isSummaryCached(summaryFactory) //
        ? toResponse(variableSummaryService.getSummary(summaryFactory, false)) //
        : get(distribution, percentiles, intervals, offset, limit, false);
  }

  private ContinuousVariableSummaryFactory.Builder getBuilder(Distribution distribution, List<Double> percentiles,
      int intervals, Integer offset) {
    return new ContinuousVariableSummaryFactory.Builder().variable(getVariable()).table(getValueTable())
        .valueSource(getVariableValueSource()).distribution(distribution).percentiles(percentiles).intervals(intervals)
        .offset(offset);
  }

  private Response toResponse(ContinuousVariableSummary summary) {
    SummaryStatisticsDto dto = SummaryStatisticsDto.newBuilder() //
        .setResource(getVariable().getName()) //
        .setExtension(ContinuousSummaryDto.continuous, Dtos.asDto(summary).build()) //
        .build();
    return summary.getOffset() == null && summary.getLimit() == null //
        ? TimestampedResponses.ok(getValueTable(), dto).build() //
        : Response.ok(dto).build();
  }

}
