/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws.intercept;

import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

@Component
@ServerInterceptor
@SecurityPrecedence
public class RequestCycleInterceptor implements PreProcessInterceptor, PostProcessInterceptor {

  private static final String REQ_ATTR = "__" + RequestCycleInterceptor.class.getName();

  private final RequestAttributesProvider requestAttributesProvider;

  private final Set<RequestCyclePostProcess> postProcesses;

  @Autowired
  public RequestCycleInterceptor(RequestAttributesProvider provider, Set<RequestCyclePostProcess> postProcesses) {
    this.requestAttributesProvider = provider;
    this.postProcesses = postProcesses;
  }

  @Override
  public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
    new RequestCycle(request, method);
    return null;
  }

  @Override
  public void postProcess(ServerResponse response) {
    RequestCycle cycle = getCurrentCycle();
    if(cycle != null) {
      for(RequestCyclePostProcess p : postProcesses) {
        p.postProces(cycle.request, cycle.resourceMethod, response);
      }
    }
  }

  RequestCycle getCurrentCycle() {
    return (RequestCycle) requestAttributesProvider.currentRequestAttributes().getAttribute(REQ_ATTR, RequestAttributes.SCOPE_REQUEST);
  }

  private final class RequestCycle {

    private final HttpRequest request;

    private final ResourceMethod resourceMethod;

    private RequestCycle(HttpRequest request, ResourceMethod method) {
      this.request = request;
      this.resourceMethod = method;
      requestAttributesProvider.currentRequestAttributes().setAttribute(REQ_ATTR, this, RequestAttributes.SCOPE_REQUEST);
    }

  }

}
