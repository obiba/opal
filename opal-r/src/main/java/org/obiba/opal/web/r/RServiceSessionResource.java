/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

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
