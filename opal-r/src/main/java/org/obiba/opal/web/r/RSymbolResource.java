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

import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.r.service.RCacheHelper;
import org.obiba.opal.r.service.RServerSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

public interface RSymbolResource {

  void setName(String name);

  void setRServerSession(RServerSession rSession);

  void setIdentifiersTableService(IdentifiersTableService identifiersTableService);

  void setDataExportService(DataExportService dataExportService);

  void setRCacheHelper(RCacheHelper rCacheHelper);

  void setResourceReferenceService(ResourceReferenceService resourceReferenceService);

  String getName();

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(summary = "Get R symbol as binary", description = "Retrieve the R symbol value in binary format")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Symbol retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Symbol not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getSymbolBinary();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get R symbol as JSON", description = "Retrieve the R symbol value in JSON format")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Symbol retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Symbol not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getSymbolJSON();

  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  @Operation(summary = "Assign string to R symbol", description = "Assign a string value to the R symbol")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "String assigned successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid content"),
    @ApiResponse(responseCode = "500", description = "Server error during assignment")
  })
  Response putString(@Context UriInfo uri, String content, @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Consumes("application/x-rscript")
  @Operation(summary = "Execute R script for symbol", description = "Execute an R script and assign the result to the symbol")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Script executed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid script"),
    @ApiResponse(responseCode = "500", description = "Server error during execution")
  })
  Response putRScript(@Context UriInfo uri, String script, @QueryParam("async") @DefaultValue("false") boolean async) throws Exception;

  /**
   * Assign value table variables to a R data.frame.
   *
   * @param uri
   * @param path
   * @param variableFilter
   * @param withMissings       Include values corresponding to "missing" categories.
   * @param idName             Include the entity ID as a column.
   * @param identifiersMapping Identifiers mapping to be used.
   * @param rClass             R data frame class (can be "data.frame" (default) or "tibble").
   * @param async
   * @return
   */
  @PUT
  @Consumes("application/x-opal")
  @Operation(summary = "Assign data table to R symbol", description = "Assign value table variables to a R data.frame symbol")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Data assigned successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid parameters or data"),
    @ApiResponse(responseCode = "404", description = "Data table not found"),
    @ApiResponse(responseCode = "500", description = "Server error during assignment")
  })
  Response putMagma(@Context UriInfo uri, String path, @QueryParam("variables") String variableFilter,
                    @QueryParam("missings") @DefaultValue("false") Boolean withMissings, @QueryParam("id") String idName,
                    @QueryParam("identifiers") String identifiersMapping,
                    @QueryParam("class") @DefaultValue("data.frame") String rClass,
                    @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Path("/table/{path}")
  @Operation(summary = "Assign table to R symbol", description = "Assign a specific table's variables to a R data.frame symbol")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Table assigned successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid parameters"),
    @ApiResponse(responseCode = "404", description = "Table not found"),
    @ApiResponse(responseCode = "500", description = "Server error during assignment")
  })
  Response putTable(@Context UriInfo uri, @PathParam("path") String path, @QueryParam("variables") String variableFilter,
                    @QueryParam("missings") @DefaultValue("false") Boolean withMissings, @QueryParam("id") String idName,
                    @QueryParam("identifiers") String identifiersMapping,
                    @QueryParam("class") @DefaultValue("data.frame") String rClass,
                    @QueryParam("async") @DefaultValue("false") boolean async);

  @PUT
  @Path("/resource/{path}")
  @Operation(summary = "Assign resource to R symbol", description = "Assign a resource to the R symbol")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resource assigned successfully"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "500", description = "Server error during assignment")
  })
  Response putResource(@Context UriInfo uri, @PathParam("path") String path,
                       @QueryParam("async") @DefaultValue("false") boolean async);

  @DELETE
  @Operation(summary = "Remove R symbol", description = "Remove the R symbol from the workspace")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Symbol removed successfully"),
    @ApiResponse(responseCode = "404", description = "Symbol not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response rm();

}
