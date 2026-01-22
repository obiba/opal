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

import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.web.model.OpalR;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;

public interface RSessionResource {

  void setRServerSession(RServerSession rSession);

  @GET
  @Operation(summary = "Get R session", description = "Retrieve the R session details")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R session retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "R session not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  OpalR.RSessionDto getRSession();

  /**
   * Destroy the R session and optionally save the associated workspace.
   *
   * @param saveId
   * @return
   */
  @DELETE
  @Operation(summary = "Destroy R session", description = "Destroy the R session and optionally save the associated workspace")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "R session destroyed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid save ID"),
    @ApiResponse(responseCode = "500", description = "Server error during destruction")
  })
  Response removeRSession(@QueryParam("save") String saveId);

  @PUT
  @Path("/current")
  @Operation(summary = "Set current R session", description = "Set this R session as the current active session")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R session set as current successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response setCurrentRSession();

  @GET
  @Path("/symbols")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(summary = "List R symbols as binary", description = "List all symbols in the R session workspace in binary format")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Symbols listed successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response lsBinary();

  @GET
  @Path("/symbols")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List R symbols as JSON", description = "List all symbols in the R session workspace in JSON format")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Symbols listed successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response lsJSON();

  @POST
  @Path("/symbols")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Operation(summary = "Assign multiple symbols", description = "Assign multiple symbols to the R session workspace")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Symbols assigned successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid symbol data"),
    @ApiResponse(responseCode = "500", description = "Server error during assignment")
  })
  Response assign(MultivaluedMap<String, String> symbols);

  @Path("/symbol/{name}")
  RSymbolResource getRSymbolResource(@PathParam("name") String name);

  @GET
  @Path("/commands")
  @Operation(summary = "Get R commands", description = "Retrieve all R commands executed in this session")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Commands retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<OpalR.RCommandDto> getRCommands();

  @GET
  @Path("/command/{rid}")
  @Operation(summary = "Get R command", description = "Retrieve a specific R command by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Command retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  OpalR.RCommandDto getRCommand(@PathParam("rid") String rid, @QueryParam("wait") @DefaultValue("false") boolean wait);

  @DELETE
  @Path("/command/{rid}")
  @Operation(summary = "Remove R command", description = "Remove a specific R command from history")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Command removed successfully"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response removeRCommand(@PathParam("rid") String rid);

  @GET
  @Path("/command/{rid}/result")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(summary = "Get R command result as binary", description = "Retrieve R command execution result in binary format")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Result retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getRCommandResultRaw(@PathParam("rid") String rid, @QueryParam("rm") @DefaultValue("true") boolean remove,
                             @QueryParam("wait") @DefaultValue("false") boolean wait);


  @GET
  @Path("/command/{rid}/result")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get R command result as JSON", description = "Retrieve R command execution result in JSON format")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Result retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getRCommandResultJSON(@PathParam("rid") String rid, @QueryParam("rm") @DefaultValue("true") boolean remove,
                             @QueryParam("wait") @DefaultValue("false") boolean wait);

  @POST
  @Path("/workspaces")
  @Operation(summary = "Save workspace", description = "Save the current R session workspace")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Workspace saved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid save ID"),
    @ApiResponse(responseCode = "500", description = "Server error during save")
  })
  Response saveWorkspace(@QueryParam("save") String saveId);

  @PUT
  @Path("/workspace/{wid}")
  @Operation(summary = "Restore workspace", description = "Restore a previously saved workspace to the R session")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Workspace restored successfully"),
    @ApiResponse(responseCode = "404", description = "Workspace not found"),
    @ApiResponse(responseCode = "500", description = "Server error during restore")
  })
  Response restoreWorkspace(@PathParam("wid") String workspaceId);
}
