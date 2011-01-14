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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.r.ScriptROperation;
import org.obiba.opal.r.service.NoSuchRSessionException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Scope("request")
@Path("/r/session/current")
public class OpalCurrentRSessionResource {

  private static final Logger log = LoggerFactory.getLogger(OpalCurrentRSessionResource.class);

  private OpalRSessionManager opalRSessionManager;

  @Autowired
  public OpalCurrentRSessionResource(OpalRSessionManager opalRSessionManager) {
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
  @Produces("application/octet-stream")
  public Response execute(@QueryParam("script") String script) {
    if(script == null) return Response.status(Status.BAD_REQUEST).build();
    if(!opalRSessionManager.hasSubjectCurrentRSession()) throw new NoSuchRSessionException();

    ScriptROperation rop = new ScriptROperation(script);
    opalRSessionManager.execute(rop);
    if(rop.hasResult() && rop.hasRawResult()) {
      return Response.ok().entity(rop.getRawResult().asBytes()).build();
    } else {
      log.error("R Script '{}' has result: {}, has raw result: {}", new Object[] { script, rop.hasResult(), rop.hasRawResult() });
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  private String getCurrentRSessionId() {
    return opalRSessionManager.getSubjectCurrentRSessionId();
  }

}
