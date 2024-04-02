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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.obiba.opal.web.ws.intercept.RequestCyclePreProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Basic CSRF detection.
 */
@Component
public class CSRFInterceptor extends AbstractSecurityComponent implements RequestCyclePreProcess {

  private static final Logger log = LoggerFactory.getLogger(CSRFInterceptor.class);

  private static final String HOST_HEADER = "Host";

  private static final String REFERER_HEADER = "Referer";

  private static final Pattern localhostPattern = Pattern.compile("^http[s]?://localhost:.*");

  private static final Pattern loopbackhostPattern = Pattern.compile("^http[s]?://127\\.0\\.0\\.1:.*");

  private final boolean productionMode;

  private final List<String> csrfAllowed;

  @Autowired
  public CSRFInterceptor(@Value("${productionMode}") boolean productionMode,
                         @Value("${csrf.allowed}") String csrfAllowed) {
    this.productionMode = productionMode;
    this.csrfAllowed = Strings.isNullOrEmpty(csrfAllowed) ? Lists.newArrayList() : Splitter.on(",").splitToList(csrfAllowed.trim());
  }

  @Override
  public void preProcess(HttpServletRequest httpServletRequest, ResourceMethodInvoker resourceMethod, ContainerRequestContext requestContext) {
    if (!productionMode || csrfAllowed.contains("*")) return;

    String host = requestContext.getHeaderString(HOST_HEADER);
    String referer = requestContext.getHeaderString(REFERER_HEADER);
    if (referer != null) {
      String refererHostPort = "";
      try {
        URI refererURI = URI.create(referer);
        refererHostPort = refererURI.getHost() + (refererURI.getPort() > 0 ? ":" + refererURI.getPort() : "");
      } catch (Exception e) {
        // malformed url
      }
      // explicitly ok
      if (csrfAllowed.contains(refererHostPort)) return;

      boolean forbidden = false;
      if (!matchesLocalhost(host) && !referer.startsWith(String.format("https://%s/", host))) {
        forbidden = true;
      }

      if (forbidden) {
        log.warn("CSRF detection: Host={}, Referer={}", host, referer);
        log.info(">> You can add {} to csrf.allowed setting", refererHostPort);
        throw new ForbiddenException("CSRF error");
      }
    }
    return;
  }

  private boolean matchesLocalhost(String host) {
    return localhostPattern.matcher(host).matches()
        || loopbackhostPattern.matcher(host).matches()
        || host.startsWith("localhost:")
        || host.startsWith("127.0.0.1:");
  }

  static String asHeader(Iterable<String> values) {
    StringBuilder sb = new StringBuilder();
    for (String s : values) {
      if (!sb.isEmpty()) sb.append(", ");
      sb.append(s);
    }
    return sb.toString();
  }

}
