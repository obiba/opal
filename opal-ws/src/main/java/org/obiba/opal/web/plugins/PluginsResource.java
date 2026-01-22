/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.plugins;


import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.stream.Collectors;

@Component
@Path("/plugins")
@Tag(name = "Plugins", description = "Operations on service plugins")
public class PluginsResource {

  @Autowired
  PluginsService pluginsService;

  @GET
  @NoAuthorization
  @Operation(
    summary = "List installed plugins",
    description = "Retrieves a list of installed plugins with optional filtering by type"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Installed plugins list retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid type parameter"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Plugins.PluginPackagesDto getInstalledPlugins(@QueryParam("type") String type) {
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired(),
        pluginsService.getInstalledPlugins().stream()
            .filter(p -> Strings.isNullOrEmpty(type) || (type.equals(p.getType())))
            .collect(Collectors.toList()), pluginsService.getUninstalledPluginNames());
  }

  @GET
  @Path("/_updates")
  @Operation(
    summary = "List updatable plugins",
    description = "Retrieves a list of plugins that have available updates with optional filtering by type"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Updatable plugins list retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid type parameter"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Plugins.PluginPackagesDto getUpdatablePlugins(@QueryParam("type") String type) {
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired(),
        pluginsService.getUpdatablePlugins().stream()
            .filter(p -> Strings.isNullOrEmpty(type) || (type.equals(p.getType())))
            .collect(Collectors.toList()));
  }

  @GET
  @Path("/_available")
  @Operation(
    summary = "List available plugins",
    description = "Retrieves a list of plugins available for installation with optional filtering by type"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Available plugins list retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid type parameter"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Plugins.PluginPackagesDto getAvailablePlugins(@QueryParam("type") String type) {
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired(),
        pluginsService.getAvailablePlugins().stream()
            .filter(p -> Strings.isNullOrEmpty(type) || (type.equals(p.getType())))
            .collect(Collectors.toList()));
  }

  @POST
  @Operation(
    summary = "Install plugin",
    description = "Installs a plugin either by name and version or from a file"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Plugin installation initiated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response installPlugin(@QueryParam("name") String name, @QueryParam("version") String version, @QueryParam("file") String file) {
    if (!Strings.isNullOrEmpty(name)) {
      pluginsService.installPlugin(name, version);
      return Response.ok().build();
    }
    if (!Strings.isNullOrEmpty(file)) {
      pluginsService.installPlugin(file);
      return Response.ok().build();
    }
    return Response.status(Response.Status.BAD_REQUEST).build();
  }

}
