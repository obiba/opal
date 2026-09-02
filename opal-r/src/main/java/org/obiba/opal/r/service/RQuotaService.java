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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.r.repository.RQuotaRepository;
import org.obiba.opal.r.repository.RSessionActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the R usage quotas that apply to a user, one per {@link RQuota.Metric}, and measures what they have spent
 * against each.
 * <p>
 * The quota model is context-generic - DataSHIELD is only its first consumer - which is why this sits next to
 * {@link RActivityService}, whose records it reads, rather than in the DataSHIELD module.
 */
@Component
public class RQuotaService implements SystemService {

  private static final Logger log = LoggerFactory.getLogger(RQuotaService.class);

  private final RQuotaRepository rQuotaRepository;

  private final RSessionActivityRepository rSessionActivityRepository;

  private final SubjectProfileService subjectProfileService;

  private final OpalRSessionManager opalRSessionManager;

  @Autowired
  public RQuotaService(RQuotaRepository rQuotaRepository, RSessionActivityRepository rSessionActivityRepository,
                       SubjectProfileService subjectProfileService, OpalRSessionManager opalRSessionManager) {
    this.rQuotaRepository = rQuotaRepository;
    this.rSessionActivityRepository = rSessionActivityRepository;
    this.subjectProfileService = subjectProfileService;
    this.opalRSessionManager = opalRSessionManager;
  }

  //
  // Configuration
  //

  public List<RQuota> getQuotas(String context) {
    return Strings.isNullOrEmpty(context) ? rQuotaRepository.findAll() : rQuotaRepository.findByContext(context);
  }

  public RQuota getQuota(long id) {
    return rQuotaRepository.findById(id).orElseThrow(() -> new NoSuchRQuotaException(id));
  }

  /**
   * Save a quota, replacing the one that subject already had in that context if there is one: the subject and the
   * context are the natural key, whatever surrogate identifier the row happens to carry.
   */
  public RQuota saveQuota(RQuota quota) {
    validate(quota);
    return rQuotaRepository.upsert(quota);
  }

  /**
   * Update the quota with that identifier, subject included. Moving it onto a subject that already has one in the same
   * context is rejected by the unique constraint rather than silently overwriting the other quota, which is what
   * {@link #saveQuota(RQuota)} would do.
   */
  public RQuota updateQuota(long id, RQuota values) {
    validate(values);
    RQuota quota = getQuota(id);
    quota.setContext(values.getContext());
    quota.setSubjectType(values.getSubjectType());
    quota.setPrincipal(values.getPrincipal());
    quota.setMetric(values.getMetric());
    quota.setPeriod(values.getPeriod());
    quota.setLimitMillis(values.getLimitMillis());
    quota.setEnabled(values.isEnabled());
    return rQuotaRepository.save(quota);
  }

  public void deleteQuota(long id) {
    rQuotaRepository.findById(id).ifPresent(rQuotaRepository::delete);
  }

  private void validate(RQuota quota) {
    if (Strings.isNullOrEmpty(quota.getContext())) throw new IllegalArgumentException("R quota context is missing");
    if (quota.getLimitMillis() < 0)
      throw new IllegalArgumentException("R quota limit cannot be negative");
    if (RQuota.SubjectType.SYSTEM.equals(quota.getSubjectType()))
      quota.setPrincipal(RQuota.SYSTEM_PRINCIPAL);
    else if (Strings.isNullOrEmpty(quota.getPrincipal()))
      throw new IllegalArgumentException("R quota subject name is missing");
  }

  //
  // Resolution and usage
  //

  /**
   * The quota that applies to a user for one metric: their own if they have one, else the most permissive of the ones
   * given to the groups they belong to, else the system default. A disabled quota is not a quota, so the search falls
   * through it; and when nothing matches there is no quota at all, which means unlimited rather than zero.
   * <p>
   * Metrics are resolved independently, and limits are only ever compared within one of them: the most permissive of
   * 60 minutes of execution time and 600 minutes of session time is not a question with an answer.
   */
  public Optional<RQuota> resolve(String context, String principal, RQuota.Metric metric) {
    List<RQuota> quotas = rQuotaRepository.findByContextAndEnabledTrue(context).stream()
        .filter(quota -> metric.equals(quota.getMetric()))
        .toList();
    if (quotas.isEmpty()) return Optional.empty();

    Optional<RQuota> personal = quotas.stream()
        .filter(quota -> RQuota.SubjectType.USER.equals(quota.getSubjectType()))
        .filter(quota -> quota.getPrincipal().equals(principal))
        .findFirst();
    if (personal.isPresent()) return personal;

    Set<String> groups = getGroups(principal);
    Optional<RQuota> group = quotas.stream()
        .filter(quota -> RQuota.SubjectType.GROUP.equals(quota.getSubjectType()))
        .filter(quota -> groups.contains(quota.getPrincipal()))
        .max(Comparator.comparingLong(RQuota::getLimitMillis));
    if (group.isPresent()) return group;

    return quotas.stream()
        .filter(quota -> RQuota.SubjectType.SYSTEM.equals(quota.getSubjectType()))
        .findFirst();
  }

  /**
   * What the user has spent for every metric, whether or not a quota applies to it, so that a caller never has to know
   * how many metrics exist to render the answer.
   */
  public List<RQuotaUsage> getUsages(String context, String principal) {
    return Arrays.stream(RQuota.Metric.values())
        .map(metric -> getUsage(context, principal, metric))
        .toList();
  }

  public RQuotaUsage getUsage(String context, String principal, RQuota.Metric metric) {
    Optional<RQuota> resolved = resolve(context, principal, metric);
    if (resolved.isEmpty()) return RQuotaUsage.unlimited(context, principal, metric);

    RQuota quota = resolved.get();
    Date windowStart = quota.getWindowStart(new Date());
    List<String> openSessionIds = getOpenSessionIds(context, principal);
    long used = RQuota.Metric.SESSION_TIME.equals(metric)
        ? rSessionActivityRepository.sumSessionTimeMillis(principal, context, windowStart) + liveSessionTimeMillis(openSessionIds)
        : rSessionActivityRepository.sumExecutionTimeMillis(principal, context, windowStart);
    Date nextCredit = null;
    if (used >= quota.getLimitMillis()) {
      // Only worth a second query when someone is actually waiting for the window to move.
      Date earliest = rSessionActivityRepository.findEarliestUpdated(principal, context, windowStart);
      if (earliest != null) nextCredit = new Date(earliest.getTime() + quota.getPeriod().getDurationMillis());
    }
    return RQuotaUsage.of(context, principal, quota, used, windowStart, nextCredit, openSessionIds.size());
  }

  /**
   * True when any metric is spent: the gate refuses a session on the first bound the user has reached, whichever it is.
   */
  public boolean isExceeded(String context, String principal) {
    return getUsages(context, principal).stream().anyMatch(RQuotaUsage::isExceeded);
  }

  @Override
  @PostConstruct
  public void start() {
  }

  @Override
  public void stop() {
  }

  //
  // Private methods
  //

  /**
   * A record's session time only moves when a command ends or the session is closed, so a session that is open and
   * idle has more of it than the table says. The difference is the time since its record was last written, and adding
   * it here keeps the measure exact at the moment it is read without a heartbeat writing to the activity log every
   * minute. It is also what stops a user from parking sessions and watching their usage stand still.
   */
  private long liveSessionTimeMillis(List<String> openSessionIds) {
    if (openSessionIds.isEmpty()) return 0;
    long now = System.currentTimeMillis();
    return rSessionActivityRepository.findAllById(openSessionIds).stream()
        .mapToLong(activity -> Math.max(0, now - activity.getUpdated().getTime()))
        .sum();
  }

  /**
   * The sessions the user has open in the context, as the session manager knows them. In-process and therefore
   * per-Opal-node, which is the whole truth as long as Opal is not clustered.
   */
  private List<String> getOpenSessionIds(String context, String principal) {
    return opalRSessionManager.getRSessions().stream()
        .filter(session -> principal.equals(session.getUser()) && context.equals(session.getExecutionContext()))
        .map(RServerSession::getId)
        .toList();
  }

  /**
   * The groups observed at the subject's last login. A subject with no profile yet has none, which only makes the
   * resolution fall back to the system default.
   */
  private Set<String> getGroups(String principal) {
    try {
      Set<String> groups = subjectProfileService.getProfile(principal).getGroups();
      return groups == null ? Sets.newHashSet() : groups;
    } catch (Exception e) {
      log.debug("No profile for {}, no group quota can apply", principal);
      return Sets.newHashSet();
    }
  }
}
