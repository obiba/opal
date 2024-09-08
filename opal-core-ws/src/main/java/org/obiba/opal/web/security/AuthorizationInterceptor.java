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

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.util.IsHttpMethod;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.service.security.realm.OpalTokenRealm;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.obiba.opal.web.ws.intercept.RequestCyclePostProcess;
import org.obiba.opal.web.ws.intercept.RequestCyclePreProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static jakarta.ws.rs.HttpMethod.GET;
import static jakarta.ws.rs.HttpMethod.OPTIONS;
import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
public class AuthorizationInterceptor extends AbstractSecurityComponent
    implements RequestCyclePreProcess, RequestCyclePostProcess {

  private static final Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);

  private static final String ALLOW_HTTP_HEADER = "Allow";

  public static final String ALT_LOCATION = "X-Alt-Location";

  public static final String ALT_PERMISSIONS = "X-Alt-Permissions";

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private RequestAttributesProvider requestAttributeProvider;

  @Override
  public void preProcess(HttpServletRequest httpServletRequest, ResourceMethodInvoker resourceMethod, ContainerRequestContext requestContext) {
    if (OPTIONS.equals(requestContext.getMethod())) {
      // Allow header will be added on postProcess
      //return Response.ok().build();
    }
    if (!isWebServicePublic(resourceMethod) && !isWebServiceWithoutAuthorization(resourceMethod) && isUserAuthenticated() &&
        !getSubject().isPermitted("rest:" + getResourceMethodPath(requestContext) + ":" + requestContext.getMethod())) {
      throw new ForbiddenException();
    }
  }

  @Override
  public void postProcess(HttpServletRequest httpServletRequest, ResourceMethodInvoker resourceMethod, ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if(GET.equals(requestContext.getMethod()) || OPTIONS.equals(requestContext.getMethod())) {
      Set<String> allowed = allowed(requestContext, resourceMethod);
      if(!allowed.isEmpty()) {
        responseContext.getHeaders().putSingle(ALLOW_HTTP_HEADER, asHeader(allowed));
      }
    } else if(HttpMethod.DELETE.equals(requestContext.getMethod()) && responseContext.getStatus() == HttpStatus.SC_OK) {
      // TODO delete all nodes starting with resource
      String resource = requestAttributeProvider.getResourcePath(requestContext.getUriInfo().getRequestUri());
      subjectAclService.deleteNodePermissions(resource);
    }

    if (OPTIONS.equals(requestContext.getMethod())) {
      // OK is always expected on that method
      responseContext.setStatus(200);
      responseContext.setEntity(requestContext.getHeaders().getFirst(ALLOW_HTTP_HEADER));
    }

    if(responseContext.getStatus() == HttpStatus.SC_CREATED) {
      addPermissions(responseContext);
    }

    Subject subject = ThreadContext.getSubject();
    if (subject != null && subject.isAuthenticated() && !subject.getPrincipals().getRealmNames().contains(OpalTokenRealm.TOKEN_REALM)) {
      Session session = SecurityUtils.getSubject().getSession(false);
      if (session != null && session.getAttribute("ensuredProfileGroups") == null) {
        Set<String> roles = (Set<String>) session.getAttribute("roles");
        if (roles != null && !roles.isEmpty()) {
          log.debug("{} has roles {}", subject.getPrincipal(), Joiner.on(",").join(roles));
          subjectProfileService.applyProfileGroups(subject.getPrincipal().toString(), roles);
          session.setAttribute("ensuredProfileGroups", true);
        }
      }
    }
  }

  static String asHeader(Iterable<String> values) {
    StringBuilder sb = new StringBuilder();
    for(String s : values) {
      if(sb.length() > 0) sb.append(", ");
      sb.append(s);
    }
    return sb.toString();
  }

  protected String getResourceMethodPath(ContainerRequestContext requestContext) {
    return requestContext.getUriInfo().getPath();
  }

  @SuppressWarnings({ "unchecked", "ConstantConditions" })
  private void addPermissions(ContainerResponseContext responseContext) {
    // Add permissions
    String resourceUriStr = responseContext.getHeaderString(HttpHeaders.LOCATION);
    if(resourceUriStr == null) {
      throw new IllegalStateException("Missing Location header in 201 response");
    }
    URI resourceUri;
    try {
      resourceUri = new URI(resourceUriStr);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Malformed Location header in 201 response");
    }
    List<?> permissions = responseContext.getHeaders().get(ALT_PERMISSIONS);
    if(permissions == null) {
      List<?> altLocations = responseContext.getHeaders().get(ALT_LOCATION);
      Iterable<URI> locations = ImmutableList.of(resourceUri);
      if(altLocations != null) {
        locations = Iterables.concat(locations, (Iterable<URI>) altLocations);
      }
      addPermissionUris(locations);
    } else {
      addPermission((Iterable<SubjectAclService.Permissions>) permissions);
      responseContext.getHeaders().remove(ALT_PERMISSIONS);
    }
  }

  private void addPermission(Iterable<SubjectAclService.Permissions> resourcePermissions) {
    for(SubjectAclService.Permissions resourcePermission : resourcePermissions) {
      for(String perm : resourcePermission.getPermissions()) {
        subjectAclService.addSubjectPermission(resourcePermission.getDomain(), resourcePermission.getNode(),
            SubjectType.USER.subjectFor(getPrincipal()), perm);
      }
    }
  }

  private void addPermissionUris(Iterable<URI> resourceUris) {
    for(URI resourceUri : resourceUris) {
      String resource = requestAttributeProvider.getResourcePath(resourceUri);
      if(isUserAuthenticated() && !getSubject().isPermitted("rest:" + resource + ":*")) {
        subjectAclService
            .addSubjectPermission("rest", resource, SubjectType.USER.subjectFor(getPrincipal()),
                "*:GET/*");
      }
    }
  }

  /**
   * Returns a {@code Set} of allowed HTTP methods on the provided resource URI. Note that this method always includes
   * "OPTIONS" in the set.
   *
   * @param uri
   * @param availableMethods
   * @return
   */
  static Set<String> allowed(final String uri, Iterable<String> availableMethods) {
    return ImmutableSet.copyOf(Iterables.concat(Iterables.filter(availableMethods, new Predicate<String>() {

      @Override
      public boolean apply(String from) {
        if (!isUserAuthenticated()) return false;
        String perm = "rest:" + uri + ":" + from;
        boolean permitted = getSubject().isPermitted(perm);
        log.debug("isPermitted({}, {})=={}", getSubject().getPrincipal(), perm, permitted);
        return permitted;
      }

    }), ImmutableSet.of(OPTIONS)));
  }

  private Set<String> allowed(ContainerRequestContext requestContext, ResourceMethodInvoker method) {
    return allowed(getResourceMethodPath(requestContext), availableMethods(method));
  }

  private Iterable<String> availableMethods(ResourceMethodInvoker method) {
    Set<String> availableMethods = Sets.newHashSet();
    if (method != null) {
      String path = getPath(method);
      for (Method otherMethod : method.getResourceClass().getMethods()) {
        Set<String> httpMethods = IsHttpMethod.getHttpMethods(otherMethod);
        if (httpMethods != null && isSamePath(otherMethod, path)) {
          availableMethods.addAll(httpMethods);
        }
      }
    }
    return availableMethods;
  }

  private String getPath(ResourceMethodInvoker method) {
    return getPath(method.getMethod());
  }

  @NotNull
  private String getPath(Method method) {
    Path path = method.getAnnotation(Path.class);
    return path == null ? "" : path.value();
  }

  private boolean isSamePath(Method otherMethod, String path) {
    return path.equals(getPath(otherMethod));
  }
}
