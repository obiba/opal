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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Handles web services on the current R session of the invoking Opal user. A current R session must be defined,
 * otherwise the web service calls will fail with a 404 status.
 */
@Component
@Scope("request")
@Path("/r/session/current")
public class CurrentOpalRSessionResource extends AbstractCurrentOpalRSessionResource {

  private OpalRSessionManager opalRSessionManager;

  @Autowired
  public CurrentOpalRSessionResource(OpalRSessionManager opalRSessionManager) {
    super();
    this.opalRSessionManager = opalRSessionManager;
  }

  @GET
  public OpalR.RSessionDto getRSession() {
    return Dtos.asDto(getCurrentRSessionId());
  }

  @DELETE
  public Response removeRSession() {
    opalRSessionManager.removeSubjectRSession(getCurrentRSessionId());
    return Response.ok().build();
  }

  @PUT
  @Path("/{id}")
  public Response setCurrentRSession(@PathParam("id") String id) {
    opalRSessionManager.setSubjectCurrentRSession(id);
    return Response.ok().build();
  }

  @GET
  @Path("/query")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response query(@QueryParam("script") String script) {
    return executeScript(script);
  }

  @GET
  @Path("/symbols")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response ls() {
    return executeScript("ls()");
  }

  @POST
  @Path("/symbols")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response assign(MultivaluedMap<String, String> symbols) {
    opalRSessionManager.execute(new StringAssignROperation(symbols));
    return Response.ok().build();
  }

  @Override
  protected OpalRSessionManager getOpalRSessionManager() {
    return opalRSessionManager;
  }

}
