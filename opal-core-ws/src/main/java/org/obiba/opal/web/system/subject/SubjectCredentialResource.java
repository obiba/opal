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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/system/subject-credential/{name}")
@Tag(name = "Subjects", description = "Operations on subjects")
public class SubjectCredentialResource {

  @PathParam("name")
  private String name;

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @GET
  @Operation(summary = "Get subject credentials", description = "Retrieves the credentials of a specific subject.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject credentials retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Subject credentials not found")
  })
  public Response get() {
    SubjectCredentials subjectCredentials = subjectCredentialsService.getSubjectCredentials(name);
    return (subjectCredentials == null //
        ? Response.status(Response.Status.NOT_FOUND) //
        : Response.ok().entity(Dtos.asDto(subjectCredentials))).build();
  }

  @PUT
  @Operation(summary = "Update subject credentials", description = "Updates the credentials of a specific subject.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject credentials updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid credentials data provided"),
      @ApiResponse(responseCode = "404", description = "Subject credentials not found")
  })
  public Response update(Opal.SubjectCredentialsDto dto) {
    if (!name.equals(dto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    if (getSubjectCredentials() == null) return Response.status(Response.Status.NOT_FOUND).build();

    SubjectCredentials subjectCredentials = Dtos.fromDto(dto);
    switch (subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        if (dto.hasPassword() && !dto.getPassword().isEmpty()) {
          subjectCredentials.setPassword(subjectCredentialsService.hashPassword(dto.getPassword()));
        }
        break;
      case CERTIFICATE:
        if (dto.hasCertificate() && !dto.getCertificate().isEmpty()) {
          subjectCredentials.setCertificate(dto.getCertificate().toByteArray());
        }
        break;
    }
    subjectCredentialsService.save(subjectCredentials);
    return Response.ok().build();
  }

  @DELETE
  @Operation(summary = "Delete subject credentials", description = "Deletes the credentials of a specific subject.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject credentials deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Subject credentials not found")
  })
  public Response delete() {
    SubjectCredentials subjectCredentials = getSubjectCredentials();
    if (subjectCredentials == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    subjectCredentialsService.delete(subjectCredentials);
    return Response.ok().build();
  }

  private SubjectCredentials getSubjectCredentials() {
    return subjectCredentialsService.getSubjectCredentials(name);
  }
}