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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@Provider
public class RequestCycleInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

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
  public void filter(ContainerRequestContext requestContext) throws IOException {
    HttpServletRequest httpServletRequest = requestAttributesProvider.currentRequestAttributes().getRequest();
    ResourceMethodInvoker methodInvoker = getResourceMethodInvoker(requestContext);
    for (RequestCyclePreProcess p : preProcesses) {
      p.preProcess(httpServletRequest, methodInvoker, requestContext);
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    HttpServletRequest httpServletRequest = requestAttributesProvider.currentRequestAttributes().getRequest();
    ResourceMethodInvoker methodInvoker = getResourceMethodInvoker(requestContext);
    for (RequestCyclePostProcess p : postProcesses) {
      p.postProcess(httpServletRequest, methodInvoker, requestContext, responseContext);
    }
  }

  private ResourceMethodInvoker getResourceMethodInvoker(ContainerRequestContext requestContext) {
    ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
//    if (methodInvoker != null) {
//      String methodName = methodInvoker.getMethod().getName();
//      String className = methodInvoker.getMethod().getDeclaringClass().getName();
//      System.out.println("Intercepting incoming request to method: " + methodName + " in class: " + className);
//    }
    return methodInvoker;
  }

}
