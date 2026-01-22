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

import org.apache.commons.vfs2.FileSystemException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface OpalRSessionResource extends RSessionResource {

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(summary = "Execute R script", description = "Execute an R script in the Opal R session and return binary results")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Script executed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid script"),
    @ApiResponse(responseCode = "500", description = "Server error during execution")
  })
  Response executeBinary(@QueryParam("script") String script, @QueryParam("async") @DefaultValue("false") boolean async,
                   String body);

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Execute R script as JSON", description = "Execute an R script in the Opal R session and return JSON results")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Script executed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid script"),
    @ApiResponse(responseCode = "500", description = "Server error during execution")
  })
  Response executeJSON(@QueryParam("script") String script, @QueryParam("async") @DefaultValue("false") boolean async,
                         String body);


  /**
   * Push a file from the opal file system into the R session workspace.
   *
   * @param source
   * @param destination
   * @return
   */
  @PUT
  @Path("/file/_push")
  @Operation(summary = "Push file to R session", description = "Push a file from the opal file system into the R session workspace")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "File pushed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid file path"),
    @ApiResponse(responseCode = "404", description = "Source file not found"),
    @ApiResponse(responseCode = "500", description = "Server error during file transfer")
  })
  Response pushFile(@QueryParam("source") String source, @QueryParam("destination") String destination) throws FileSystemException;

  /**
   * Pull a file from the R session workspace to the opal file system.
   *
   * @param source
   * @param destination
   * @return
   */
  @PUT
  @Path("/file/_pull")
  @Operation(summary = "Pull file from R session", description = "Pull a file from the R session workspace to the opal file system")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "File pulled successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid file path"),
    @ApiResponse(responseCode = "404", description = "Source file not found"),
    @ApiResponse(responseCode = "500", description = "Server error during file transfer")
  })
  Response pullFile(@QueryParam("source") String source, @QueryParam("destination") String destination) throws FileSystemException;

}
