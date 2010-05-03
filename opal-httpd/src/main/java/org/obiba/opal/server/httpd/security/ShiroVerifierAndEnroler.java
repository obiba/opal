/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.security.Enroler;
import org.restlet.security.Role;
import org.restlet.security.SecretVerifier;

/**
 *
 */
public class ShiroVerifierAndEnroler extends SecretVerifier implements Enroler {

  String pwel = "AUTH";

  @Override
  public boolean verify(String identifier, char[] secret) {
    Request request = Request.getCurrent();

    Cookie authCookie = request.getCookies().getFirst(pwel);
    if(authCookie != null) {
      try {
        String sessionId = authCookie.getValue();
        SecurityUtils.getSecurityManager().checkValid(sessionId);
      } catch(InvalidSessionException e) {
        System.out.println("invalid session id=" + authCookie.getValue());
        authCookie = null;
      }
    }

    Subject s;

    if(authCookie == null) {
      s = SecurityUtils.getSubject();
    } else {
      String sessionId = authCookie.getValue();
      s = new Subject.Builder().sessionId(sessionId).buildSubject();
    }

    try {
      s.login(new UsernamePasswordToken(identifier, secret, request.getClientInfo().getAddress()));
      String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
      System.out.println("session id=" + sessionId);
      CookieSetting authSetting = new CookieSetting(pwel, sessionId);
      Response.getCurrent().getCookieSettings().add(authSetting);
      return true;
    } catch(AuthenticationException e) {
      return false;
    }
  }

  public void enrole(ClientInfo clientInfo) {
    for(Object principal : SecurityUtils.getSubject().getPrincipals().asList()) {
      clientInfo.getRoles().add(new Role(principal.toString(), null));
    }
  }
}
