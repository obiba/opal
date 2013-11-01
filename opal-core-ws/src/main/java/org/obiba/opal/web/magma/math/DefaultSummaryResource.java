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

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.core.service.VariableStatsService;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

/**
 *
 */
public class DefaultSummaryResource extends AbstractSummaryResource {

  public DefaultSummaryResource(@NotNull VariableStatsService variableStatsService, ValueTable valueTable,
      Variable variable, VariableValueSource variableValueSource) {
    super(variableStatsService, valueTable, variable, variableValueSource);
  }

  @GET
  @POST
  public SummaryStatisticsDto compute() {
    return SummaryStatisticsDto.newBuilder().setResource(getVariable().getName()).build();
  }

}
