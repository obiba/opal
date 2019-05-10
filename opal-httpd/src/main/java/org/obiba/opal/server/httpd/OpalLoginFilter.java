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
import org.obiba.oidc.OIDCStateManager;
import org.obiba.oidc.web.filter.OIDCLoginFilter;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;

@Component("opalLoginFilter")
public class OpalLoginFilter extends OIDCLoginFilter {

  @Autowired
  private OIDCConfigurationProvider oidcConfigurationProvider;

  @Autowired
  private OIDCStateManager oidcStateManager;

  @Value("${org.obiba.opal.public.url}")
  private String opalPublicUrl;

  @PostConstruct
  public void init() {
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCStateManager(oidcStateManager);
    String callbackUrl = opalPublicUrl + (opalPublicUrl.endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("opalLoginFilter");
    }
  }

}
