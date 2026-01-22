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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.support.ConflictingRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/system/subject-credentials")
@Tag(name = "Subjects", description = "Operations on subjects")
public class SubjectCredentialsResource implements BaseResource {

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @GET
  @Operation(summary = "Get all subject credentials", description = "Retrieves all subject credentials.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject credentials retrieved successfully")
  })
  public List<Opal.SubjectCredentialsDto> getAll() {
    return Lists.newArrayList(Iterables.transform(subjectCredentialsService.getSubjectCredentials(),
        new Function<SubjectCredentials, Opal.SubjectCredentialsDto>() {
          @Override
          public Opal.SubjectCredentialsDto apply(SubjectCredentials subjectCredentials) {
            return Dtos.asDto(subjectCredentials);
          }
        }));
  }

  @POST
  @Operation(summary = "Create subject credentials", description = "Creates new subject credentials.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject credentials created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid credentials data provided"),
      @ApiResponse(responseCode = "409", description = "Subject already exists")
  })
  public Response create(Opal.SubjectCredentialsDto dto) {
    SubjectCredentials subjectCredentials = Dtos.fromDto(dto);
    if (subjectCredentials.getName().trim().isEmpty()) {
      throw new BadRequestException("Subject name cannot be empty");
    }

    if (subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName()) != null) {
      throw new ConflictingRequestException("Subject name must be unique");
    }

    switch (subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        if (dto.hasPassword()) {
          subjectCredentials.setPassword(subjectCredentialsService.hashPassword(dto.getPassword()));
        }
        break;
      case CERTIFICATE:
        if (dto.hasCertificate()) {
          subjectCredentials.setCertificate(dto.getCertificate().toByteArray());
        } else {
          throw new BadRequestException("Subject certificate must be unique");
        }
        break;
    }
    subjectCredentialsService.save(subjectCredentials);
    return Response.ok().build();
  }
}