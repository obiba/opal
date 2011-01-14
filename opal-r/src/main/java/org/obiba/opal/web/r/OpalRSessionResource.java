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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.r.service.NoSuchRSessionException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
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

  @DELETE
  public Response removeRSession() {
    try {
      opalRSessionManager.removeSubjectRSession(id);
      return Response.ok().build();
    } catch(NoSuchRSessionException e) {
      return Response.status(Status.NOT_FOUND).build();
    } catch(Exception e) {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

}
