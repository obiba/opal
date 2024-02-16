/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.math;

import jakarta.ws.rs.core.Response;

import org.obiba.magma.math.summary.GeoVariableSummary;
import org.obiba.magma.math.summary.GeoVariableSummaryFactory;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@Transactional
public class GeoSummaryResourceImpl extends AbstractSummaryResource implements GeoSummaryResource {

  @Override
  public Response get(Integer offset, Integer limit, boolean fullIfCached, boolean resetCache) {
    GeoVariableSummaryFactory summaryFactory = new GeoVariableSummaryFactory.Builder() //
        .variable(getVariable()) //
        .table(getValueTable()) //
        .valueSource(getVariableValueSource()) //
        .offset(offset).build();

    GeoVariableSummary summary;
    if(fullIfCached && variableSummaryService.isSummaryCached(summaryFactory)) {
      summary = variableSummaryService.getSummary(summaryFactory, resetCache);
    } else {
      summaryFactory.setLimit(limit);
      summary = variableSummaryService.getSummary(summaryFactory, resetCache);
    }
    SummaryStatisticsDto.Builder dtoBuilder = SummaryStatisticsDto.newBuilder() //
        .setResource(getVariable().getName()) //
        .setExtension(Math.GeoSummaryDto.geoSummary, Dtos.asDto(summary).build());
    if(summary.getLimit() != null) dtoBuilder.setLimit(summary.getLimit());
    SummaryStatisticsDto dto = dtoBuilder.build();
    return summary.getOffset() == null && summary.getLimit() == null //
        ? TimestampedResponses.ok(getValueTable(), dto).build() //
        : Response.ok(dto).build();
  }

}
