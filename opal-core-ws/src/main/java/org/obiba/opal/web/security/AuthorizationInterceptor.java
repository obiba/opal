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
import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.core.ResourceInvoker;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.util.IsHttpMethod;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.obiba.opal.web.ws.intercept.RequestCyclePostProcess;
import org.obiba.opal.web.ws.intercept.RequestCyclePreProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.OPTIONS;
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
  private RequestAttributesProvider requestAttributeProvider;

  @Nullable
  @Override
  public Response preProcess(HttpRequest request, ResourceMethodInvoker method) {
    if(OPTIONS.equals(request.getHttpMethod())) {
      // Allow header will be added on postProcess
      return Response.ok().build();
    }
    if(!isWebServicePublic(method) && !isWebServiceWithoutAuthorization(method) &&
        !getSubject().isPermitted("rest:" + getResourceMethodUri(request) + ":" + request.getHttpMethod())) {
      return Response.status(Status.FORBIDDEN).build();
    }
    return null;
  }

  @Override
  public void postProcess(HttpRequest request, ResourceMethodInvoker resourceMethod, ServerResponse response) {
    if(GET.equals(request.getHttpMethod()) || OPTIONS.equals(request.getHttpMethod())) {
      Set<String> allowed = allowed(request, resourceMethod);
      if(allowed != null && allowed.size() > 0) {
        response.getMetadata().add(ALLOW_HTTP_HEADER, asHeader(allowed));
      }
    } else if(HttpMethod.DELETE.equals(request.getHttpMethod()) && response.getStatus() == HttpStatus.SC_OK) {
      // TODO delete all nodes starting with resource
      String resource = requestAttributeProvider.getResourcePath(request.getUri().getRequestUri());
      subjectAclService.deleteNodePermissions(resource);
    }

    if(response.getStatus() == HttpStatus.SC_CREATED) {
      addPermissions(response);
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

  protected String getResourceMethodUri(HttpRequest request) {
    return request.getUri().getPath();
  }

  @SuppressWarnings({ "unchecked", "ConstantConditions" })
  private void addPermissions(ServerResponse response) {
    // Add permissions
    URI resourceUri = (URI) response.getMetadata().getFirst(HttpHeaders.LOCATION);
    if(resourceUri == null) {
      throw new IllegalStateException("Missing Location header in 201 response");
    }
    List<?> permissions = response.getMetadata().get(ALT_PERMISSIONS);
    if(permissions == null) {
      List<?> altLocations = response.getMetadata().get(ALT_LOCATION);
      Iterable<URI> locations = ImmutableList.of(resourceUri);
      if(altLocations != null) {
        locations = Iterables.concat(locations, (Iterable<URI>) altLocations);
      }
      addPermissionUris(locations);
    } else {
      addPermission((Iterable<SubjectAclService.Permissions>) permissions);
      response.getMetadata().remove(ALT_PERMISSIONS);
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

  public void addPermissionUris(Iterable<URI> resourceUris) {
    for(URI resourceUri : resourceUris) {
      String resource = requestAttributeProvider.getResourcePath(resourceUri);
      if(!getSubject().isPermitted("rest:" + resource + ":*")) {
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
        String perm = "rest:" + uri + ":" + from;
        boolean permitted = getSubject().isPermitted(perm);
        log.debug("isPermitted({}, {})=={}", getSubject().getPrincipal(), perm, permitted);
        return permitted;
      }

    }), ImmutableSet.of(OPTIONS)));
  }

  private Set<String> allowed(HttpRequest request, ResourceMethodInvoker method) {
    return allowed(request.getUri().getPath(), availableMethods(method));
  }

  private Iterable<String> availableMethods(ResourceMethodInvoker method) {
    Set<String> availableMethods = Sets.newHashSet();
    String path = getPath(method);
    for(Method otherMethod : method.getResourceClass().getMethods()) {
      Set<String> httpMethods = IsHttpMethod.getHttpMethods(otherMethod);
      if(httpMethods != null && isSamePath(otherMethod, path)) {
        availableMethods.addAll(httpMethods);
      }
    }
    return availableMethods;
  }

  private String getPath(ResourceInvoker method) {
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
