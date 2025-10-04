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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

public interface RSessionsResource {

  @GET
  List<OpalR.RSessionDto> getRSessions();

  @DELETE
  Response removeRSessions();

  /**
   * Create a new R session and wait for it to be operational.
   *
   * @param info
   * @param restore
   * @param profile
   * @param wait
   * @return
   */
  @POST
  Response newRSession(@Context UriInfo info, @QueryParam("restore") String restore, @QueryParam("profile") String profile, @QueryParam("wait") @DefaultValue("true") boolean wait);

  @PUT
  @Path("/_test")
  Response testNewRSession(@QueryParam("profile") String profile);

  @Autowired
  void setOpalRSessionManager(OpalRSessionManager opalRSessionManager);
}
