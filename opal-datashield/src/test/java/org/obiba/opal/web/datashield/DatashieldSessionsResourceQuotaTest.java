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

import com.google.common.collect.Lists;
import jakarta.ws.rs.ForbiddenException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.r.service.RQuota;
import org.obiba.opal.r.service.RQuotaService;
import org.obiba.opal.r.service.RQuotaUsage;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What the DataSHIELD gate does with the usages the quota service reports, one per metric. Where the gate sits on the
 * request path is {@link org.obiba.opal.web.r.RSessionsResourceQuotaTest}'s business.
 */
public class DatashieldSessionsResourceQuotaTest {

  private static final String USER = "jsmith";

  private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);

  private RQuotaService rQuotaService;

  private DatashieldSessionsResourceImpl resource;

  @Before
  public void setUp() {
    rQuotaService = mock(RQuotaService.class);
    resource = new DatashieldSessionsResourceImpl();
    Field field = ReflectionUtils.findField(DatashieldSessionsResourceImpl.class, "rQuotaService");
    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, resource, rQuotaService);
    login();
  }

  @After
  public void logout() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
    SecurityUtils.setSecurityManager(null);
  }

  @Test
  public void test_the_gate_asks_about_the_datashield_context_and_the_authenticated_user() {
    withUsages(unlimited(RQuota.Metric.EXECUTION_TIME), unlimited(RQuota.Metric.SESSION_TIME));

    resource.checkQuota();

    verify(rQuotaService).getUsages(DatashieldSessionsResourceImpl.DS_CONTEXT, USER);
  }

  /**
   * The upgrade case, and the case of every deployment that never configures a quota: nothing is enforced.
   */
  @Test
  public void test_no_quota_means_no_enforcement() {
    withUsages(unlimited(RQuota.Metric.EXECUTION_TIME), unlimited(RQuota.Metric.SESSION_TIME));

    resource.checkQuota();
  }

  @Test
  public void test_a_quota_that_is_not_spent_lets_the_session_through() {
    withUsages(usage(RQuota.Metric.EXECUTION_TIME, 98 * ONE_MINUTE, 120 * ONE_MINUTE, null, 0),
        unlimited(RQuota.Metric.SESSION_TIME));

    resource.checkQuota();
  }

  @Test
  public void test_an_exhausted_quota_refuses_the_session_with_the_numbers() {
    withUsages(usage(RQuota.Metric.EXECUTION_TIME, 121 * ONE_MINUTE, 120 * ONE_MINUTE, null, 0),
        unlimited(RQuota.Metric.SESSION_TIME));

    try {
      resource.checkQuota();
      fail("an exhausted quota must refuse the session");
    } catch (ForbiddenException e) {
      assertThat(e.getMessage())
          .isEqualTo("DataSHIELD quota exceeded: 121 of 120 minutes of execution time used in the last 7 days.");
    }
  }

  /**
   * A user turned away deserves to know when they can come back, which is the point of the rolling window.
   */
  @Test
  public void test_the_refusal_says_when_capacity_returns() {
    Date credit = new Date(1789818600000L); // 2026-09-19 12:30 UTC
    withUsages(usage(RQuota.Metric.EXECUTION_TIME, 121 * ONE_MINUTE, 120 * ONE_MINUTE, credit, 0),
        unlimited(RQuota.Metric.SESSION_TIME));

    try {
      resource.checkQuota();
      fail("an exhausted quota must refuse the session");
    } catch (ForbiddenException e) {
      assertThat(e.getMessage()).contains("Some capacity returns on ");
      assertThat(e.getMessage()).doesNotContain("GMT");
    }
  }

  /**
   * Waiting does not help a user whose session time is being spent by sessions they still hold, so the refusal tells
   * them what does.
   */
  @Test
  public void test_a_session_time_refusal_names_the_metric_and_the_open_sessions() {
    Date credit = new Date(1789818600000L);
    withUsages(unlimited(RQuota.Metric.EXECUTION_TIME),
        usage(RQuota.Metric.SESSION_TIME, 502 * ONE_MINUTE, 480 * ONE_MINUTE, credit, 2));

    try {
      resource.checkQuota();
      fail("an exhausted quota must refuse the session");
    } catch (ForbiddenException e) {
      assertThat(e.getMessage()).contains("502 of 480 minutes of session time");
      assertThat(e.getMessage()).contains("You have 2 DataSHIELD sessions open");
      assertThat(e.getMessage()).doesNotContain("Some capacity returns on ");
    }
  }

  /**
   * A user who frees up one metric and is still refused learns nothing from a message that only named the other.
   */
  @Test
  public void test_both_metrics_are_named_when_both_are_spent() {
    withUsages(usage(RQuota.Metric.EXECUTION_TIME, 121 * ONE_MINUTE, 120 * ONE_MINUTE, null, 1),
        usage(RQuota.Metric.SESSION_TIME, 502 * ONE_MINUTE, 480 * ONE_MINUTE, null, 1));

    try {
      resource.checkQuota();
      fail("an exhausted quota must refuse the session");
    } catch (ForbiddenException e) {
      assertThat(e.getMessage()).contains("minutes of execution time");
      assertThat(e.getMessage()).contains("minutes of session time");
    }
  }

  private void withUsages(RQuotaUsage... usages) {
    when(rQuotaService.getUsages(DatashieldSessionsResourceImpl.DS_CONTEXT, USER))
        .thenReturn(Lists.newArrayList(usages));
  }

  private RQuotaUsage unlimited(RQuota.Metric metric) {
    return RQuotaUsage.unlimited(DatashieldSessionsResourceImpl.DS_CONTEXT, USER, metric);
  }

  private RQuotaUsage usage(RQuota.Metric metric, long used, long limit, Date nextCreditDate, int openSessions) {
    RQuota quota = new RQuota();
    quota.setContext(DatashieldSessionsResourceImpl.DS_CONTEXT);
    quota.setSubjectType(RQuota.SubjectType.USER);
    quota.setPrincipal(USER);
    quota.setMetric(metric);
    quota.setPeriod(RQuota.Period.WEEKLY);
    quota.setLimitMillis(limit);
    Date windowStart = quota.getPeriod().getWindowStart(new Date());
    return RQuotaUsage.of(DatashieldSessionsResourceImpl.DS_CONTEXT, USER, quota, used, windowStart, nextCreditDate,
        openSessions);
  }

  private void login() {
    DefaultSecurityManager securityManager = new DefaultSecurityManager();
    SecurityUtils.setSecurityManager(securityManager);
    Subject subject = new Subject.Builder(securityManager)
        .principals(new SimplePrincipalCollection(USER, "test"))
        .authenticated(true)
        .buildSubject();
    ThreadContext.bind(subject);
  }
}
