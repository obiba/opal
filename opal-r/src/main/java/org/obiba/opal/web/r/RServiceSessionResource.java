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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "R", description = "Operations on R")
public class RServiceSessionResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @PathParam("id")
  private String id;

  @GET
  @Operation(
    summary = "Get R session",
    description = "Retrieves detailed information about a specific R session identified by its ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R session"),
    @ApiResponse(responseCode = "404", description = "R session not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public OpalR.RSessionDto getRSession() {
    return Dtos.asDto(opalRSessionManager.getRSession(id));
  }

  @DELETE
  @Operation(
    summary = "Remove R session",
    description = "Removes a specific R session identified by its ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R session successfully removed"),
    @ApiResponse(responseCode = "404", description = "R session not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response removeRSession() {
    opalRSessionManager.removeRSession(id);
    return Response.ok().build();
  }

}
