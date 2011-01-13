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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.r.service.OpalRSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */

@Component
@Scope("request")
@Path("/r/sessions")
public class OpalRSessionsResource {

  private OpalRSessionManager opalRSessionManager;

  @Autowired
  public OpalRSessionsResource(OpalRSessionManager opalRSessionManager) {
    super();
    this.opalRSessionManager = opalRSessionManager;
  }

  @GET
  public List<String> getRSessionIds() {
    return opalRSessionManager.getSubjectRSessionIds();
  }

  @GET
  @Path("/current")
  public Response getCurrentRSessionId() {
    String id = opalRSessionManager.getSubjectCurrentRSessionId();
    if(id != null) {
      return Response.ok(id).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @POST
  public Response newCurrentRSession() {
    String id = opalRSessionManager.newSubjectCurrentRSession();
    UriBuilder ub = UriBuilder.fromPath("/r/session").path(id);
    return Response.created(ub.build()).entity(id).build();
  }

}
