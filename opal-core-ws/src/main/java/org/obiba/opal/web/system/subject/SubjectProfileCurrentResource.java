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
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/system/subject-profile/_current")
public class SubjectProfileCurrentResource {

  @Autowired
  private SubjectProfileService subjectProfileService;

  @GET
  @NoAuthorization
  public Response get() {
    SubjectProfile profile = subjectProfileService.getProfile(getPrincipal());
    return (profile == null //
        ? Response.status(Response.Status.NOT_FOUND) //
        : Response.ok().entity(Dtos.asDto(profile))).build();
  }

  private String getPrincipal() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }
}