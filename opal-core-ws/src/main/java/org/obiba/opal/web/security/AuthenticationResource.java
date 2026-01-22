/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.security;

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONObject;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.obiba.shiro.web.filter.UserBannedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response.Status;
import java.util.stream.StreamSupport;

@Component
@Path("/auth")
@Tag(name = "Authentication", description = "Operations related to user authentication")
public class AuthenticationResource extends AbstractSecurityComponent {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationResource.class);

  private static final String OBIBA_ID_COOKIE_NAME = "obibaid";

  @Autowired
  private AuthenticationExecutor authenticationExecutor;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @Autowired
  private SubjectProfileService subjectProfileService;

@POST
@Path("/sessions")
@NotAuthenticated
@Operation(summary = "Create user session", description = "Authenticate user and create a new session. Supports username/password authentication and optional one-time password (OTP) for two-factor authentication.")
@ApiResponses({
  @ApiResponse(responseCode = "201", description = "Session successfully created"),
  @ApiResponse(responseCode = "401", description = "Authentication failed or OTP required"),
  @ApiResponse(responseCode = "403", description = "User banned or invalid credentials"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response createSession(@SuppressWarnings("TypeMayBeWeakened") @Context HttpServletRequest servletRequest,
                              @FormParam("username") String username, @FormParam("password") String password) {
    if (isUserAuthenticated()) {
      invalidateSession();
    }
    try {
      authenticationExecutor.login(servletRequest, makeUsernamePasswordToken(username, password, servletRequest));
      String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
      log.info("Successful session creation for user '{}' at ip: '{}': session ID is '{}'.", username,
          servletRequest.getRemoteAddr(), sessionId);
      return Response.created(
          UriBuilder.fromPath("/").path(AuthenticationResource.class).path(AuthenticationResource.class, "checkSession")
              .build(sessionId)).build();
    } catch (UserBannedException e) {
      log.info("Authentication failure: {}", e.getMessage());
      throw e;
    } catch (NoSuchOtpException e) {
      Response.ResponseBuilder builder =  Response.status(Status.UNAUTHORIZED)
          .header("WWW-Authenticate", e.getOtpHeader());
      if (e.hasQrImage()) {
        JSONObject respObject = new JSONObject();
        respObject.put("image", e.getQrImage());
        builder.header("Content-type", "application/json")
            .entity(respObject.toString());
      }
      return builder.build();
    } catch (AuthenticationException e) {
      if (e.getCause() instanceof NoSuchOtpException) {
        NoSuchOtpException otpException = (NoSuchOtpException) e.getCause();
        return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", otpException.getOtpHeader()).build();
      }
      log.info("Authentication failure of user '{}' at ip: '{}': {}", username, servletRequest.getRemoteAddr(),
          e.getMessage());
      // When a request contains credentials and they are invalid, the a 403 (Forbidden) should be returned.
      return Response.status(Status.FORBIDDEN).build();
    }
  }

@HEAD
@Path("/session/{id}")
@Operation(summary = "Check session validity", description = "Check if a session with the given ID is still valid")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Session is valid"),
  @ApiResponse(responseCode = "404", description = "Session not found or expired"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response checkSession(@PathParam("id") String sessionId) {
    // Find the Shiro Session
    return isValidSessionId(sessionId) ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
  }

@DELETE
@Path("/session/{id}")
@NoAuthorization
@Operation(summary = "Delete session by ID", description = "Delete a specific user session by its ID (legacy endpoint)")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Session successfully deleted"),
  @ApiResponse(responseCode = "404", description = "Session not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response deleteSession(@PathParam("id") String sessionId) {
    // legacy
    return deleteCurrentSession();
  }

@DELETE
@Path("/session/_current")
@NoAuthorization
@Operation(summary = "Delete current session", description = "Delete the current user session and invalidate authentication")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Current session successfully deleted"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response deleteCurrentSession() {
    // Delete the Shiro session
    try {
      Session session = SecurityUtils.getSubject().getSession();
      Object cookieValue = session.getAttribute(HttpHeaders.SET_COOKIE);
      SecurityUtils.getSubject().logout();

      if (cookieValue != null) {
        NewCookie cookie = RuntimeDelegate.getInstance().createHeaderDelegate(NewCookie.class).fromString(cookieValue.toString());
        if (OBIBA_ID_COOKIE_NAME.equals(cookie.getName())) {
          return Response.ok().header(HttpHeaders.SET_COOKIE,
              new NewCookie.Builder(OBIBA_ID_COOKIE_NAME)
                  .value(null)
                  .path("/")
                  .domain(cookie.getDomain())
                  .comment("Obiba session deleted")
                  .maxAge(0)
                  .httpOnly(true)
                  .secure(true)
                  .sameSite(NewCookie.SameSite.LAX)
                  .build()).build();
        }
      }
    } catch (InvalidSessionException e) {
      // Ignore
    }
    return Response.ok().build();
  }

@GET
@Path("/session/_current/username")
@NoAuthorization
@Operation(summary = "Get current user", description = "Retrieve information about the currently authenticated user")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Current user information successfully retrieved"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Opal.Subject getCurrentSubject() {
    // Find the Shiro username
    Subject subject = SecurityUtils.getSubject();
    String principal = subject.getPrincipal() == null ? "" : subject.getPrincipal().toString();
    return Opal.Subject.newBuilder()
        .setPrincipal(principal)
        .setType(Opal.Subject.SubjectType.USER)
        .setOtpRequired(isSubjectProfileSecretRequired(principal))
        .build();
  }

@GET
@Path("/session/{id}/username")
@Operation(summary = "Get user by session ID", description = "Retrieve user information for a specific session ID")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "User information successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "Session not found or expired"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Opal.Subject getSubject(@PathParam("id") String sessionId) {
    // Find the Shiro username
    String principal = isValidSessionId(sessionId) ? SecurityUtils.getSubject().getPrincipal().toString() : null;
    return Opal.Subject.newBuilder()
        .setPrincipal(principal)
        .setType(Opal.Subject.SubjectType.USER)
        .setOtpRequired(isSubjectProfileSecretRequired(principal))
        .build();
  }

  private boolean isSubjectProfileSecretRequired(String principal) {
    boolean secretRequired = false;
    if (opalGeneralConfigService.getConfig().isEnforced2FA()) {
      SubjectProfile profile = subjectProfileService.getProfile(principal);
      boolean otpRealm = StreamSupport.stream(profile.getRealms().spliterator(), false)
          .anyMatch(realm -> realm.equals("opal-user-realm") || realm.equals("opal-ini-realm"));
      secretRequired = otpRealm && !profile.hasSecret();
    }
    return secretRequired;
  }

  private UsernamePasswordToken makeUsernamePasswordToken(String username, String password, HttpServletRequest request) {
    String otp = request.getHeader("X-Opal-TOTP");
    if (!Strings.isNullOrEmpty(otp))
      return new UsernamePasswordOtpToken(username, password, otp);
    otp = request.getHeader("X-Obiba-TOTP");
    if (!Strings.isNullOrEmpty(otp))
      return new UsernamePasswordOtpToken(username, password, otp);
    return new UsernamePasswordToken(username, password);
  }

}
