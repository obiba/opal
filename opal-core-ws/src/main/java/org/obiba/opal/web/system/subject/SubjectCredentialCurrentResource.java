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

import javax.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/system/subject-credential/_current")
public class SubjectCredentialCurrentResource {

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @GET
  @NoAuthorization
  public Response get() {
    SubjectCredentials subjectCredentials = getSubjectCredentials();
    return (subjectCredentials == null //
        ? Response.status(Response.Status.NOT_FOUND) //
        : Response.ok().entity(Dtos.asDto(subjectCredentials))).build();
  }

  @PUT
  @Path("/password")
  @NoAuthorization
  public Response updatePassword(@NotNull Opal.PasswordDto passwordDto) {

    if(!getName().equals(passwordDto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    subjectCredentialsService
        .changePassword(passwordDto.getName(), passwordDto.getOldPassword(), passwordDto.getNewPassword());

    return Response.ok().build();
  }

  private SubjectCredentials getSubjectCredentials() {
    return subjectCredentialsService.getSubjectCredentials(getName());
  }

  private String getName() {
    return (String)SecurityUtils.getSubject().getPrincipal();
  }

}