/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Component
@Transactional
@Scope("request")
@Path("/datashield/option")
@Tag(name = "DataSHIELD", description = "Operations on DataSHIELD")
public class DataShieldROptionResourceImpl implements DataShieldROptionResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @DELETE
  @Operation(
    summary = "Delete DataSHIELD R option",
    description = "Removes a specific R option from the DataSHIELD profile configuration by option name."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully deleted R option"),
    @ApiResponse(responseCode = "404", description = "DataSHIELD profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Override
  public Response deleteDataShieldROption(String name, String profile) {
    DataShieldProfile config = getDataShieldProfile(profile);
    config.removeOption(name);
    datashieldProfileService.saveProfile(config);
    return Response.ok().build();
  }

  @PUT
  @Operation(
    summary = "Add or update DataSHIELD R option",
    description = "Adds a new R option to the DataSHIELD profile or updates an existing one with the specified name and value."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully added or updated R option"),
    @ApiResponse(responseCode = "404", description = "DataSHIELD profile not found"),
    @ApiResponse(responseCode = "400", description = "Invalid option data provided"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Override
  public Response addOrUpdateDataShieldROption(String profile, final DataShield.DataShieldROptionDto dto) {
    DataShieldProfile config = getDataShieldProfile(profile);
    config.addOrUpdateOption(dto.getName(), dto.getValue());
    datashieldProfileService.saveProfile(config);
    return Response.ok().build();
  }

  @GET
  @Operation(
    summary = "Get DataSHIELD R option",
    description = "Retrieves a specific R option from the DataSHIELD profile configuration by option name."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R option"),
    @ApiResponse(responseCode = "404", description = "DataSHIELD profile or option not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Override
  public Response getDataShieldROption(String name, String profile) {
    DataShieldProfile config = getDataShieldProfile(profile);

    if (config.hasOption(name)) {
      DataShield.DataShieldROptionDto dto = DataShield.DataShieldROptionDto.newBuilder().setName(name)
          .setValue(config.getOption(name).getValue()).build();

      return Response.ok().entity(dto).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }

  private DataShieldProfile getDataShieldProfile(String profileName) {
    return datashieldProfileService.getProfile(profileName);
  }
}
