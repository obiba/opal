/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.ListClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ohs.ExcelVariablesClause;
import org.obiba.opal.web.math.AbstractSummaryStatisticsResource;
import org.obiba.opal.web.math.CategoricalSummaryStatisticsResource;
import org.obiba.opal.web.math.ContinuousSummaryStatisticsResource;
import org.obiba.opal.web.math.DefaultSummaryStatisticsResource;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class VariableResource {

  private static final Logger log = LoggerFactory.getLogger(VariableResource.class);

  private final ValueTable valueTable;

  private final VariableValueSource vvs;

  public VariableResource(ValueTable valueTable, VariableValueSource vvs) {
    this.valueTable = valueTable;
    this.vvs = vvs;
  }

  @GET
  @Produces("application/xml")
  public Variable get() {
    return vvs.getVariable();
  }

  @PUT
  public Response write(VariableDto dto) throws IOException {
    log.info("writing to {}", valueTable.getDatasource().getName() + "." + valueTable.getName() + ":" + dto.getName());
    if(valueTable instanceof View) {
      View view = (View) valueTable;
      log.info("writing to view.");
      ListClause clause = view.getListClause();
      if(clause instanceof ExcelVariablesClause) {
        ExcelVariablesClause excelClause = (ExcelVariablesClause) clause;
        log.info("writing to ExcelVariablesClause {}", excelClause.getTable());
        ValueTableWriter writer = excelClause.getDatasource().createWriter(excelClause.getTable(), valueTable.getEntityType());
        VariableWriter vw = writer.writeVariables();
        vw.writeVariable(Dtos.fromDto(dto));
        vw.close();
        writer.close();
      }
      // This is a HUGE hack. We have to do this so that the list clause re-reads the variables.
      // This would not be required if we were writing through the View or the ListClause itself.
      Disposables.dispose(clause);
      Initialisables.initialise(clause);
    }
    return Response.ok().build();
  }

  @GET
  @Path("/values")
  public Collection<ValueDto> getValues(@QueryParam("limit") @DefaultValue("10") Integer limit) {
    ImmutableList.Builder<ValueDto> values = ImmutableList.builder();
    VectorSource vectorSource = vvs.asVectorSource();
    if(vectorSource != null) {
      int i = 0;
      for(Value value : vectorSource.getValues(Sets.newTreeSet(valueTable.getVariableEntities()))) {
        ValueDto.Builder valueBuilder = ValueDto.newBuilder().setValueType(vvs.getValueType().getName()).setIsSequence(value.isSequence());
        if(value.isNull() == false) {
          valueBuilder.setValue(value.toString());
        }
        values.add(valueBuilder.build());
        if(limit >= 0 && i++ >= limit) break;
      }
    }
    return values.build();
  }

  @Path("/summary")
  public AbstractSummaryStatisticsResource getSummary() {
    VectorSource vectorSource = vvs.asVectorSource();

    if(vectorSource != null) {
      if(vvs.getVariable().hasCategories()) {
        return new CategoricalSummaryStatisticsResource(this.valueTable, this.vvs.getVariable(), this.vvs.asVectorSource());
      } else if(vvs.getVariable().getValueType().isNumeric()) {
        return new ContinuousSummaryStatisticsResource(this.valueTable, this.vvs.getVariable(), this.vvs.asVectorSource());
      }
    }
    return new DefaultSummaryStatisticsResource(this.valueTable, this.vvs.getVariable(), this.vvs.asVectorSource());
  }

}
