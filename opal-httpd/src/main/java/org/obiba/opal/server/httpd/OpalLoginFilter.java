/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.httpd;

import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCException;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.oidc.web.filter.OIDCLoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("opalLoginFilter")
public class OpalLoginFilter extends OIDCLoginFilter {

  @Autowired
  private OIDCConfigurationProvider oidcConfigurationProvider;

  @Autowired
  private OIDCSessionManager oidcSessionManager;

  @Value("${org.obiba.opal.public.url}")
  private String opalPublicUrl;

  @PostConstruct
  public void init() {
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    String callbackUrl = opalPublicUrl + (opalPublicUrl.endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      super.doFilterInternal(request, response, filterChain);
    } catch (OIDCException e) {
      response.sendRedirect(opalPublicUrl);
    }
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("opalLoginFilter");
    }
  }

}
