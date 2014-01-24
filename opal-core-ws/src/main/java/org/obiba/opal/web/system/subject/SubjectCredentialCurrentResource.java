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
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import static org.obiba.opal.web.model.Ws.ClientErrorDto;

@Component
@Scope("request")
@Path("/system/subject-credential/_current")
public class SubjectCredentialCurrentResource {

  private static final int MINIMUM_LEMGTH = 6;

  private enum ValidationStatus {
    PASSWORD_VALID,
    OLD_PASSWORD_MISMATCH,
    PASSWORD_NOT_CHANGED,
    PASSWORD_TOO_SHORT
  }

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

    String newPassword = Strings.nullToEmpty(passwordDto.getNewPassword());

    ValidationStatus status = validatePassword(originalSubjectCredentials.getPassword(),
        Strings.nullToEmpty(passwordDto.getOldPassword()), newPassword);

    if (ValidationStatus.PASSWORD_VALID == status) {
      originalSubjectCredentials.setPassword(subjectCredentialsService.hashPassword(newPassword));
      subjectCredentialsService.save(originalSubjectCredentials);
      return Response.ok().build();
    }

    return Response.status(Response.Status.BAD_REQUEST).entity(createClientErrorDto(status)).build();
  }

  private ClientErrorDto createClientErrorDto(ValidationStatus status) {
    ClientErrorDto dto = ClientErrorDto.getDefaultInstance();

    switch (status) {
      case OLD_PASSWORD_MISMATCH:
        dto = ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "OldPasswordMismatch").build();
        break;
      case PASSWORD_TOO_SHORT:
        dto = ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "PasswordLengthMin")
            .addArguments(String.valueOf(MINIMUM_LEMGTH)).build();
        break;
      case PASSWORD_NOT_CHANGED:
        dto = ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "PasswordNotChanged").build();
        break;
    }

    return dto;
  }

  private SubjectCredentials getSubjectCredentials() {
    return subjectCredentialsService.getSubjectCredentials(getName());
  }

  private String getName() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  private ValidationStatus validatePassword(String password, String oldPassword, String newPassword) {
    if (!password.equals(subjectCredentialsService.hashPassword(oldPassword))) {
      return ValidationStatus.OLD_PASSWORD_MISMATCH;
    }

    if (newPassword.length() < MINIMUM_LEMGTH) {
      return ValidationStatus.PASSWORD_TOO_SHORT;
    }

    if (oldPassword.equals(newPassword)) {
      return ValidationStatus.PASSWORD_NOT_CHANGED;
    }

    return ValidationStatus.PASSWORD_VALID;
  }
}