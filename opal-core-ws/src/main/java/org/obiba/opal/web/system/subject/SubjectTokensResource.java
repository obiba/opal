/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.subject;

import com.google.common.base.Strings;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/system/subject-token/{principal}/tokens")
public class SubjectTokensResource {

  @PathParam("principal")
  private String principal;

  @Autowired
  private SubjectTokenService subjectTokenService;

  @GET
  public List<Opal.SubjectTokenDto> getAll() {
    return subjectTokenService.getTokens(principal).stream()
        .map(Dtos::asDto)
        .collect(Collectors.toList());
  }

  @POST
  public Response create(Opal.SubjectTokenDto token) {
    if (token.hasToken() || Strings.isNullOrEmpty(token.getName()))
      return Response.status(Response.Status.BAD_REQUEST).build();
    SubjectToken tokenObj = Dtos.fromDto(token);
    tokenObj.setPrincipal(principal);
    tokenObj = subjectTokenService.saveToken(tokenObj);
    URI tokenUri = UriBuilder.fromPath("/").path(SubjectTokenResource.class).build(principal, tokenObj.getToken());
    return Response.created(tokenUri).build();
  }

  @DELETE
  public Response deleteAll() {
    subjectTokenService.deleteTokens(principal);
    return Response.noContent().build();
  }

}