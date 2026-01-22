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
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional
@Path("/r/profiles")
@Tag(name = "R", description = "Operations on R")
public class RProfilesResource {

  @Autowired
  private RServerManagerService rServerManagerService;

  @GET
  @Operation(
    summary = "Get R profiles",
    description = "Retrieves a list of R profiles, optionally filtered by enabled status. Profiles represent R server configurations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R profiles"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RProfileDto> getProfiles(@QueryParam("enabled") Boolean enabled) {
    return rServerManagerService.getRServerClusters().stream()
        .map(Dtos::asProfileDto)
        .filter(p -> enabled == null || (enabled && p.getEnabled()) || (!enabled && !p.getEnabled()))
        .collect(Collectors.toList());
  }

}
