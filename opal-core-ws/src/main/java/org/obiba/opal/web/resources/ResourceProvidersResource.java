/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.Resources;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Path("/resource-providers")
@Tag(name = "Resource Providers", description = "Operations on R resource providers")
public class ResourceProvidersResource {

  @Autowired
  private ResourceProvidersService resourceProvidersService;

  @Autowired
  private RServerManagerService rServerManagerService;

@GET
@NoAuthorization
@Operation(summary = "List resource providers", description = "Retrieve a comprehensive list of all available R resource providers and their categories")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Resource providers list successfully retrieved"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Resources.ResourceProvidersDto list() {
    Resources.ResourceProvidersDto.Builder builder = Resources.ResourceProvidersDto.newBuilder();
    builder.addAllProviders(resourceProvidersService.getResourceProviders().stream()
        .map(Dtos::asDto).collect(Collectors.toList()));
    builder.addAllCategories(resourceProvidersService.getAllCategories().stream()
        .map(Dtos::asDto).collect(Collectors.toList()));
    return builder.build();
  }

@DELETE
@Operation(summary = "Initialize resource providers", description = "Reinitialize all resource providers, forcing them to reload their configurations and rediscover available resources")
@ApiResponses({
  @ApiResponse(responseCode = "204", description = "Resource providers successfully initialized"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response init() {
    resourceProvidersService.initResourceProviders();
    return Response.noContent().build();
  }

@GET
@Path("/_status")
@NoAuthorization
@Operation(summary = "Get resource providers status", description = "Retrieve the current status of all resource providers including provider count and R server availability")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Resource providers status successfully retrieved"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Resources.ResourceProvidersStatusDto status() {
    Resources.ResourceProvidersStatusDto.Builder builder = Resources.ResourceProvidersStatusDto.newBuilder()
        .setProvidersCount(resourceProvidersService.getResourceProviders().size())
        .setRServerRunning(rServerManagerService.getDefaultRServer().isRunning());
    return builder.build();
  }

}
