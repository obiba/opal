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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/system/groups")
public class GroupsResource {

  private final SubjectCredentialsService subjectCredentialsService;

  @Autowired
  public GroupsResource(SubjectCredentialsService subjectCredentialsService) {
    this.subjectCredentialsService = subjectCredentialsService;
  }

  @GET
  public List<Opal.GroupDto> getGroups() {
    return Lists
        .newArrayList(Iterables.transform(subjectCredentialsService.getGroups(), new Function<Group, Opal.GroupDto>() {
          @Override
          public Opal.GroupDto apply(Group group) {
            return Dtos.asDto(group);
          }
        }));
  }

  @POST
  public Response createGroup(Opal.GroupDto dto) {
    Group group = new Group(dto.getName());
    if(subjectCredentialsService.getGroup(dto.getName()) != null) {
      throw new BadRequestException("Group name must be unique");
    }
    subjectCredentialsService.createGroup(dto.getName());
    return Response.ok().build();
  }

}