/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.InvalidSessionException;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/auth")
public class SecurityResource extends AbstractSecurityComponent {

  private static final Logger log = LoggerFactory.getLogger(SecurityResource.class);

  @Autowired
  public SecurityResource(SessionsSecurityManager securityManager) {
    super(securityManager);
  }

  @POST
  @Path("/sessions")
  @NotAuthenticated
  public Response createSession(@FormParam("username") String username, @FormParam("password") String password) {
    try {
      SecurityUtils.getSubject().login(new UsernamePasswordToken(username, password));
    } catch(AuthenticationException e) {
      // When a request contains credentials and they are invalid, the a 401 (Unauthorized) should be returned.
      return Response.status(Status.UNAUTHORIZED).build();
    }
    String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
    log.info("Successfull session creation for user '{}' session ID is '{}'.", username, sessionId);
    return Response.created(UriBuilder.fromPath("/").path(SecurityResource.class).path(SecurityResource.class, "checkSession").build(sessionId)).build();
  }

  @HEAD
  @Path("/session/{id}")
  public Response checkSession(@PathParam("id") String sessionId) {
    // Find the Shiro Session
    if(isValidSessionId(sessionId) == false) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  @DELETE
  @Path("/session/{id}")
  public Response deleteSession(@PathParam("id") String sessionId) {
    // Delete the Shiro session
    try {
      SecurityUtils.getSubject().logout();
    } catch(InvalidSessionException e) {
      // Ignore
    }
    return Response.ok().build();
  }
}
