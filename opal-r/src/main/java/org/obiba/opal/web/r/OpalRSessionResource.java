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
import javax.ws.rs.core.Response;

import org.obiba.opal.r.service.NoSuchRSessionException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Handles web services on a particular R session of the invoking Opal user.
 */
@Component
@Scope("request")
@Path("/r/session/{id}")
public class OpalRSessionResource {

  private OpalRSessionManager opalRSessionManager;

  @PathParam("id")
  private String id;

  @Autowired
  public OpalRSessionResource(OpalRSessionManager opalRSessionManager) {
    super();
    this.opalRSessionManager = opalRSessionManager;
  }

  @GET
  public OpalR.RSessionDto getRSession() {
    if(!opalRSessionManager.hasSubjectRSession(id)) throw new NoSuchRSessionException(id);
    return Dtos.asDto(id);
  }

  @DELETE
  public Response removeRSession() {
    opalRSessionManager.removeSubjectRSession(id);
    return Response.ok().build();
  }

  @PUT
  @Path("/current")
  public Response setCurrentRSession() {
    opalRSessionManager.setSubjectCurrentRSession(id);
    return Response.ok().build();
  }

}
