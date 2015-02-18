/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/service/r/session/{id}")
public class RServiceSessionResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @PathParam("id")
  private String id;

  @GET
  public OpalR.RSessionDto getRSession() {
    return Dtos.asDto(opalRSessionManager.getRSession(id));
  }

  @DELETE
  public Response removeRSession() {
    opalRSessionManager.removeRSession(id);
    return Response.ok().build();
  }

}
