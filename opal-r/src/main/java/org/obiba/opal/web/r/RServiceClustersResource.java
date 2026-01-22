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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/service/r/clusters")
@Tag(name = "R", description = "Operations on R")
public class RServiceClustersResource {

  @Autowired
  private RServerManagerService rServerManagerService;

  @GET
  @Operation(
    summary = "Get R server clusters",
    description = "Retrieves a list of all configured R server clusters, sorted alphabetically by name."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R server clusters"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RServerClusterDto> getClusters() {
    return rServerManagerService.getRServerClusters().stream()
        .map(Dtos::asDto)
        .sorted(Comparator.comparing(OpalR.RServerClusterDto::getName))
        .collect(Collectors.toList());
  }

  @DELETE
  @Path("/cache")
  @Operation(
    summary = "Evict clusters cache",
    description = "Clears the R server clusters cache to force refresh of cluster information."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cache successfully evicted"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response evictCache() {
    rServerManagerService.evictCache();
    return Response.ok().build();
  }
}
