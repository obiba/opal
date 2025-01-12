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

import com.google.common.base.Strings;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.opal.core.service.security.realm.OpalTokenRealm;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/system/subject-token/_current/tokens")
public class SubjectTokensCurrentResource {

  @Autowired
  private SubjectTokenService subjectTokenService;

  @GET
  @NoAuthorization
  public List<Opal.SubjectTokenDto> getAll() {
    checkSubjectNotToken();
    return subjectTokenService.getTokens(getPrincipal()).stream()
        .map(token -> Dtos.asDto(token, subjectTokenService.getTokenTimestamps(token)))
        .collect(Collectors.toList());
  }

  @POST
  @NoAuthorization
  public Response create(Opal.SubjectTokenDto token) {
    checkSubjectNotToken();
    if (Strings.isNullOrEmpty(token.getName()))
      return Response.status(Response.Status.BAD_REQUEST).build();
    SubjectToken tokenObj = Dtos.fromDto(token);
    tokenObj.setPrincipal(getPrincipal());
    if (!tokenObj.hasToken()) {
      tokenObj.setToken(subjectTokenService.generateToken());
    }
    String secret = tokenObj.getToken();
    subjectTokenService.saveToken(tokenObj);
    URI tokenUri = UriBuilder.fromPath("/").path(SubjectTokenResource.class).build(getPrincipal(), token.getName());
    return Response.created(tokenUri).entity(token.toBuilder().setToken(secret).build()).build();
  }

  @DELETE
  @NoAuthorization
  public Response deleteAll() {
    checkSubjectNotToken();
    subjectTokenService.deleteTokens(getPrincipal());
    return Response.noContent().build();
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