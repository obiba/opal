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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.hc.core5.http.HttpStatus;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.obiba.opal.audit.OpalUserProvider;
import org.obiba.opal.web.ws.cfg.OpalWsConfig;
import org.obiba.opal.web.ws.intercept.RequestCyclePostProcess;
import org.obiba.opal.web.ws.intercept.RequestCyclePreProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class AuditInterceptor implements RequestCyclePreProcess, RequestCyclePostProcess {

  private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);

  private static final String LOG_FORMAT = "{}";

  private static final String[] VALID_IP_HEADER_CANDIDATES = {
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_X_FORWARDED_FOR",
      "HTTP_X_FORWARDED",
      "HTTP_X_CLUSTER_CLIENT_IP",
      "HTTP_CLIENT_IP",
      "HTTP_FORWARDED_FOR",
      "HTTP_FORWARDED",
      "HTTP_VIA",
      "REMOTE_ADDR"};

  @Autowired
  private OpalUserProvider opalUserProvider;

  @Override
  public void preProcess(HttpServletRequest httpServletRequest, ResourceMethodInvoker resourceMethod, ContainerRequestContext requestContext) {
    MDC.put("ip", getClientIP(httpServletRequest, requestContext));
    MDC.put("method", requestContext.getMethod());
  }

  @Override
  public void postProcess(HttpServletRequest httpServletRequest, ResourceMethodInvoker resourceMethod, ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    MDC.put("username", opalUserProvider.getUsername());
    MDC.put("status", responseContext.getStatus() + "");
    MDC.put("method", requestContext.getMethod());
    if (Strings.isNullOrEmpty(MDC.get("ip")))
      MDC.put("ip", getClientIP(httpServletRequest, requestContext));

    logServerError(requestContext, responseContext);
    logClientError(requestContext, responseContext);
    logInfo(requestContext, responseContext);
    MDC.clear();
  }

  private String getArguments(ContainerRequestContext requestContext) {
    StringBuilder sb = new StringBuilder(requestContext.getUriInfo().getPath(true));
    MultivaluedMap<String, String> params = requestContext.getUriInfo().getQueryParameters();
    if (!params.isEmpty()) {
      sb.append(" queryParams:").append(params);
    }

    return sb.toString();
  }

  private String getClientIP(HttpServletRequest httpServletRequest, ContainerRequestContext requestContext) {
    String ip = "";

    for (String ipHeader : VALID_IP_HEADER_CANDIDATES) {
      ip = requestContext.getHeaders().keySet().stream()
          .filter(ipHeader::equalsIgnoreCase)
          .map(requestContext::getHeaderString)
          .findFirst().orElse("");
      if (!Strings.isNullOrEmpty(ip)) break;
    }

    if (Strings.isNullOrEmpty(ip))
       ip = httpServletRequest.getRemoteAddr();

    return ip;
  }

  private void logServerError(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (!log.isErrorEnabled()) return;
    if (responseContext.getStatus() < HttpStatus.SC_INTERNAL_SERVER_ERROR) return;

    log.error(LOG_FORMAT, getArguments(requestContext));
  }

  private void logClientError(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (!log.isWarnEnabled()) return;
    if (responseContext.getStatus() < HttpStatus.SC_BAD_REQUEST) return;
    if (responseContext.getStatus() >= HttpStatus.SC_INTERNAL_SERVER_ERROR) return;

    log.warn(LOG_FORMAT, getArguments(requestContext));
  }

  private void logInfo(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (!log.isInfoEnabled()) return;
    if (responseContext.getStatus() >= HttpStatus.SC_BAD_REQUEST) return;

    boolean logged = false;
    if (responseContext.getStatus() == HttpStatus.SC_CREATED) {
      String resourceUriStr = responseContext.getHeaderString(HttpHeaders.LOCATION);
      if (!Strings.isNullOrEmpty(resourceUriStr)) {
        try {
          URI resourceUri = new URI(resourceUriStr);
          String path = resourceUri.getPath().substring(OpalWsConfig.WS_ROOT.length());
          MDC.put("created", path);
          log.info(LOG_FORMAT, getArguments(requestContext));
          logged = true;
        } catch (URISyntaxException e) {
          // ignore
        }
      }
    }

    if (!logged) {
      log.info(LOG_FORMAT, getArguments(requestContext));
    }
  }

}
