/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.datashield.cfg.RestrictedROperation;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RScriptROperation;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.net.URI;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Every way of writing a symbol into a DataSHIELD session with client-supplied R code must go through the restricted
 * parser: the text/plain symbol PUT and the form-encoded symbols POST used to reach the R server verbatim.
 */
public class DataShieldRestrictedAssignmentTest {

  private static final String FORBIDDEN_SCRIPT = "system('id')";

  private static final String ALLOWED_SCRIPT = "c(1, 2)";

  private RServerSession session;

  private DataShieldSymbolResourceImpl symbolResource;

  private DataShieldSessionResourceImpl sessionResource;

  @Before
  public void setUp() {
    login(); // the audit log names the user behind every parsed script
    DataShieldProfile profile = new DataShieldProfile("default");
    profile.getEnvironment(DSMethodType.ASSIGN).addOrUpdate(new DefaultDSMethod("c", "base::c"));
    session = mock(RServerSession.class);
    when(session.getId()).thenReturn("rid");
    when(session.getProfile()).thenReturn(profile);
    when(session.getExecutionContext()).thenReturn(DatashieldSessionsResourceImpl.DS_CONTEXT);
    // the constructor only stores its collaborators; none of them is needed to resolve the parser version
    DataShieldProfileService profileService = new DataShieldProfileService(null, null, null, null);

    symbolResource = new DataShieldSymbolResourceImpl();
    symbolResource.setName("x");
    symbolResource.setRServerSession(session);
    inject(symbolResource, "datashieldProfileService", profileService);

    sessionResource = new DataShieldSessionResourceImpl();
    sessionResource.setRServerSession(session);
    inject(sessionResource, "datashieldProfileService", profileService);
  }

  @Test
  public void putStringRejectsAScriptTheParserRefuses() {
    Response response = symbolResource.putString(uriInfo(), FORBIDDEN_SCRIPT, false);

    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    verify(session, never()).execute(any(ROperation.class));
    verify(session, never()).executeAsync(any(ROperation.class));
  }

  @Test
  public void putStringRunsAnAllowedScriptThroughTheRestrictedParser() {
    Response response = symbolResource.putString(uriInfo(), ALLOWED_SCRIPT, false);

    assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    assertThat(onlyExecutedOperation()).isInstanceOf(RestrictedROperation.class);
  }

  @Test
  public void assignRejectsAScriptTheParserRefuses() {
    Response response = sessionResource.assign(form("y", FORBIDDEN_SCRIPT));

    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    verify(session, never()).execute(any(ROperation.class));
  }

  @Test
  public void assignRunsAnAllowedScriptThroughTheRestrictedParser() {
    sessionResource.assign(form("y", ALLOWED_SCRIPT));

    // the first operation is the assignment; the listing that follows it is not under test
    ArgumentCaptor<ROperation> captor = ArgumentCaptor.forClass(ROperation.class);
    verify(session, atLeastOnce()).execute(captor.capture());
    assertThat(captor.getAllValues().get(0)).isInstanceOf(RestrictedROperation.class);
  }

  @Test
  public void assignRejectsTheWholeFormWhenOneScriptIsRefused() {
    MultivaluedMap<String, String> form = form("y", FORBIDDEN_SCRIPT);
    form.add("z", ALLOWED_SCRIPT);

    Response response = sessionResource.assign(form);

    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test(expected = ForbiddenException.class)
  public void unrestrictedStringAssignmentIsRefusedBeforeReachingTheSession() {
    symbolResource.wrapROperation(new StringAssignROperation("x", FORBIDDEN_SCRIPT));
  }

  @Test(expected = ForbiddenException.class)
  public void unrestrictedScriptIsRefusedBeforeReachingTheSession() {
    symbolResource.wrapROperation(new RScriptROperation(FORBIDDEN_SCRIPT));
  }

  @After
  public void logout() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
    SecurityUtils.setSecurityManager(null);
  }

  private static void login() {
    DefaultSecurityManager securityManager = new DefaultSecurityManager();
    SecurityUtils.setSecurityManager(securityManager);
    Subject subject = new Subject.Builder(securityManager)
        .principals(new SimplePrincipalCollection("jsmith", "test"))
        .authenticated(true)
        .buildSubject();
    ThreadContext.bind(subject);
  }

  private ROperation onlyExecutedOperation() {
    ArgumentCaptor<ROperation> captor = ArgumentCaptor.forClass(ROperation.class);
    verify(session, times(1)).execute(captor.capture());
    return captor.getValue();
  }

  private static MultivaluedMap<String, String> form(String symbol, String script) {
    MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
    form.add(symbol, script);
    return form;
  }

  private static UriInfo uriInfo() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getRequestUri()).thenReturn(URI.create("https://opal.example.org/ws/datashield/session/rid/symbol/x"));
    return uriInfo;
  }

  private static void inject(Object target, String fieldName, Object value) {
    Field field = ReflectionUtils.findField(target.getClass(), fieldName);
    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, target, value);
  }
}
