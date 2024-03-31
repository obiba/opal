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

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.opal.core.service.security.realm.OpalTokenRealm;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/system/subject-token/_current/token/{name}")
public class SubjectTokenCurrentResource {

  @PathParam("name")
  private String name;

  @Autowired
  private SubjectTokenService subjectTokenService;

  @DELETE
  @NoAuthorization
  public Response delete() {
    checkSubjectNotToken();
    subjectTokenService.deleteToken(getPrincipal(), name);
    return Response.ok().build();
  }

  @PUT
  @Path("/_renew")
  @NoAuthorization
  public Response renew() {
    checkSubjectNotToken();
    subjectTokenService.renewToken(getPrincipal(), name);
    return Response.ok().build();
  }

  private String getPrincipal() {
    return (String) SecurityUtils.getSubject().getPrincipal();
  }

  /**
   * Verifies that the requesting subject is not authenticated by a token.
   */
  private void checkSubjectNotToken() {
    if (!SecurityUtils.getSubject().getPrincipals().fromRealm(OpalTokenRealm.TOKEN_REALM).isEmpty())
      throw new ForbiddenException();
  }
}