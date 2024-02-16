/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.httpd;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.oidc.*;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.obiba.oidc.utils.OIDCHelper;
import org.obiba.oidc.web.J2EContext;
import org.obiba.oidc.web.filter.OIDCCallbackFilter;
import org.obiba.opal.core.event.OpalGeneralConfigUpdatedEvent;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("opalCallbackFilter")
public class OpalCallbackFilter extends OIDCCallbackFilter {

  private static final Logger log = LoggerFactory.getLogger(OpalCallbackFilter.class);

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  private final AuthenticationExecutor authenticationExecutor;

  private final OpalGeneralConfigService opalGeneralConfigService;

  @Value("${org.obiba.opal.public.url}")
  private String defaultOpalPublicUrl;

  private String opalPublicUrl;

  @Autowired
  public OpalCallbackFilter(OIDCConfigurationProvider oidcConfigurationProvider,
                            OIDCSessionManager oidcSessionManager,
                            AuthenticationExecutor authenticationExecutor,
                            OpalGeneralConfigService opalGeneralConfigService) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
    this.authenticationExecutor = authenticationExecutor;
    this.opalGeneralConfigService = opalGeneralConfigService;
  }

  @Override
  protected void initFilterBean() throws ServletException {
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    initFilterUrls();
  }

  @Subscribe
  public void onOpalGeneralConfigUpdated(OpalGeneralConfigUpdatedEvent event) {
    initFilterUrls();
  }

  private void initFilterUrls() {
    String publicUrl = opalGeneralConfigService.getConfig().getPublicUrl();
    opalPublicUrl = Strings.isNullOrEmpty(publicUrl) ? defaultOpalPublicUrl : publicUrl;
    setDefaultRedirectURL(opalPublicUrl);
    String callbackUrl = opalPublicUrl + (opalPublicUrl.endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      super.doFilterInternal(request, response, filterChain);
    } catch (OIDCException e) {
      J2EContext context = new J2EContext(request, response);
      String provider = OIDCHelper.extractProviderName(context, getProviderParameter());
      OIDCConfiguration config = oidcConfigurationProvider.getConfiguration(provider);
      if (config.hasCallbackURL()) {
         String[] tokens = config.getCallbackURL().split("/auth/callback");
         response.sendRedirect(tokens.length>0 ? tokens[0] : opalPublicUrl);
      } else
        response.sendRedirect(opalPublicUrl);
    }
  }

  @Override
  protected void onRedirect(OIDCSession session, J2EContext context, String provider) throws IOException {
    if (!Strings.isNullOrEmpty(provider)) {
      OIDCConfiguration config = oidcConfigurationProvider.getConfiguration(provider);
      if (config != null && config.hasCallbackURL()) {
        String[] tokens = config.getCallbackURL().split("/auth/callback");
        context.getResponse().sendRedirect(tokens.length>0 ? tokens[0] : opalPublicUrl);
        return;
      }
    }
    super.onRedirect(session, context, provider);
  }

  @Override
  protected void onAuthenticationSuccess(OIDCSession session, OIDCCredentials credentials, HttpServletResponse response) {
    Subject subject = authenticationExecutor.login(new OIDCAuthenticationToken(credentials));
    if (subject != null) {
      Session subjectSession = subject.getSession();
      log.trace("Binding subject {} session {} to executing thread {}", subject.getPrincipal(), subjectSession.getId(), Thread.currentThread().getId());
      ThreadContext.bind(subject);
      subjectSession.touch();
      int timeout = (int) (subjectSession.getTimeout() / 1000);
      Cookie cookie = new Cookie("opalsid", subjectSession.getId().toString());
      cookie.setMaxAge(timeout);
      cookie.setPath("/");
      cookie.setSecure(true);
      cookie.setHttpOnly(true);
      response.addCookie(cookie);
      log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
    }
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("opalCallbackFilter");
    }
  }

}
