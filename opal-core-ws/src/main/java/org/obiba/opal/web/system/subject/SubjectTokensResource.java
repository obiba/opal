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
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/system/subject-token/{principal}/tokens")
@Tag(name = "Subjects", description = "Operations on subjects")
public class SubjectTokensResource {

  @PathParam("principal")
  private String principal;

  @Autowired
  private SubjectTokenService subjectTokenService;

  @GET
  @Operation(summary = "Get subject tokens", description = "Retrieves all tokens belonging to a specific subject.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject tokens retrieved successfully")
  })
  public List<Opal.SubjectTokenDto> getAll() {
    return subjectTokenService.getTokens(principal).stream()
        .map(token -> Dtos.asDto(token, subjectTokenService.getTokenTimestamps(token)))
        .collect(Collectors.toList());
  }

  @DELETE
  @Operation(summary = "Delete all subject tokens", description = "Deletes all tokens belonging to a specific subject.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "All tokens deleted successfully")
  })
  public Response deleteAll() {
    subjectTokenService.deleteTokens(principal);
    return Response.noContent().build();
  }

}