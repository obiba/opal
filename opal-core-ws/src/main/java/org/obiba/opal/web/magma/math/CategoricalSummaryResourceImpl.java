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

import javax.ws.rs.core.Response;

import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.core.magma.math.CategoricalVariableSummaryFactory;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class CategoricalSummaryResourceImpl extends AbstractSummaryResource implements CategoricalSummaryResource {

  @Override
  public Response get(boolean distinct, Integer offset, Integer limit, Boolean resetCache) {

    CategoricalVariableSummaryFactory summaryFactory = new CategoricalVariableSummaryFactory.Builder()
        .variable(getVariable()).table(getValueTable()).valueSource(getVariableValueSource()).distinct(distinct)
        .offset(offset).limit(limit).build();

    CategoricalVariableSummary summary = variableStatsService
        .getCategoricalSummary(summaryFactory, resetCache.booleanValue());

    SummaryStatisticsDto dto = SummaryStatisticsDto.newBuilder() //
        .setResource(getVariable().getName()) //
        .setExtension(CategoricalSummaryDto.categorical, Dtos.asDto(summary).build()) //
        .build();

    return offset == null && limit == null
        ? TimestampedResponses.ok(getValueTable(), dto).build()
        : Response.ok(dto).build();
  }

}
