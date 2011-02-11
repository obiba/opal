/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import java.lang.reflect.Method;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.shiro.mgt.SessionsSecurityManager;
import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.DefaultOptionsMethodException;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.util.IsHttpMethod;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Component
@ServerInterceptor
@Precedence("HEADER_DECORATOR")
public class AuthorizationInterceptor extends AbstractSecurityComponent implements PreProcessInterceptor, PostProcessInterceptor, ExceptionMapper<DefaultOptionsMethodException> {

  private static final Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);

  private final RequestAttributesProvider requestAttributeProvider;

  @Autowired
  public AuthorizationInterceptor(SessionsSecurityManager securityManager, RequestAttributesProvider requestAttributeProvider) {
    super(securityManager);
    this.requestAttributeProvider = requestAttributeProvider;
  }

  @Override
  public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
    if(HttpMethod.GET.equals(request.getHttpMethod())) {
      addAllowHeader(request, method);
    }
    return null;
  }

  @Override
  public void postProcess(ServerResponse response) {
    Set<String> allow = (Set<String>) requestAttributeProvider.currentRequestAttributes().getAttribute("__OPAL__ALLOW__", RequestAttributes.SCOPE_REQUEST);
    if(allow != null) {
      response.getMetadata().add("Allow", asHeader(allow));
    }
    if(response.getStatus() == 201) {
      // Add permissions
    }
  }

  @Override
  public Response toResponse(DefaultOptionsMethodException exception) {
    ServerResponse response = (ServerResponse) exception.getResponse();
    String availableMethods = (String) response.getMetadata().getFirst("Allow");
    return allow(allowed(requestAttributeProvider.currentRequestAttributes().getRequest().getRequestURI(), ImmutableSet.of(availableMethods.split(", "))));
  }

  private Response allow(Set<String> allowed) {
    return Response.ok().header("Allow", asHeader(allowed)).build();
  }

  private String asHeader(Iterable<String> values) {
    StringBuilder sb = new StringBuilder();
    for(String s : values) {
      if(sb.length() > 0) sb.append(", ");
      sb.append(s);
    }
    return sb.toString();
  }

  /**
   * @param request
   * @param method
   */
  private void addAllowHeader(HttpRequest request, ResourceMethod method) {
    Set<String> allowed = allowed(request, method);
    requestAttributeProvider.currentRequestAttributes().setAttribute("__OPAL__ALLOW__", allowed, RequestAttributes.SCOPE_REQUEST);
  }

  private Set<String> allowed(final String uri, Set<String> availableMethods) {
    return ImmutableSet.copyOf(Iterables.filter(availableMethods, new Predicate<String>() {

      @Override
      public boolean apply(String from) {
        String perm = "magma:" + uri + ":" + from;
        boolean permitted = getSubject().isPermitted(perm);
        log.info("isPermitted({}, {})=={}", new Object[] { getSubject().getPrincipal(), perm, permitted });
        return permitted;
      }

    }));
  }

  private Set<String> allowed(final HttpRequest request, ResourceMethod method) {
    return allowed(request.getUri().getPath(), availableMethods(method));
  }

  private Set<String> availableMethods(ResourceMethod method) {
    Set<String> availableMethods = Sets.newHashSet();
    String path = getPath(method);
    for(Method otherMethod : method.getResourceClass().getMethods()) {
      Set<String> patate = IsHttpMethod.getHttpMethods(otherMethod);
      if(patate != null && isSamePath(otherMethod, path)) {
        availableMethods.addAll(patate);
      }
    }
    return availableMethods;
  }

  private String getPath(ResourceMethod method) {
    return getPath(method.getMethod());
  }

  private String getPath(Method method) {
    Path path = method.getAnnotation(Path.class);
    return path != null ? path.value() : "";
  }

  /**
   * @param otherMethod
   * @param path
   * @return
   */
  private boolean isSamePath(Method otherMethod, String path) {
    return path.equals(getPath(otherMethod));
  }
}
