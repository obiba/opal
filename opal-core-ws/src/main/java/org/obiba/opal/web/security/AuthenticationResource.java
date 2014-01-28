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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/auth")
public class AuthenticationResource extends AbstractSecurityComponent {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationResource.class);

  private static final String HOME_PERM = "FILES_SHARE";

  private static final String ENSURED_PROFILE = "ensuredProfile";

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private OpalRuntime opalRuntime;

  @POST
  @Path("/sessions")
  @NotAuthenticated
  public Response createSession(@SuppressWarnings("TypeMayBeWeakened") @Context HttpServletRequest servletRequest,
      @FormParam("username") String username, @FormParam("password") String password) {
    try {
      Subject subject = SecurityUtils.getSubject();
      subject.login(new UsernamePasswordToken(username, password));
      ThreadContext.bind(subject);
      ensureProfile(subject);
      String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
      log.info("Successful session creation for user '{}' session ID is '{}'.", username, sessionId);
      return Response.created(
          UriBuilder.fromPath("/").path(AuthenticationResource.class).path(AuthenticationResource.class, "checkSession")
              .build(sessionId)).build();

    } catch(AuthenticationException e) {
      log.info("Authentication failure of user '{}' at ip: '{}': {}", username, servletRequest.getRemoteAddr(),
          e.getMessage());
      // When a request contains credentials and they are invalid, the a 403 (Forbidden) should be returned.
      return Response.status(Status.FORBIDDEN).build();
    }
  }

  @HEAD
  @Path("/session/{id}")
  public Response checkSession(@PathParam("id") String sessionId) {
    // Find the Shiro Session
    return isValidSessionId(sessionId) ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
  }

  @DELETE
  @Path("/session/{id}")
  public Response deleteSession() {
    // Delete the Shiro session
    try {
      SecurityUtils.getSubject().logout();
    } catch(InvalidSessionException e) {
      // Ignore
    }
    return Response.ok().build();
  }

  @GET
  @Path("/session/{id}/username")
  public Opal.Subject getSubject(@PathParam("id") String sessionId) {
    // Find the Shiro username
    String principal = isValidSessionId(sessionId) ? SecurityUtils.getSubject().getPrincipal().toString() : null;
    return Opal.Subject.newBuilder() //
        .setPrincipal(principal) //
        .setType(Opal.Subject.SubjectType.USER) //
        .build();
  }

  private void ensureProfile(Subject subject) {
    Object principal = subject.getPrincipal();

    if(!subjectProfileService.supportProfile(principal)) {
      return;
    }

    Session subjectSession = subject.getSession(false);
    boolean ensuredProfile = subjectSession != null && subjectSession.getAttribute(ENSURED_PROFILE) != null;
    if(!ensuredProfile) {
      String username = principal.toString();
      log.info("Ensure HOME folder for {}", username);
      subjectProfileService.ensureProfile(subject.getPrincipals());
      ensureUserHomeExists(username);
      ensureFolderPermissions(username, "/home/" + username);
      ensureFolderPermissions(username, "/tmp");

      if(subjectSession != null) {
        subjectSession.setAttribute(ENSURED_PROFILE, true);
      }
    }
  }

  private void ensureUserHomeExists(String username) {
    try {
      FileObject home = opalRuntime.getFileSystem().getRoot().resolveFile("/home/" + username);
      if(!home.exists()) {
        log.info("Creating user home: /home/{}", username);
        home.createFolder();
      }
    } catch(FileSystemException e) {
      log.error("Failed creating user home.", e);
    }
  }

  private void ensureFolderPermissions(String username, String path) {
    String folderNode = "/files" + path;
    boolean found = false;
    for(SubjectAclService.Permissions acl : subjectAclService
        .getNodePermissions("opal", folderNode, SubjectAcl.SubjectType.USER)) {
      found = findPermission(acl, HOME_PERM);
      if(found) break;
    }
    if(!found) {
      subjectAclService
          .addSubjectPermission("opal", folderNode, SubjectAcl.SubjectType.USER.subjectFor(username), HOME_PERM);
    }
  }

  private boolean findPermission(SubjectAclService.Permissions acl, String permission) {
    boolean found = false;
    for(String perm : acl.getPermissions()) {
      if(perm.equals(permission)) {
        found = true;
        break;
      }
    }
    return found;
  }
}
