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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.opal.web.magma.support.DefaultPagingVectorSourceImpl;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.magma.support.PagingVectorSource;
import org.obiba.opal.web.math.AbstractSummaryStatisticsResource;
import org.obiba.opal.web.math.CategoricalSummaryStatisticsResource;
import org.obiba.opal.web.math.ContinuousSummaryStatisticsResource;
import org.obiba.opal.web.math.DefaultSummaryStatisticsResource;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;

public class VariableResource {

  private final ValueTable valueTable;

  private final VariableValueSource vvs;

  private PagingVectorSource pagingVectorSource;

  public VariableResource(ValueTable valueTable, VariableValueSource vvs) {
    this.valueTable = valueTable;
    this.vvs = vvs;
  }

  @GET
  public VariableDto get() {
    return Dtos.asDto(vvs.getVariable()).build();
  }

  @GET
  @Path("/values")
  public Iterable<ValueDto> getValues(@QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") @DefaultValue("10") Integer limit) {
    if(limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }

    Iterable<Value> values = getPagingVectorSource().getValues(offset, limit);

    List<ValueDto> valueDtos = new ArrayList<ValueDto>();
    for(Value value : values) {
      valueDtos.add(Dtos.asDto(value).build());
    }
    return valueDtos;
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

  PagingVectorSource getPagingVectorSource() {
    if(pagingVectorSource == null) {
      pagingVectorSource = new DefaultPagingVectorSourceImpl(valueTable, vvs);
    }
    return pagingVectorSource;
  }
}
