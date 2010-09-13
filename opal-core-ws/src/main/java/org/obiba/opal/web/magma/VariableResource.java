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

import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.opal.web.math.AbstractSummaryStatisticsResource;
import org.obiba.opal.web.math.CategoricalSummaryStatisticsResource;
import org.obiba.opal.web.math.ContinuousSummaryStatisticsResource;
import org.obiba.opal.web.math.DefaultSummaryStatisticsResource;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class VariableResource {

  private final ValueTable valueTable;

  private final VariableValueSource vvs;

  public VariableResource(ValueTable valueTable, VariableValueSource vvs) {
    this.valueTable = valueTable;
    this.vvs = vvs;
  }

  @GET
  public VariableDto get() {
    return Dtos.asDto(vvs.getVariable()).build();
  }

  @Path("/values")
  public Iterable<ValueDto> getValues() {
    VectorSource vectorSource = vvs.asVectorSource();

    if(vectorSource != null) {
      Iterable<Value> values = vectorSource.getValues(new TreeSet<VariableEntity>(valueTable.getVariableEntities()));
      return Iterables.transform(values, Dtos.valueAsDtoFunc);
    }
    return ImmutableList.of();
  }

  @Path("/summary")
  public AbstractSummaryStatisticsResource getSummary() {
    VectorSource vectorSource = vvs.asVectorSource();

    if(vectorSource != null) {
      if(vvs.getVariable().hasCategories()) {
        return new CategoricalSummaryStatisticsResource(this.valueTable, this.vvs.getVariable(), this.vvs.asVectorSource());
      } else if(vvs.getValueType().isNumeric()) {
        return new ContinuousSummaryStatisticsResource(this.valueTable, this.vvs.getVariable(), this.vvs.asVectorSource());
      }
    }
    return new DefaultSummaryStatisticsResource(this.valueTable, this.vvs.getVariable(), this.vvs.asVectorSource());
  }

}
