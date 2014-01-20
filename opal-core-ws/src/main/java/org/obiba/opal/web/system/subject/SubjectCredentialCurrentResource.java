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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.support.InvalidRequestException;
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
  @NoAuthorization
  public Response update(Opal.SubjectCredentialsDto dto) {
    if(!getName().equals(dto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    SubjectCredentials originalSubjectCredentials = getSubjectCredentials();
    if (originalSubjectCredentials == null) return Response.status(Response.Status.NOT_FOUND).build();

    if (!dto.getAuthenticationType().toString().equals(originalSubjectCredentials.getAuthenticationType().name())) {
      throw new InvalidRequestException("Authentication type cannot be changed");
    }

    switch(originalSubjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        if(dto.hasPassword() && !dto.getPassword().isEmpty()) {
          originalSubjectCredentials.setPassword(subjectCredentialsService.hashPassword(dto.getPassword()));
        }
        break;
      case CERTIFICATE:
        if(dto.hasCertificate() && !dto.getCertificate().isEmpty()) {
          originalSubjectCredentials.setCertificate(dto.getCertificate().toByteArray());
        }
        break;
    }
    subjectCredentialsService.save(originalSubjectCredentials);
    return Response.ok().build();
  }

  private SubjectCredentials getSubjectCredentials() {
    return subjectCredentialsService.getSubjectCredentials(getName());
  }

  private String getName() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

}