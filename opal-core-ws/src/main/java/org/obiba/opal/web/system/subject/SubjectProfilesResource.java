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

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Path("/system/subject-profiles")
public class SubjectProfilesResource {

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private SubjectAclService subjectAclService;

  @GET
  public List<Opal.SubjectProfileDto> getAll() {
    return StreamSupport.stream(subjectProfileService.getProfiles().spliterator(), false)
        .map(Dtos::asDto)
        .collect(Collectors.toList());
  }

  @GET
  @Path("/_search")
  public Opal.SuggestionsDto suggestNames(@QueryParam("type") @DefaultValue("USER") SubjectAcl.SubjectType type,
                                          @QueryParam("query") String query) {
    List<String> suggestions = subjectAclService.suggestSubjects(SubjectAcl.SubjectType.GROUP.equals(type) ? SubjectAcl.SubjectType.GROUP : SubjectAcl.SubjectType.USER, query);
    Opal.SuggestionsDto.Builder builder = Opal.SuggestionsDto.newBuilder()
        .setQuery(query == null ? "" : query)
        .addAllSuggestions(suggestions);
    return builder.build();
  }
}