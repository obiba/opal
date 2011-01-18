/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.r.MagmaAssignROperation;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Handles web services on the symbols of the current R session of the invoking Opal user. A current R session must be
 * defined, otherwise the web service calls will fail with a 404 status.
 */
@Component
@Scope("request")
@Path("/r/session/current/symbol/{name}")
public class RSymbolResource extends AbstractCurrentOpalRSessionResource {

  @PathParam("name")
  private String name;

  private OpalRSessionManager opalRSessionManager;

  @Autowired
  public RSymbolResource(OpalRSessionManager opalRSessionManager) {
    super();
    this.opalRSessionManager = opalRSessionManager;
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getSymbol() {
    return executeScript(name);
  }

  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  public Response putString(String content) {
    opalRSessionManager.execute(new StringAssignROperation(name, content));
    return Response.created(getSymbolURI()).build();
  }

  @PUT
  @Consumes("application/x-rscript")
  public Response putRScript(String script) {
    opalRSessionManager.execute(new RScriptROperation(name + "<-" + script));
    return Response.created(getSymbolURI()).build();
  }

  @PUT
  @Consumes("application/x-opal")
  public Response putMagma(String path) {
    opalRSessionManager.execute(new MagmaAssignROperation(name, path));
    return Response.created(getSymbolURI()).build();
  }

  @Override
  protected OpalRSessionManager getOpalRSessionManager() {
    return opalRSessionManager;
  }

  private URI getSymbolURI() {
    return UriBuilder.fromPath("/").path(RSymbolResource.class).build(name);
  }

}
