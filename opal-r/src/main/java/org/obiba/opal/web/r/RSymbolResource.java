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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.r.MagmaAssignROperation;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.OpalRSession;

/**
 * Handles web services on the symbols of the current R session of the invoking Opal user. A current R session must be
 * defined, otherwise the web service calls will fail with a 404 status.
 */
public class RSymbolResource extends AbstractOpalRSessionResource {

  private final String name;

  private final OpalRSession rSession;

  public RSymbolResource(OpalRSession rSession, String name) {
    super();
    this.rSession = rSession;
    this.name = name;
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getSymbol() {
    return executeScript(rSession, name);
  }

  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  public Response putString(@Context UriInfo uri, String content) {
    rSession.execute(new StringAssignROperation(name, content));
    return Response.created(getSymbolURI(uri)).build();
  }

  @PUT
  @Consumes("application/x-rscript")
  public Response putRScript(@Context UriInfo uri, String script) {
    rSession.execute(new RScriptROperation(name + "<-" + script));
    return Response.created(getSymbolURI(uri)).build();
  }

  @PUT
  @Consumes("application/x-opal")
  public Response putMagma(@Context UriInfo uri, String path) {
    rSession.execute(new MagmaAssignROperation(rSession, name, path));
    return Response.created(getSymbolURI(uri)).build();
  }

  @DELETE
  public Response rm() {
    rSession.execute(new RScriptROperation("base::rm(" + name + ")"));
    return Response.ok().build();
  }

  protected URI getSymbolURI(UriInfo info) {
    return info.getRequestUri();
  }
}
