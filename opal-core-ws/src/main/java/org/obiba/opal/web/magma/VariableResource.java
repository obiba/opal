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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.web.magma.support.DefaultPagingVectorSourceImpl;
import org.obiba.opal.web.magma.support.PagingVectorSource;
import org.obiba.opal.web.math.AbstractSummaryStatisticsResource;
import org.obiba.opal.web.math.SummaryStatisticsResourceFactory;
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

  @Path("/summary")
  public AbstractSummaryStatisticsResource getSummary(@QueryParam("nature") String nature) {
    return new SummaryStatisticsResourceFactory().getResource(this.valueTable, this.vvs, nature);
  }

  VariableValueSource getVariableValueSource() {
    return vvs;
  }

  PagingVectorSource getPagingVectorSource() {
    if(pagingVectorSource == null) {
      pagingVectorSource = new DefaultPagingVectorSourceImpl(valueTable, vvs);
    }
    return pagingVectorSource;
  }

}
