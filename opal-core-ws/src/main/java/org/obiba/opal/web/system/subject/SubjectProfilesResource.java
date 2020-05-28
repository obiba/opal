/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.subject;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Path("/system/subject-profiles")
public class SubjectProfilesResource {

  @Autowired
  private SubjectProfileService subjectProfileService;

  @GET
  public List<Opal.SubjectProfileDto> getAll() {
    return Lists.newArrayList(Iterables.transform(subjectProfileService.getProfiles(),
        new Function<SubjectProfile, Opal.SubjectProfileDto>() {
          @Override
          public Opal.SubjectProfileDto apply(SubjectProfile profile) {
            return Dtos.asDto(profile);
          }
        }));
  }
}