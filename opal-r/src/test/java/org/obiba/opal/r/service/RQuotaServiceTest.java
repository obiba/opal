/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.NoSuchSubjectProfileException;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.r.repository.RQuotaRepository;
import org.obiba.opal.r.repository.RSessionActivityRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The two things the quota service decides on its own: which quota applies to a user, for each metric, and what they
 * have spent against it over its rolling window.
 */
public class RQuotaServiceTest {

  private static final String CONTEXT = "DataSHIELD";

  private static final String USER = "jsmith";

  private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);

  private RQuotaRepository quotaRepository;

  private RSessionActivityRepository activityRepository;

  private SubjectProfileService subjectProfileService;

  private OpalRSessionManager rSessionManager;

  private RQuotaService service;

  @Before
  public void setUp() {
    quotaRepository = mock(RQuotaRepository.class);
    activityRepository = mock(RSessionActivityRepository.class);
    subjectProfileService = mock(SubjectProfileService.class);
    rSessionManager = mock(OpalRSessionManager.class);
    when(rSessionManager.getRSessions()).thenReturn(Lists.newArrayList());
    service = new RQuotaService(quotaRepository, activityRepository, subjectProfileService, rSessionManager);
    withGroups();
  }

  //
  // Resolution: user > group (most permissive) > system, and absence means unlimited
  //

  @Test
  public void test_nothing_configured_resolves_to_no_quota() {
    withQuotas();

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME)).isEqualTo(Optional.empty());
  }

  @Test
  public void test_the_system_default_applies_when_nothing_else_does() {
    withQuotas(system(60));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis()).isEqualTo(60 * ONE_MINUTE);
  }

  @Test
  public void test_a_group_quota_beats_the_system_default() {
    withGroups("analysts");
    withQuotas(system(60), group("analysts", 120));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis()).isEqualTo(120 * ONE_MINUTE);
  }

  @Test
  public void test_the_most_permissive_group_quota_wins() {
    withGroups("analysts", "partners");
    withQuotas(system(60), group("analysts", 120), group("partners", 300));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis()).isEqualTo(300 * ONE_MINUTE);
  }

  @Test
  public void test_a_group_the_user_is_not_in_is_ignored() {
    withGroups("analysts");
    withQuotas(system(60), group("analysts", 120), group("partners", 300));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis()).isEqualTo(120 * ONE_MINUTE);
  }

  @Test
  public void test_a_personal_quota_beats_every_group() {
    withGroups("analysts", "partners");
    withQuotas(system(60), group("analysts", 120), group("partners", 300), user(USER, 90));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis()).isEqualTo(90 * ONE_MINUTE);
  }

  @Test
  public void test_a_personal_quota_of_somebody_else_is_ignored() {
    withQuotas(system(60), user("someone-else", 90));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis()).isEqualTo(60 * ONE_MINUTE);
  }

  /**
   * A disabled quota is invisible to the resolution, not an exemption granted to its subject: the search has to fall
   * through it to the next level. The service expresses that by only ever asking for the enabled ones, which is what
   * this pins - the filtering itself is the repository's, and is covered by the schema it queries.
   */
  @Test
  public void test_only_enabled_quotas_take_part_in_the_resolution() {
    withQuotas(system(60));

    service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME);

    org.mockito.Mockito.verify(quotaRepository).findByContextAndEnabledTrue(CONTEXT);
    org.mockito.Mockito.verify(quotaRepository, org.mockito.Mockito.never()).findByContext(anyString());
  }

  @Test
  public void test_a_user_without_a_profile_can_still_get_the_system_default() {
    when(subjectProfileService.getProfile(anyString())).thenThrow(new NoSuchSubjectProfileException(USER));
    withQuotas(system(60), group("analysts", 120));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis()).isEqualTo(60 * ONE_MINUTE);
  }

  //
  // Usage over the rolling window
  //

  @Test
  public void test_no_quota_means_nothing_is_measured() {
    withQuotas();

    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME);

    assertThat(usage.hasQuota()).isFalse();
    assertThat(usage.isExceeded()).isFalse();
    assertThat(usage.getWindowStart()).isNull();
  }

  @Test
  public void test_a_weekly_window_starts_seven_days_back() {
    withQuotas(system(120));
    withUsage(30 * ONE_MINUTE);

    Date before = new Date();
    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME);
    Date after = new Date();

    assertThat(usage.getWindowStart()).isNotNull();
    assertWindowStart(usage, before, after, TimeUnit.DAYS.toMillis(7));
    assertThat(usage.getUsedMillis()).isEqualTo(30 * ONE_MINUTE);
    assertThat(usage.isExceeded()).isFalse();
  }

  @Test
  public void test_a_daily_window_starts_twenty_four_hours_back() {
    RQuota quota = system(120);
    quota.setPeriod(RQuota.Period.DAILY);
    withQuotas(quota);
    withUsage(30 * ONE_MINUTE);

    Date before = new Date();
    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME);
    Date after = new Date();

    assertWindowStart(usage, before, after, TimeUnit.HOURS.toMillis(24));
  }

  /**
   * Spending exactly the allowance exhausts it, which is what makes a limit of zero mean "not at all".
   */
  @Test
  public void test_spending_exactly_the_allowance_exceeds_it() {
    withQuotas(system(120));
    withUsage(120 * ONE_MINUTE);
    withEarliestActivity(null);

    assertThat(service.getUsage(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).isExceeded()).isTrue();
  }

  @Test
  public void test_a_limit_of_zero_forbids_the_context() {
    withQuotas(system(0));
    withUsage(0);
    withEarliestActivity(null);

    assertThat(service.isExceeded(CONTEXT, USER)).isTrue();
  }

  @Test
  public void test_capacity_returns_when_the_oldest_counted_activity_leaves_the_window() {
    withQuotas(system(120));
    withUsage(121 * ONE_MINUTE);
    Date earliest = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(6));
    withEarliestActivity(earliest);

    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME);

    assertThat(usage.isExceeded()).isTrue();
    assertThat(usage.getNextCreditDate())
        .isEqualTo(new Date(earliest.getTime() + TimeUnit.DAYS.toMillis(7)));
  }

  /**
   * The second query only earns its keep when someone is waiting for the window to move.
   */
  @Test
  public void test_no_credit_date_is_computed_while_under_the_quota() {
    withQuotas(system(120));
    withUsage(30 * ONE_MINUTE);

    assertThat(service.getUsage(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).getNextCreditDate()).isNull();
  }

  //
  // The metrics are resolved independently, and both are enforced
  //

  /**
   * Limits of different metrics are not comparable, so the resolution of one must not see the quotas of the other -
   * otherwise a generous session time allowance would masquerade as the most permissive execution time quota.
   */
  @Test
  public void test_a_quota_of_the_other_metric_is_invisible_to_the_resolution() {
    withGroups("analysts");
    withQuotas(system(60), sessionTime(RQuota.SubjectType.GROUP, "analysts", 600));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME).get().getLimitMillis())
        .isEqualTo(60 * ONE_MINUTE);
    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.SESSION_TIME).get().getLimitMillis())
        .isEqualTo(600 * ONE_MINUTE);
  }

  /**
   * A user can be bounded on one axis and free on the other: nothing infers one metric from the other.
   */
  @Test
  public void test_a_metric_without_a_quota_stays_unlimited() {
    withQuotas(system(60));

    assertThat(service.resolve(CONTEXT, USER, RQuota.Metric.SESSION_TIME)).isEqualTo(Optional.empty());
    assertThat(service.getUsage(CONTEXT, USER, RQuota.Metric.SESSION_TIME).hasQuota()).isFalse();
  }

  @Test
  public void test_every_metric_is_reported_whether_or_not_a_quota_applies() {
    withQuotas(system(60));
    withUsage(30 * ONE_MINUTE);

    List<RQuotaUsage> usages = service.getUsages(CONTEXT, USER);

    assertThat(usages).hasSize(RQuota.Metric.values().length);
    assertThat(usages.stream().filter(RQuotaUsage::hasQuota).count()).isEqualTo(1);
  }

  /**
   * The gate refuses on the first bound the user has reached, whichever metric it belongs to.
   */
  @Test
  public void test_being_over_on_either_metric_is_being_over() {
    withQuotas(system(120), sessionTime(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, 480));
    withUsage(30 * ONE_MINUTE);
    withSessionTimeUsage(481 * ONE_MINUTE);
    withEarliestActivity(null);

    assertThat(service.isExceeded(CONTEXT, USER)).isTrue();
  }

  //
  // Session time, and the live tail of sessions that are still open
  //

  @Test
  public void test_session_time_usage_sums_the_session_time_column() {
    withQuotas(sessionTime(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, 480));
    withSessionTimeUsage(300 * ONE_MINUTE);

    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.SESSION_TIME);

    assertThat(usage.getUsedMillis()).isEqualTo(300 * ONE_MINUTE);
    assertThat(usage.isExceeded()).isFalse();
    verify(activityRepository).sumSessionTimeMillis(eq(USER), eq(CONTEXT), any(Date.class));
    verify(activityRepository, never()).sumExecutionTimeMillis(eq(USER), eq(CONTEXT), any(Date.class));
  }

  /**
   * A record only moves when a command ends or the session closes, so an idle open session has more session time than
   * the table says. Without this the metric would stand still for exactly the user it is meant to catch.
   */
  @Test
  public void test_an_open_session_adds_the_time_since_its_record_was_last_written() {
    withQuotas(sessionTime(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, 480));
    withSessionTimeUsage(300 * ONE_MINUTE);
    withOpenSessions(new Date(System.currentTimeMillis() - 60 * ONE_MINUTE));

    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.SESSION_TIME);

    assertThat(usage.getUsedMillis()).isGreaterThanOrEqualTo(360 * ONE_MINUTE);
    assertThat(usage.getUsedMillis()).isLessThan(361 * ONE_MINUTE);
    assertThat(usage.getOpenSessionsCount()).isEqualTo(1);
  }

  @Test
  public void test_the_live_tail_is_what_pushes_a_user_over() {
    withQuotas(sessionTime(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, 480));
    withSessionTimeUsage(470 * ONE_MINUTE);
    withOpenSessions(new Date(System.currentTimeMillis() - 20 * ONE_MINUTE));
    withEarliestActivity(null);

    assertThat(service.getUsage(CONTEXT, USER, RQuota.Metric.SESSION_TIME).isExceeded()).isTrue();
  }

  /**
   * Execution time is complete the moment a command ends, so it gets no correction - only the session count, which the
   * message needs either way.
   */
  @Test
  public void test_execution_time_is_not_corrected_by_open_sessions() {
    withQuotas(system(120));
    withUsage(30 * ONE_MINUTE);
    withOpenSessions(new Date(System.currentTimeMillis() - 60 * ONE_MINUTE));

    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.EXECUTION_TIME);

    assertThat(usage.getUsedMillis()).isEqualTo(30 * ONE_MINUTE);
    assertThat(usage.getOpenSessionsCount()).isEqualTo(1);
  }

  @Test
  public void test_no_open_session_means_no_extra_query() {
    withQuotas(sessionTime(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, 480));
    withSessionTimeUsage(300 * ONE_MINUTE);

    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.SESSION_TIME);

    assertThat(usage.getUsedMillis()).isEqualTo(300 * ONE_MINUTE);
    assertThat(usage.getOpenSessionsCount()).isEqualTo(0);
    verify(activityRepository, never()).findAllById(any(Iterable.class));
  }

  /**
   * Someone else's open session is someone else's problem, and so is one in another context.
   */
  @Test
  public void test_only_the_users_own_sessions_in_the_context_count() {
    withQuotas(sessionTime(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, 480));
    withSessionTimeUsage(300 * ONE_MINUTE);
    RServerSession other = mock(RServerSession.class);
    when(other.getUser()).thenReturn("someone-else");
    when(other.getExecutionContext()).thenReturn(CONTEXT);
    RServerSession otherContext = mock(RServerSession.class);
    when(otherContext.getUser()).thenReturn(USER);
    when(otherContext.getExecutionContext()).thenReturn("R");
    when(rSessionManager.getRSessions()).thenReturn(Lists.newArrayList(other, otherContext));

    RQuotaUsage usage = service.getUsage(CONTEXT, USER, RQuota.Metric.SESSION_TIME);

    assertThat(usage.getUsedMillis()).isEqualTo(300 * ONE_MINUTE);
    assertThat(usage.getOpenSessionsCount()).isEqualTo(0);
  }

  //
  // Helpers
  //

  private void assertWindowStart(RQuotaUsage usage, Date before, Date after, long windowMillis) {
    ArgumentCaptor<Date> from = ArgumentCaptor.forClass(Date.class);
    org.mockito.Mockito.verify(activityRepository).sumExecutionTimeMillis(eq(USER), eq(CONTEXT), from.capture());
    assertThat(from.getValue()).isEqualTo(usage.getWindowStart());
    assertThat(from.getValue().getTime()).isGreaterThanOrEqualTo(before.getTime() - windowMillis);
    assertThat(from.getValue().getTime()).isLessThanOrEqualTo(after.getTime() - windowMillis);
  }

  private void withQuotas(RQuota... quotas) {
    List<RQuota> enabled = Lists.newArrayList(quotas);
    when(quotaRepository.findByContextAndEnabledTrue(CONTEXT)).thenReturn(enabled);
  }

  private void withGroups(String... groups) {
    SubjectProfile profile = SubjectProfile.Builder.create(USER).realm("opal-user-realm").build();
    profile.setGroups(Sets.newHashSet(groups));
    when(subjectProfileService.getProfile(USER)).thenReturn(profile);
  }

  private void withUsage(long usedMillis) {
    when(activityRepository.sumExecutionTimeMillis(eq(USER), eq(CONTEXT), any(Date.class))).thenReturn(usedMillis);
  }

  private void withEarliestActivity(Date earliest) {
    when(activityRepository.findEarliestUpdated(eq(USER), eq(CONTEXT), any(Date.class))).thenReturn(earliest);
  }

  private RQuota system(long limitMinutes) {
    return quota(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, limitMinutes, RQuota.Metric.EXECUTION_TIME);
  }

  private RQuota group(String name, long limitMinutes) {
    return quota(RQuota.SubjectType.GROUP, name, limitMinutes, RQuota.Metric.EXECUTION_TIME);
  }

  private RQuota user(String name, long limitMinutes) {
    return quota(RQuota.SubjectType.USER, name, limitMinutes, RQuota.Metric.EXECUTION_TIME);
  }

  private RQuota sessionTime(RQuota.SubjectType subjectType, String principal, long limitMinutes) {
    return quota(subjectType, principal, limitMinutes, RQuota.Metric.SESSION_TIME);
  }

  private RQuota quota(RQuota.SubjectType subjectType, String principal, long limitMinutes, RQuota.Metric metric) {
    RQuota quota = new RQuota();
    quota.setContext(CONTEXT);
    quota.setSubjectType(subjectType);
    quota.setPrincipal(principal);
    quota.setMetric(metric);
    quota.setPeriod(RQuota.Period.WEEKLY);
    quota.setLimitMillis(limitMinutes * ONE_MINUTE);
    return quota;
  }

  private void withSessionTimeUsage(long usedMillis) {
    when(activityRepository.sumSessionTimeMillis(eq(USER), eq(CONTEXT), any(Date.class))).thenReturn(usedMillis);
  }

  /**
   * Sessions the manager reports as open, with the moment their activity record was last written.
   */
  private void withOpenSessions(Date... lastUpdated) {
    List<RServerSession> sessions = Lists.newArrayList();
    List<RSessionActivity> activities = Lists.newArrayList();
    for (int i = 0; i < lastUpdated.length; i++) {
      String id = "session-" + i;
      RServerSession session = mock(RServerSession.class);
      when(session.getId()).thenReturn(id);
      when(session.getUser()).thenReturn(USER);
      when(session.getExecutionContext()).thenReturn(CONTEXT);
      sessions.add(session);
      RSessionActivity activity = new RSessionActivity();
      activity.setId(id);
      activity.setUser(USER);
      activity.setContext(CONTEXT);
      activity.setUpdated(lastUpdated[i]);
      activities.add(activity);
    }
    when(rSessionManager.getRSessions()).thenReturn(sessions);
    when(activityRepository.findAllById(any(Iterable.class))).thenReturn(activities);
  }
}
