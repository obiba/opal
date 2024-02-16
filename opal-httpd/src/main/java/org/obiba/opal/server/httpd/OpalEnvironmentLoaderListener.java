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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;

import jakarta.servlet.ServletContext;

public class OpalEnvironmentLoaderListener extends EnvironmentLoaderListener {

  @Override
  protected WebEnvironment determineWebEnvironment(ServletContext servletContext) {
    DefaultWebEnvironment webEnvironment = new DefaultWebEnvironment();
    webEnvironment.setServletContext(servletContext);
    webEnvironment.setSecurityManager(SecurityUtils.getSecurityManager());
    return webEnvironment;
  }
}
