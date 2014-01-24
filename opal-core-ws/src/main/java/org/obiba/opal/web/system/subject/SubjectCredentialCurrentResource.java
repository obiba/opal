/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.system.subject;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@Scope("request")
@Path("/system/subject-credential/_current")
public class SubjectCredentialCurrentResource {

  private static final int MINIMUM_LEMGTH = 6;


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

    SubjectCredentials originalSubjectCredentials = getSubjectCredentials();
    if(originalSubjectCredentials == null) return Response.status(Response.Status.NOT_FOUND).build();

    changePassword(originalSubjectCredentials, passwordDto);

    return Response.ok().build();
  }

  private SubjectCredentials getSubjectCredentials() {
    return subjectCredentialsService.getSubjectCredentials(getName());
  }

  private String getName() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  private void changePassword(SubjectCredentials subjectCredentials, Opal.PasswordDto passwordDto) {
    String currentPassword = subjectCredentials.getPassword();
    String oldPassword = Strings.nullToEmpty(passwordDto.getOldPassword());
    String newPassword = Strings.nullToEmpty(passwordDto.getNewPassword());

    if (!currentPassword.equals(subjectCredentialsService.hashPassword(oldPassword))) {
      throw new OldPasswordMismatchException();
    }

    if (newPassword.length() < MINIMUM_LEMGTH) {
      throw new PasswordTooShortException(MINIMUM_LEMGTH);
    }

    if (oldPassword.equals(newPassword)) {
      throw new PasswordNotChangedException();
    }

    subjectCredentials.setPassword(subjectCredentialsService.hashPassword(newPassword));
    subjectCredentialsService.save(subjectCredentials);
  }
}