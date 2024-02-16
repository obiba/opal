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

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionProvider;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class OpalVersionFilter extends OncePerRequestFilter {

  private Version version;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if(version == null) {
      VersionProvider opalVersionProvider = WebApplicationContextUtils
          .getRequiredWebApplicationContext(getServletContext()).getBean(VersionProvider.class);
      version = opalVersionProvider.getVersion();
    }
    response.addHeader("X-Opal-Version", version.toString());
    filterChain.doFilter(request, response);
  }

}
