/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.jaxrs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.PerRequest;

@Component("jersey.variableResource")
@Scope("prototype")
@PerRequest
public class VariableResource {

  private ValueTable valueTable;

  private VariableValueSource vvs;

  public VariableResource() {
  }

  public void setValueTable(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  public void setVariableValueSource(VariableValueSource vvs) {
    this.vvs = vvs;
  }

  @GET
  @Path("/values.json")
  @Produces("application/json")
  public Response getValues() {
    Map<String, Object> response = new HashMap<String, Object>();

    List<Object> values = new LinkedList<Object>();
    for(ValueSet vs : valueTable.getValueSets()) {
      values.add(vvs.getValue(vs).getValue());
    }
    response.put(vvs.getVariable().getName(), values);

    return Response.ok(new JSONObject(response).toString()).build();
  }

}
