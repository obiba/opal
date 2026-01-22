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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/system/group/{name}")
@Tag(name = "Subjects", description = "Operations on subjects")
public class GroupResource {

  @PathParam("name")
  private String name;

  private final SubjectCredentialsService subjectCredentialsService;

  @Autowired
  public GroupResource(SubjectCredentialsService subjectCredentialsService) {
    this.subjectCredentialsService = subjectCredentialsService;
  }

  @GET
  @Operation(summary = "Get group", description = "Retrieves a specific group.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Group retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Group not found")
  })
  public Response getGroup() {
    Group group = subjectCredentialsService.getGroup(name);
    return group == null
        ? Response.status(Response.Status.NOT_FOUND).build()
        : Response.ok().entity(Dtos.asDto(group)).build();
  }

  @DELETE
  @Operation(summary = "Delete group", description = "Deletes a specific group.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Group deleted successfully")
  })
  public Response deleteGroup() {
    Group group = subjectCredentialsService.getGroup(name);
    subjectCredentialsService.delete(group);
    return Response.ok().build();
  }
}