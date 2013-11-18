/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.obiba.opal.audit.OpalUserProvider;
import org.obiba.opal.web.ws.intercept.RequestCyclePostProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class AuditInterceptor implements RequestCyclePostProcess {

  private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);

  private static final String LOG_FORMAT = "{} - {} - {} - {}";

  @Autowired
  private OpalUserProvider opalUserProvider;

  @Override
  public void postProcess(HttpRequest request, ResourceMethodInvoker resourceMethod, ServerResponse response) {
    logServerError(request, response);
    logClientError(request, response);
    logInfo(request, response);
    logHeaders(request);
  }

  private Object[] getArguments(HttpRequest request, ServerResponse response) {
    // TODO get the remote IP
    return new Object[] { opalUserProvider.getUsername(), response.getStatus(), request.getHttpMethod(),
        request.getUri().getPath(true) };
  }

  private void logServerError(HttpRequest request, ServerResponse response) {
    if(!log.isErrorEnabled()) return;
    if(response.getStatus() < HttpStatus.SC_INTERNAL_SERVER_ERROR) return;

    log.error(LOG_FORMAT, getArguments(request, response));
  }

  private void logClientError(HttpRequest request, ServerResponse response) {
    if(!log.isWarnEnabled()) return;
    if(response.getStatus() < HttpStatus.SC_BAD_REQUEST) return;
    if(response.getStatus() >= HttpStatus.SC_INTERNAL_SERVER_ERROR) return;

    log.warn(LOG_FORMAT, getArguments(request, response));
  }

  private void logInfo(HttpRequest request, ServerResponse response) {
    if(!log.isInfoEnabled()) return;
    if(response.getStatus() >= HttpStatus.SC_BAD_REQUEST) return;

    boolean logged = false;
    if(response.getStatus() == HttpStatus.SC_CREATED) {
      URI resourceUri = (URI) response.getMetadata().getFirst(HttpHeaders.LOCATION);
      if(resourceUri != null) {
        String path = resourceUri.getPath().substring("/ws".length());
        List<Object> args = Lists.newArrayList(getArguments(request, response));
        args.add(path);
        log.info(LOG_FORMAT + " - {}", args.toArray());
        logged = true;
      }
    }

    if(!logged) {
      log.info(LOG_FORMAT, getArguments(request, response));
    }
  }

  private void logHeaders(HttpRequest request) {
    if(!log.isTraceEnabled()) return;

    MultivaluedMap<String, String> headers = request.getHttpHeaders().getRequestHeaders();
    for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
      for(String value : entry.getValue()) {
        log.trace("{}={}", entry.getKey(), value);
      }
    }
  }
}
