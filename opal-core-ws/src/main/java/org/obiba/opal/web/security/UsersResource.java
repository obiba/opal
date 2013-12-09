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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.util.SimpleByteSource;
import org.obiba.opal.core.domain.user.SubjectCredentials;
import org.obiba.opal.core.service.DuplicateUserNameException;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/users")
public class UsersResource {

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @GET
  public List<Opal.UserDto> getUsers() {
    return Lists.newArrayList(Iterables
        .transform(subjectCredentialsService.getSubjectCredentials(), new Function<SubjectCredentials, Opal.UserDto>() {
          @Override
          public Opal.UserDto apply(SubjectCredentials subjectCredentials) {
            return Dtos.asDto(subjectCredentials);
          }
        }));
  }

  @POST
  public Response createUser(Opal.UserDto dto) {
    SubjectCredentials subjectCredentials = Dtos.fromDto(dto);
    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    if(found != null) {
      throw new DuplicateUserNameException(found, subjectCredentials);
    }
    subjectCredentials.setPassword(
        SubjectCredentials.digest(dto.getPassword(), new SimpleByteSource(subjectCredentials.getName()).getBytes()));
    subjectCredentialsService.save(subjectCredentials);
    return Response.ok().build();
  }
}