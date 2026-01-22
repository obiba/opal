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
import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.web.model.Resources;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.stream.Collectors;

@Component
@Path("/resource-provider/{name}")
@Scope("request")
@Tag(name = "Resource Providers", description = "Operations on R resource providers")
public class ResourceProviderResource {

  @Autowired
  private ResourceProvidersService resourceProvidersService;

  @PathParam("name")
  private String name;

@GET
@NoAuthorization
@Operation(summary = "Get resource provider", description = "Retrieve detailed information about a specific R resource provider including its configuration and capabilities")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Resource provider information successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "Resource provider not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Resources.ResourceProviderDto getResourceProvider() {
    return Dtos.asDto(resourceProvidersService.getResourceProvider(name));
  }

@GET
@NoAuthorization
@Path("/factory/{type}")
@Operation(summary = "Get resource factory", description = "Retrieve information about a specific resource factory type within a resource provider, including its configuration schema and parameters")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Resource factory information successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "Resource provider or factory type not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Resources.ResourceFactoryDto getResourceFactory(@PathParam("type") String type) {
    return Dtos.asDto(resourceProvidersService.getResourceFactory(name, type));
  }
}
