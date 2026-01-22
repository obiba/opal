/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.subject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.service.security.IDProvidersService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/system/idproviders")
@Tag(name = "Subjects", description = "Operations on subjects")
public class IDProvidersResource {

  private static final Logger log = LoggerFactory.getLogger(IDProvidersResource.class);

  private final IDProvidersService idProvidersService;

  @Autowired
  public IDProvidersResource(IDProvidersService idProvidersService) {
    this.idProvidersService = idProvidersService;
  }

  @GET
  @Operation(summary = "Get all ID providers", description = "Retrieves all identity provider configurations.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "ID providers retrieved successfully")
  })
  public List<Opal.IDProviderDto> list() {
    return idProvidersService.getConfigurations().stream()
        .map(Dtos::asDto)
        .collect(Collectors.toList());
  }

  @POST
  @Operation(summary = "Create ID provider", description = "Creates a new identity provider configuration.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "ID provider created successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response createOrUpdate(@Context UriInfo uriInfo, Opal.IDProviderDto dto) {
    try {
      idProvidersService.ensureUniqueConfiguration(dto.getName());
      idProvidersService.saveConfiguration(Dtos.fromDto(dto));
      return Response.created(uriInfo.getBaseUriBuilder().path("/system/idprovider/" + dto.getName()).build()).build();
    } catch (IOException e) {
      log.error("Save OIDC configuration failed", e);
      return Response.serverError().build();
    }
  }

}