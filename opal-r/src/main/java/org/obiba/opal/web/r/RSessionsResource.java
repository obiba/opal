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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

public interface RSessionsResource {

  @GET
  @Operation(summary = "Get R sessions", description = "Retrieve all active R sessions")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R sessions retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<OpalR.RSessionDto> getRSessions();

  @DELETE
  @Operation(summary = "Remove R sessions", description = "Remove all active R sessions")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "R sessions removed successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
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
  @Operation(summary = "Create new R session", description = "Create a new R session and wait for it to be operational")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "R session created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid parameters"),
    @ApiResponse(responseCode = "500", description = "Server error during session creation")
  })
  Response newRSession(@Context UriInfo info, @QueryParam("restore") String restore, @QueryParam("profile") String profile, @QueryParam("wait") @DefaultValue("true") boolean wait);

  @PUT
  @Path("/_test")
  @Operation(summary = "Test R session creation", description = "Test the creation of a new R session with specified profile")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R session test successful"),
    @ApiResponse(responseCode = "400", description = "Invalid profile"),
    @ApiResponse(responseCode = "500", description = "Server error during test")
  })
  Response testNewRSession(@QueryParam("profile") String profile);

  @Autowired
  void setOpalRSessionManager(OpalRSessionManager opalRSessionManager);
}
