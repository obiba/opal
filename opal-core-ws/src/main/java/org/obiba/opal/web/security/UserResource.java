/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.util.SimpleByteSource;
import org.obiba.opal.core.domain.user.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/user/{name}")
public class UserResource {

  @PathParam("name")
  private String name;

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @GET
  public Response getUser() {
    //TODO: Use TimestampedResponses.evaluate(request, ...); ?
    SubjectCredentials subjectCredentials = subjectCredentialsService.getSubjectCredentials(name);
    if(subjectCredentials == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok().entity(Dtos.asDto(subjectCredentials)).build();
  }

  @PUT
  public Response updateUser(Opal.UserDto dto) {
    if(!name.equals(dto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    SubjectCredentials subjectCredentials = Dtos.fromDto(dto);
    if(dto.hasPassword() && !dto.getPassword().isEmpty()) {
      subjectCredentials.setPassword(
          SubjectCredentials.digest(dto.getPassword(), new SimpleByteSource(subjectCredentials.getName()).getBytes()));
    }
    subjectCredentialsService.save(subjectCredentials);
    return Response.ok().build();
  }

  @DELETE
  public Response deleteUser() {
    SubjectCredentials subjectCredentials = subjectCredentialsService.getSubjectCredentials(name);
    if(subjectCredentials == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    subjectCredentialsService.delete(subjectCredentials);
    return Response.ok().build();
  }
}