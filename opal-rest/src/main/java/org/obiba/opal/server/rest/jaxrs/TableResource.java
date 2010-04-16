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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component("jersey.tableResource")
@Scope("prototype")
@PerRequest
public class TableResource {

  @Context
  private ResourceContext resourceContext;

  private ValueTable valueTable;

  public TableResource() {
  }

  public void setValueTable(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @GET
  public Map<String, String> details() {
    return ImmutableMap.of("name", valueTable.getName(), "entityType", valueTable.getEntityType());
  }

  @GET
  @Path("/variables")
  @Produces("application/xml")
  public Iterable<Variable> getVariables() {
    return valueTable.getVariables();
  }

  @GET
  @Path("/entities")
  @Produces("application/xml")
  public Set<String> getEntities() {
    return ImmutableSet.copyOf(Iterables.transform(valueTable.getValueSets(), new Function<ValueSet, String>() {
      @Override
      public String apply(ValueSet from) {
        return from.getVariableEntity().getIdentifier();
      }
    }));
  }

  @GET
  @Path("/values.json")
  @Produces("application/json")
  public Response getValuesAsJson(@QueryParam("v") List<String> variables) {
    return Response.ok(new JSONObject(readValues(variables)).toString()).build();
  }

  @GET
  @Path("/values.xml")
  @Produces("application/xml")
  public Map<String, List<Object>> getValuesAsXml(@QueryParam("v") List<String> variables) {
    return readValues(variables);
  }

  @Path("/{variable}")
  public VariableResource getVariable(@PathParam("variable") String name) {
    VariableResource resource = resourceContext.getResource(VariableResource.class);
    resource.setValueTable(valueTable);
    resource.setVariableValueSource(valueTable.getVariableValueSource(name));
    return resource;
  }

  private Map<String, List<Object>> readValues(List<String> variables) {
    Map<String, List<Object>> response = new LinkedHashMap<String, List<Object>>();

    if(variables.size() == 0) {
      variables = ImmutableList.copyOf(Iterables.transform(valueTable.getVariables(), new Function<Variable, String>() {
        @Override
        public String apply(Variable from) {
          return from.getName();
        }
      }));
    }

    for(String name : variables) {
      response.put(name, new LinkedList<Object>());
    }

    for(ValueSet vs : valueTable.getValueSets()) {
      for(String name : response.keySet()) {
        response.get(name).add(valueTable.getVariableValueSource(name).getValue(vs).getValue());
      }
    }
    return response;
  }
}
