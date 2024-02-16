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
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCException;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.oidc.web.filter.OIDCLoginFilter;
import org.obiba.opal.core.event.OpalGeneralConfigUpdatedEvent;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("opalLoginFilter")
public class OpalLoginFilter extends OIDCLoginFilter {

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  private final OpalGeneralConfigService opalGeneralConfigService;

  @Value("${org.obiba.opal.public.url}")
  private String defaultOpalPublicUrl;

  private String opalPublicUrl;

  @Autowired
  public OpalLoginFilter(OIDCConfigurationProvider oidcConfigurationProvider,
                         OIDCSessionManager oidcSessionManager,
                         OpalGeneralConfigService opalGeneralConfigService) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
    this.opalGeneralConfigService = opalGeneralConfigService;
  }

  @Override
  public void afterPropertiesSet() throws ServletException {
    super.afterPropertiesSet();
  }

  @Override
  protected void initFilterBean() {
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
