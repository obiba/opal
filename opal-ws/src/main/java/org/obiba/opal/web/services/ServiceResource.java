/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.services;

import java.net.URI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Scope("request")
@Path("/service/{name}")
@Tag(name = "Services", description = "Operations on services")
public class ServiceResource {

  @PathParam("name")
  private String name;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private ServiceConfigurationHandlerRegistry configHandler;

  @GET
  @Operation(summary = "Get service information", description = "Returns detailed information about a specific service including its status and configuration link.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Service information successfully retrieved"),
      @ApiResponse(responseCode = "404", description = "Service not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response service() {

    Service service = opalRuntime.getService(name);

    Opal.ServiceStatus status = service.isRunning() ? Opal.ServiceStatus.RUNNING : Opal.ServiceStatus.STOPPED;
    URI link = UriBuilder.fromPath("/").path(ServiceResource.class).build(service.getName());

    Opal.ServiceDto dto = Opal.ServiceDto.newBuilder().setName(service.getName()).setStatus(status)
        .setLink(link.getPath()).build();

    return Response.ok().entity(dto).build();
  }

  @PUT
  @Operation(summary = "Start service", description = "Starts a specific service that is currently stopped.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Service successfully started"),
      @ApiResponse(responseCode = "404", description = "Service not found"),
      @ApiResponse(responseCode = "409", description = "Service is already running"),
      @ApiResponse(responseCode = "500", description = "Internal server error or failed to start service")
  })
  public Response start() {

    Service service = opalRuntime.getService(name);
    service.start();

    return Response.ok().build();
  }

  @DELETE
  @Operation(summary = "Stop service", description = "Stops a specific service that is currently running.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Service successfully stopped"),
      @ApiResponse(responseCode = "404", description = "Service not found"),
      @ApiResponse(responseCode = "409", description = "Service is already stopped"),
      @ApiResponse(responseCode = "500", description = "Internal server error or failed to stop service")
  })
  public Response stop() {

    Service service = opalRuntime.getService(name);
    service.stop();

    return Response.ok().build();
  }

  @GET
  @Path("/cfg")
  @Operation(summary = "Get service configuration", description = "Returns the configuration details of a specific service.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Service configuration successfully retrieved"),
      @ApiResponse(responseCode = "404", description = "Service not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getConfig() {
    Service service = opalRuntime.getService(name);

    Opal.ServiceCfgDto dto = configHandler.get(service.getConfig(), name);

    return Response.ok().entity(dto).build();
  }

  @PUT
  @Path("/cfg")
  @Operation(summary = "Save service configuration", description = "Updates and saves the configuration for a specific service.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Service configuration successfully saved"),
      @ApiResponse(responseCode = "400", description = "Invalid configuration data"),
      @ApiResponse(responseCode = "404", description = "Service not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error or failed to save configuration")
  })
  public Response saveConfig(Opal.ServiceCfgDto serviceDto) {

    opalRuntime.getService(name);
    configHandler.put(serviceDto, name);

    return Response.ok().build();

  }
}
