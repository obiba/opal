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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.services.ServicePluginResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/plugin/{name}")
@Tag(name = "Plugins", description = "Operations on service plugins")
public class PluginResource {

  @PathParam("name")
  private String name;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private PluginsService pluginsService;

  @GET
  @Operation(
    summary = "Get plugin details",
    description = "Retrieves detailed information about a specific installed plugin"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Plugin details retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Plugin not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Plugins.PluginDto get() {
    return Dtos.asDto(pluginsService.getInstalledPlugin(name));
  }

  @DELETE
  @Operation(
    summary = "Uninstall plugin",
    description = "Prepares a plugin for uninstallation"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Plugin uninstallation prepared successfully"),
    @ApiResponse(responseCode = "404", description = "Plugin not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response uninstall() {
    pluginsService.prepareUninstallPlugin(name);
    return Response.noContent().build();
  }

  @PUT
  @Operation(
    summary = "Cancel plugin uninstallation",
    description = "Cancels the uninstallation process for a plugin"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Plugin uninstallation cancelled successfully"),
    @ApiResponse(responseCode = "404", description = "Plugin not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response cancelUninstallation() {
    pluginsService.cancelUninstallPlugin(name);
    return Response.noContent().build();
  }

  @Path("/service")
  public ServicePluginResource service() {
    ServicePluginResource resource = applicationContext.getBean(ServicePluginResource.class);
    resource.setService(name);
    return resource;
  }

  @PUT
  @Path("/cfg")
  @Consumes("text/plain")
  @Operation(
    summary = "Save plugin configuration",
    description = "Saves configuration properties for a specific plugin"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Plugin configuration saved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid configuration properties"),
    @ApiResponse(responseCode = "404", description = "Plugin not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response saveConfig(String properties) {
    pluginsService.setInstalledPluginSiteProperties(name, properties);
    return Response.ok().build();
  }

}
