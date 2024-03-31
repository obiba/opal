/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.ws.intercept;

import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.specimpl.BuiltResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@Provider
@ServerInterceptor
@SecurityPrecedence
public class RequestCycleInterceptor implements PreProcessInterceptor, PostProcessInterceptor {

  private static final String REQ_ATTR = "__" + RequestCycleInterceptor.class.getName();

  private final RequestAttributesProvider requestAttributesProvider;

  private final List<RequestCyclePreProcess> preProcesses;

  private final List<RequestCyclePostProcess> postProcesses;

  @Autowired
  public RequestCycleInterceptor(RequestAttributesProvider provider, Set<RequestCyclePreProcess> preProcesses,
                                 Set<RequestCyclePostProcess> postProcesses) {
    requestAttributesProvider = provider;
    this.preProcesses = new ArrayList<>(preProcesses);
    this.postProcesses = new ArrayList<>(postProcesses);
    Collections.sort(this.preProcesses, new AnnotationAwareOrderComparator());
    Collections.sort(this.postProcesses, new AnnotationAwareOrderComparator());
  }

  @Override
  public ServerResponse preProcess(HttpRequest request, ResourceMethodInvoker method)
      throws Failure, WebApplicationException {
    new RequestCycle(request, method);

    HttpServletRequest httpServletRequest = requestAttributesProvider.currentRequestAttributes().getRequest();
    for (RequestCyclePreProcess p : preProcesses) {
      Response r = p.preProcess(httpServletRequest, request, method);
      if (r != null) {
        return r instanceof ServerResponse
            ? (ServerResponse) r
            : new ServerResponse((BuiltResponse) Response.fromResponse(r).build());
      }
    }

    return null;
  }

  @Override
  public void postProcess(ServerResponse response) {
    RequestCycle cycle = getCurrentCycle();
    if (cycle != null) {
      HttpServletRequest httpServletRequest = requestAttributesProvider.currentRequestAttributes().getRequest();
      for (RequestCyclePostProcess p : postProcesses) {
        p.postProcess(httpServletRequest, cycle.request, cycle.resourceMethod, response);
      }
    }
  }

  RequestCycle getCurrentCycle() {
    return (RequestCycle) requestAttributesProvider.currentRequestAttributes()
        .getAttribute(REQ_ATTR, RequestAttributes.SCOPE_REQUEST);
  }

  private final class RequestCycle {

    private final HttpRequest request;

    private final ResourceMethodInvoker resourceMethod;

    private RequestCycle(HttpRequest request, ResourceMethodInvoker method) {
      this.request = request;
      resourceMethod = method;
      requestAttributesProvider.currentRequestAttributes()
          .setAttribute(REQ_ATTR, this, RequestAttributes.SCOPE_REQUEST);
    }

  }

}
