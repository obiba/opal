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
import org.obiba.opal.web.support.ConflictingRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
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
    if (group.getName().trim().isEmpty()) {
      throw new BadRequestException("Group name cannot be empty");
    }

    if(subjectCredentialsService.getGroup(dto.getName()) != null) {
      throw new ConflictingRequestException("Group name must be unique");
    }
    subjectCredentialsService.createGroup(dto.getName());
    return Response.ok().build();
  }

}