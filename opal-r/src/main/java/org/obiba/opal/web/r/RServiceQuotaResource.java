/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.r.service.RQuotaService;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A single R usage quota.
 */
@Component
@Scope("request")
@Path("/service/r/quota/{id}")
@Tag(name = "R", description = "Operations on R")
@Transactional
public class RServiceQuotaResource {

  @PathParam("id")
  private long id;

  private final RQuotaService rQuotaService;

  @Autowired
  public RServiceQuotaResource(RQuotaService rQuotaService) {
    this.rQuotaService = rQuotaService;
  }

  @GET
  @Operation(summary = "Get a R quota", description = "Retrieves a R usage quota.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the R quota", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "404", description = "No such R quota")
  })
  public OpalR.RQuotaDto get() {
    return Dtos.asDto(rQuotaService.getQuota(id));
  }

  @PUT
  @Operation(summary = "Update a R quota", description = "Updates a R usage quota.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "R quota updated", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "Invalid quota"),
      @ApiResponse(responseCode = "404", description = "No such R quota")
  })
  public OpalR.RQuotaDto update(OpalR.RQuotaDto dto) {
    return Dtos.asDto(rQuotaService.updateQuota(id, Dtos.fromDto(dto)));
  }

  @DELETE
  @Operation(summary = "Delete a R quota", description = "Deletes a R usage quota. Deleting the one that applied makes the subject unlimited again, unless a broader quota still matches.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "R quota deleted")
  })
  public Response delete() {
    rQuotaService.deleteQuota(id);
    return Response.ok().build();
  }
}
