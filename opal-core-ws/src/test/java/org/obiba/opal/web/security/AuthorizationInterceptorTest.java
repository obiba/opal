/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.objenesis.ObjenesisStd;
import org.junit.After;
import org.junit.Test;
import org.obiba.opal.web.system.subject.SubjectProfileCurrentResource;
import org.obiba.opal.web.system.subject.SubjectProfileResource;

import java.lang.reflect.Method;

import static org.easymock.EasyMock.*;

/**
 * The authorization interceptor must submit every non-self-service method to the {@code rest:} permission check.
 */
public class AuthorizationInterceptorTest {

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test(expected = ForbiddenException.class)
  public void disableOtpOfAnotherPrincipalIsForbiddenWithoutPermission() throws Exception {
    Subject subject = bindAuthenticatedSubject();
    expect(subject.isPermitted("rest:/system/subject-profile/administrator/otp:DELETE")).andReturn(false);
    replay(subject);

    new AuthorizationInterceptor().preProcess(null,
        invokerOf(SubjectProfileResource.class, "disableOtp"),
        requestOf(HttpMethod.DELETE, "/system/subject-profile/administrator/otp"));
  }

  @Test
  public void disableOtpOfAnotherPrincipalIsAllowedWithPermission() throws Exception {
    Subject subject = bindAuthenticatedSubject();
    expect(subject.isPermitted("rest:/system/subject-profile/administrator/otp:DELETE")).andReturn(true);
    replay(subject);

    new AuthorizationInterceptor().preProcess(null,
        invokerOf(SubjectProfileResource.class, "disableOtp"),
        requestOf(HttpMethod.DELETE, "/system/subject-profile/administrator/otp"));

    verify(subject);
  }

  @Test
  public void disableOwnOtpNeedsNoPermission() throws Exception {
    // a strict mock: any isPermitted() call would fail the test
    Subject subject = bindAuthenticatedSubject();
    replay(subject);

    new AuthorizationInterceptor().preProcess(null,
        invokerOf(SubjectProfileCurrentResource.class, "disableOtp"),
        requestOf(HttpMethod.DELETE, "/system/subject-profile/_current/otp"));

    verify(subject);
  }

  private Subject bindAuthenticatedSubject() {
    Subject subject = createStrictMock(Subject.class);
    expect(subject.isAuthenticated()).andReturn(true).anyTimes();
    ThreadContext.bind(subject);
    return subject;
  }

  /**
   * {@link ResourceMethodInvoker} is a concrete class whose constructor needs the whole RESTEasy runtime, and the
   * cglib bundled with EasyMock cannot proxy it on a recent JVM: the stub is instantiated without running any
   * constructor and only answers the two accessors the interceptor reads.
   */
  private ResourceMethodInvoker invokerOf(Class<?> resourceClass, String methodName) throws NoSuchMethodException {
    StubInvoker invoker = new ObjenesisStd().newInstance(StubInvoker.class);
    invoker.method = resourceClass.getMethod(methodName);
    invoker.resourceClass = resourceClass;
    return invoker;
  }

  private static class StubInvoker extends ResourceMethodInvoker {

    private Method method;

    private Class<?> resourceClass;

    private StubInvoker() {
      super(null, null, null, null); // never executed, see invokerOf()
    }

    @Override
    public Method getMethod() {
      return method;
    }

    @Override
    public Class<?> getResourceClass() {
      return resourceClass;
    }
  }

  private ContainerRequestContext requestOf(String httpMethod, String path) {
    UriInfo uriInfo = createNiceMock(UriInfo.class);
    expect(uriInfo.getPath()).andReturn(path).anyTimes();
    replay(uriInfo);
    ContainerRequestContext request = createNiceMock(ContainerRequestContext.class);
    expect(request.getMethod()).andReturn(httpMethod).anyTimes();
    expect(request.getUriInfo()).andReturn(uriInfo).anyTimes();
    replay(request);
    return request;
  }
}
