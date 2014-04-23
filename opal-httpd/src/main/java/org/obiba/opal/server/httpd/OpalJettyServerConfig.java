/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.server.httpd;

import org.obiba.opal.web.security.OpalAuth;
import org.obiba.shiro.web.filter.AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpalJettyServerConfig {

  @Bean
  public AuthenticationFilter authenticationFilter() {
    AuthenticationFilter filter = new AuthenticationFilter();
    filter.setHeaderCredentials(OpalAuth.CREDENTIALS_HEADER);
    filter.setSessionIdCookieName("opalsid");
    filter.setRequestIdCookieName("opalrid");
    return filter;
  }

}
