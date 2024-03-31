/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.ws.inject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.specimpl.ResteasyUriInfo;
import org.obiba.opal.web.ws.cfg.OpalWsConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

@Component
public class SpringRequestAttributesProvider implements RequestAttributesProvider {

  @Override
  public ServletRequestAttributes currentRequestAttributes() {
    RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
    if (attributes instanceof ServletRequestAttributes) {
      return (ServletRequestAttributes) attributes;
    }
    throw new IllegalStateException("Not a servlet request");
  }

  @Override
  public UriInfo getUriInfo() {
    return extractUriInfo(currentRequestAttributes().getRequest(), OpalWsConfig.WS_ROOT);
  }

  @Override
  public String getResourcePath(URI uri) {
    return uri.getPath().replaceFirst(OpalWsConfig.WS_ROOT, "");
  }


  private static ResteasyUriInfo extractUriInfo(HttpServletRequest request, String servletPrefix) {
    String contextPath = request.getContextPath();
    if (servletPrefix != null && !servletPrefix.isEmpty() && !servletPrefix.equals("/")) {
      if (!contextPath.endsWith("/") && !servletPrefix.startsWith("/"))
        contextPath += "/";
      contextPath += servletPrefix;
    }
    return new ResteasyUriInfo(request.getRequestURL().toString(), contextPath);
  }
}
