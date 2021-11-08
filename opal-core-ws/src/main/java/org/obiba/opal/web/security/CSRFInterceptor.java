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
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.spi.HttpRequest;
import org.obiba.opal.web.ws.intercept.RequestCyclePreProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Component
public class CSRFInterceptor extends AbstractSecurityComponent implements RequestCyclePreProcess {

  private static final Logger log = LoggerFactory.getLogger(CSRFInterceptor.class);

  private static final String HOST_HEADER = "Host";

  private static final String REFERER_HEADER = "Referer";

  @Nullable
  @Override
  public Response preProcess(HttpRequest request, ResourceMethodInvoker method) {
    String host = request.getHttpHeaders().getHeaderString(HOST_HEADER);
    String referer = request.getHttpHeaders().getHeaderString(REFERER_HEADER);
    log.info("host={} referer={}", host, referer);
    if (referer != null) {
      if ("localhost:8080".equals(host)) {
        if (!referer.startsWith(String.format("http://%s", host)))
          return Response.status(Status.FORBIDDEN).build();
      } else if (!referer.startsWith(String.format("https://%s", host)))
        return Response.status(Status.FORBIDDEN).build();
    }
    return null;
  }

  static String asHeader(Iterable<String> values) {
    StringBuilder sb = new StringBuilder();
    for (String s : values) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(s);
    }
    return sb.toString();
  }

}
