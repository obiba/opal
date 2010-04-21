/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.datashield;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.r.ParrallelVectorBuilder;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.service.datashield.DatashieldService;
import org.obiba.opal.service.datashield.DatashieldService.RTemplate;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
@Path("/datashield")
public class DatashieldResource {

  /**
   * Accepted R aggregation methods
   */
  public enum Aggregation {
    summary, length, coefficients
  }

  @Autowired
  private DatashieldService datashieldService;

  public DatashieldResource() {

  }

  @POST
  @Path("/R")
  @Produces("text/plain")
  public Response createSession(@Context UriInfo uriInfo) {
    String sessionId = this.datashieldService.createSession();
    return Response.created(UriBuilder.fromUri(uriInfo.getRequestUri()).segment(sessionId).build()).entity(sessionId).build();
  }

  @POST
  @Path("/R/{sessionId}/aggregate/{function}")
  @Consumes("application/x-www-form-urlencoded")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response eval(@PathParam("sessionId") String sessionId, @PathParam("function") Aggregation function, @FormParam("script") String script, @FormParam("aggregate") String aggregate) {
    RTemplate template = this.datashieldService.newTemplate(sessionId);
    REXP result = template.parseAndEval(script);

    REXP rawResult = template.parseAndEval("serialize(" + function.toString() + "(" + aggregate + "), NULL)");
    try {
      return Response.ok(rawResult.asBytes(), MediaType.APPLICATION_OCTET_STREAM).build();
    } catch(REXPMismatchException e) {
      throw new RuntimeException(e);
    }
  }

  @POST
  @Path("/R/{sessionId}/assign")
  @Consumes("application/x-www-form-urlencoded")
  @Produces("application/xml")
  public Response load(@PathParam("sessionId") String sessionId, MultivaluedMap<String, String> formParameters) {
    RTemplate template = this.datashieldService.newTemplate(sessionId);

    ParrallelVectorBuilder builder = new ParrallelVectorBuilder();
    for(String symbol : formParameters.keySet()) {
      String variableName = formParameters.getFirst(symbol);
      MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(variableName);
      builder.add(resolver.resolveSource(), symbol);
      builder.addValues(resolver.resolveTable(null).getValueSets());
    }

    Map<String, Integer> lengths = Maps.newHashMap();
    Map<String, REXP> vectors = builder.build();
    for(String symbol : formParameters.keySet()) {
      template.assign(symbol, vectors.get(symbol));
      try {
        lengths.put(symbol, vectors.get(symbol).length());
      } catch(REXPMismatchException e) {
        throw new RuntimeException(e);
      }
    }
    return Response.ok(lengths).build();
  }
}
