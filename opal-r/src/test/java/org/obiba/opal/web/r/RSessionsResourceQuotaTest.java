/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import com.google.common.collect.Lists;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerSession;

import java.net.URI;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Where the quota is enforced, which is the whole of the enforcement contract: on the way in to a new session, and
 * nowhere else. What a quota actually says is {@link org.obiba.opal.r.service.RQuotaServiceTest}'s business.
 */
public class RSessionsResourceQuotaTest {

  private static final String REFUSAL = "DataSHIELD quota exceeded: 121 of 120 minutes used in the last 7 days.";

  private OpalRSessionManager rSessionManager;

  private TestableRSessionsResource resource;

  @Before
  public void setUp() {
    rSessionManager = mock(OpalRSessionManager.class);
    resource = new TestableRSessionsResource();
    resource.setOpalRSessionManager(rSessionManager);
  }

  @Test
  public void test_an_exhausted_quota_refuses_the_session_before_it_is_created() {
    resource.refusal = REFUSAL;

    try {
      resource.newRSession(null, null, "default", false);
      fail("an exhausted quota must refuse the session");
    } catch (ForbiddenException e) {
      assertThat(e.getMessage()).isEqualTo(REFUSAL);
    }

    assertThat(resource.quotaChecks).isEqualTo(1);
    // nothing was allocated and then thrown away: the R server was never asked for a session
    verify(rSessionManager, never()).newSubjectRSession(any(RServerProfile.class), any());
  }

  @Test
  public void test_a_session_is_created_when_the_quota_allows_it() {
    RServerSession rSession = rSession();
    when(rSessionManager.newSubjectRSession(any(RServerProfile.class), any())).thenReturn(rSession);

    Response response = resource.newRSession(null, null, "default", false);

    assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    assertThat(resource.quotaChecks).isEqualTo(1);
    verify(rSessionManager).newSubjectRSession(any(RServerProfile.class), any());
  }

  /**
   * The administrator's profile smoke test opens a session and drops it again. Refusing it on a quota would make an
   * exhausted user's profile look broken, so it does not go through the gate at all.
   */
  @Test
  public void test_the_profile_smoke_test_ignores_the_quota() {
    RServerSession rSession = rSession();
    when(rSessionManager.newSubjectRSession(any(RServerProfile.class))).thenReturn(rSession);
    resource.refusal = REFUSAL;

    Response response = resource.testNewRSession("default");

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(resource.quotaChecks).isEqualTo(0);
    verify(rSessionManager).removeRSession("session-1");
  }

  /**
   * Plain R carries no quota of its own: the base implementation lets everything through, and only DataSHIELD
   * overrides it. Enforcing R quotas would need a context of its own, which v1 does not have.
   */
  @Test
  public void test_the_base_check_lets_everything_through() {
    new RSessionsResourceImpl().checkQuota();
  }

  private RServerSession rSession() {
    RServerProfile profile = mock(RServerProfile.class);
    when(profile.getName()).thenReturn("default");
    when(profile.getCluster()).thenReturn("default");
    RServerSession rSession = mock(RServerSession.class);
    when(rSession.getId()).thenReturn("session-1");
    when(rSession.getUser()).thenReturn("jsmith");
    when(rSession.getCreated()).thenReturn(new Date());
    when(rSession.getTimestamp()).thenReturn(new Date());
    when(rSession.getState()).thenReturn(RServerSession.State.RUNNING);
    when(rSession.getExecutionContext()).thenReturn("DataSHIELD");
    when(rSession.getProfile()).thenReturn(profile);
    when(rSession.getRServerServiceName()).thenReturn("default");
    when(rSession.getEvents()).thenReturn(Lists.newArrayList());
    return rSession;
  }

  /**
   * Stands in for the DataSHIELD resource: it counts the quota checks and can refuse, without needing a quota service
   * or an authenticated subject. Everything else that would reach out of the process is neutralised.
   */
  private static class TestableRSessionsResource extends RSessionsResourceImpl {

    private int quotaChecks;

    private String refusal;

    @Override
    protected boolean createRSessionEnabled() {
      return true;
    }

    @Override
    protected void checkAuthenticationMethod() {
    }

    @Override
    protected void checkQuota() throws ForbiddenException {
      quotaChecks++;
      if (refusal != null) throw new ForbiddenException(refusal);
    }

    @Override
    protected RServerProfile createProfile(String profileName) {
      return new DefaultRServerProfile(profileName);
    }

    @Override
    protected void onNewRSession(RServerSession rSession) {
    }

    @Override
    URI getLocation(UriInfo info, String id) {
      return URI.create("http://localhost:8080/ws/r/session/" + id);
    }
  }
}
