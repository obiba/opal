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

import org.obiba.opal.core.magma.math.ContinuousVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary.Distribution;
import org.obiba.opal.core.magma.math.ContinuousVariableSummaryFactory;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.ContinuousSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class ContinuousSummaryResourceImpl extends AbstractSummaryResource implements ContinuousSummaryResource {

  @Override
  public Response get(Distribution distribution, List<Double> percentiles, int intervals, Integer offset, Integer limit,
      Boolean resetCache) {

    ContinuousVariableSummaryFactory summaryFactory = new ContinuousVariableSummaryFactory.Builder()
        .variable(getVariable()).table(getValueTable()).valueSource(getVariableValueSource()).distribution(distribution)
        .percentiles(percentiles).intervals(intervals).offset(offset).limit(limit).build();

    ContinuousVariableSummary summary = variableStatsService.getContinuousSummary(summaryFactory, resetCache);

    SummaryStatisticsDto dto = SummaryStatisticsDto.newBuilder() //
        .setResource(getVariable().getName()) //
        .setExtension(ContinuousSummaryDto.continuous, Dtos.asDto(summary).build()) //
        .build();

    return offset == null && limit == null
        ? TimestampedResponses.ok(getValueTable(), dto).build()
        : Response.ok(dto).build();

  }

}
