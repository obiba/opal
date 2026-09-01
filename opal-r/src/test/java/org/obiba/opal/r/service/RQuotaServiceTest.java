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
import static org.mockito.Mockito.when;

/**
 * The two things the quota service decides on its own: which quota applies to a user, and what they have spent
 * against it over its rolling window.
 */
public class RQuotaServiceTest {

  private static final String CONTEXT = "DataSHIELD";

  private static final String USER = "jsmith";

  private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);

  private RQuotaRepository quotaRepository;

  private RSessionActivityRepository activityRepository;

  private SubjectProfileService subjectProfileService;

  private RQuotaService service;

  @Before
  public void setUp() {
    quotaRepository = mock(RQuotaRepository.class);
    activityRepository = mock(RSessionActivityRepository.class);
    subjectProfileService = mock(SubjectProfileService.class);
    service = new RQuotaService(quotaRepository, activityRepository, subjectProfileService);
    withGroups();
  }

  //
  // Resolution: user > group (most permissive) > system, and absence means unlimited
  //

  @Test
  public void test_nothing_configured_resolves_to_no_quota() {
    withQuotas();

    assertThat(service.resolve(CONTEXT, USER)).isEqualTo(Optional.empty());
  }

  @Test
  public void test_the_system_default_applies_when_nothing_else_does() {
    withQuotas(system(60));

    assertThat(service.resolve(CONTEXT, USER).get().getExecutionTimeLimitMillis()).isEqualTo(60 * ONE_MINUTE);
  }

  @Test
  public void test_a_group_quota_beats_the_system_default() {
    withGroups("analysts");
    withQuotas(system(60), group("analysts", 120));

    assertThat(service.resolve(CONTEXT, USER).get().getExecutionTimeLimitMillis()).isEqualTo(120 * ONE_MINUTE);
  }

  @Test
  public void test_the_most_permissive_group_quota_wins() {
    withGroups("analysts", "partners");
    withQuotas(system(60), group("analysts", 120), group("partners", 300));

    assertThat(service.resolve(CONTEXT, USER).get().getExecutionTimeLimitMillis()).isEqualTo(300 * ONE_MINUTE);
  }

  @Test
  public void test_a_group_the_user_is_not_in_is_ignored() {
    withGroups("analysts");
    withQuotas(system(60), group("analysts", 120), group("partners", 300));

    assertThat(service.resolve(CONTEXT, USER).get().getExecutionTimeLimitMillis()).isEqualTo(120 * ONE_MINUTE);
  }

  @Test
  public void test_a_personal_quota_beats_every_group() {
    withGroups("analysts", "partners");
    withQuotas(system(60), group("analysts", 120), group("partners", 300), user(USER, 90));

    assertThat(service.resolve(CONTEXT, USER).get().getExecutionTimeLimitMillis()).isEqualTo(90 * ONE_MINUTE);
  }

  @Test
  public void test_a_personal_quota_of_somebody_else_is_ignored() {
    withQuotas(system(60), user("someone-else", 90));

    assertThat(service.resolve(CONTEXT, USER).get().getExecutionTimeLimitMillis()).isEqualTo(60 * ONE_MINUTE);
  }

  /**
   * A disabled quota is invisible to the resolution, not an exemption granted to its subject: the search has to fall
   * through it to the next level. The service expresses that by only ever asking for the enabled ones, which is what
   * this pins - the filtering itself is the repository's, and is covered by the schema it queries.
   */
  @Test
  public void test_only_enabled_quotas_take_part_in_the_resolution() {
    withQuotas(system(60));

    service.resolve(CONTEXT, USER);

    org.mockito.Mockito.verify(quotaRepository).findByContextAndEnabledTrue(CONTEXT);
    org.mockito.Mockito.verify(quotaRepository, org.mockito.Mockito.never()).findByContext(anyString());
  }

  @Test
  public void test_a_user_without_a_profile_can_still_get_the_system_default() {
    when(subjectProfileService.getProfile(anyString())).thenThrow(new NoSuchSubjectProfileException(USER));
    withQuotas(system(60), group("analysts", 120));

    assertThat(service.resolve(CONTEXT, USER).get().getExecutionTimeLimitMillis()).isEqualTo(60 * ONE_MINUTE);
  }

  //
  // Usage over the rolling window
  //

  @Test
  public void test_no_quota_means_nothing_is_measured() {
    withQuotas();

    RQuotaUsage usage = service.getUsage(CONTEXT, USER);

    assertThat(usage.hasQuota()).isFalse();
    assertThat(usage.isExceeded()).isFalse();
    assertThat(usage.getWindowStart()).isNull();
  }

  @Test
  public void test_a_weekly_window_starts_seven_days_back() {
    withQuotas(system(120));
    withUsage(30 * ONE_MINUTE);

    Date before = new Date();
    RQuotaUsage usage = service.getUsage(CONTEXT, USER);
    Date after = new Date();

    assertThat(usage.getWindowStart()).isNotNull();
    assertWindowStart(usage, before, after, TimeUnit.DAYS.toMillis(7));
    assertThat(usage.getUsedExecutionTimeMillis()).isEqualTo(30 * ONE_MINUTE);
    assertThat(usage.isExceeded()).isFalse();
  }

  @Test
  public void test_a_daily_window_starts_twenty_four_hours_back() {
    RQuota quota = system(120);
    quota.setPeriod(RQuota.Period.DAILY);
    withQuotas(quota);
    withUsage(30 * ONE_MINUTE);

    Date before = new Date();
    RQuotaUsage usage = service.getUsage(CONTEXT, USER);
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

    assertThat(service.getUsage(CONTEXT, USER).isExceeded()).isTrue();
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

    RQuotaUsage usage = service.getUsage(CONTEXT, USER);

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

    assertThat(service.getUsage(CONTEXT, USER).getNextCreditDate()).isNull();
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
    return quota(RQuota.SubjectType.SYSTEM, RQuota.SYSTEM_PRINCIPAL, limitMinutes);
  }

  private RQuota group(String name, long limitMinutes) {
    return quota(RQuota.SubjectType.GROUP, name, limitMinutes);
  }

  private RQuota user(String name, long limitMinutes) {
    return quota(RQuota.SubjectType.USER, name, limitMinutes);
  }

  private RQuota quota(RQuota.SubjectType subjectType, String principal, long limitMinutes) {
    RQuota quota = new RQuota();
    quota.setContext(CONTEXT);
    quota.setSubjectType(subjectType);
    quota.setPrincipal(principal);
    quota.setPeriod(RQuota.Period.WEEKLY);
    quota.setExecutionTimeLimitMillis(limitMinutes * ONE_MINUTE);
    return quota;
  }
}
