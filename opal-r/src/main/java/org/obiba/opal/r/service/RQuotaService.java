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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the R execution time quota that applies to a user, and measures what they have spent against it.
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

  @Autowired
  public RQuotaService(RQuotaRepository rQuotaRepository, RSessionActivityRepository rSessionActivityRepository,
                       SubjectProfileService subjectProfileService) {
    this.rQuotaRepository = rQuotaRepository;
    this.rSessionActivityRepository = rSessionActivityRepository;
    this.subjectProfileService = subjectProfileService;
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
    quota.setPeriod(values.getPeriod());
    quota.setExecutionTimeLimitMillis(values.getExecutionTimeLimitMillis());
    quota.setEnabled(values.isEnabled());
    return rQuotaRepository.save(quota);
  }

  public void deleteQuota(long id) {
    rQuotaRepository.findById(id).ifPresent(rQuotaRepository::delete);
  }

  private void validate(RQuota quota) {
    if (Strings.isNullOrEmpty(quota.getContext())) throw new IllegalArgumentException("R quota context is missing");
    if (quota.getExecutionTimeLimitMillis() < 0)
      throw new IllegalArgumentException("R quota execution time limit cannot be negative");
    if (RQuota.SubjectType.SYSTEM.equals(quota.getSubjectType()))
      quota.setPrincipal(RQuota.SYSTEM_PRINCIPAL);
    else if (Strings.isNullOrEmpty(quota.getPrincipal()))
      throw new IllegalArgumentException("R quota subject name is missing");
  }

  //
  // Resolution and usage
  //

  /**
   * The quota that applies to a user: their own if they have one, else the most permissive of the ones given to the
   * groups they belong to, else the system default. A disabled quota is not a quota, so the search falls through it;
   * and when nothing matches there is no quota at all, which means unlimited rather than zero.
   */
  public Optional<RQuota> resolve(String context, String principal) {
    List<RQuota> quotas = rQuotaRepository.findByContextAndEnabledTrue(context);
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
        .max(Comparator.comparingLong(RQuota::getExecutionTimeLimitMillis));
    if (group.isPresent()) return group;

    return quotas.stream()
        .filter(quota -> RQuota.SubjectType.SYSTEM.equals(quota.getSubjectType()))
        .findFirst();
  }

  public RQuotaUsage getUsage(String context, String principal) {
    Optional<RQuota> resolved = resolve(context, principal);
    if (resolved.isEmpty()) return RQuotaUsage.unlimited(context, principal);

    RQuota quota = resolved.get();
    Date windowStart = quota.getWindowStart(new Date());
    long used = rSessionActivityRepository.sumExecutionTimeMillis(principal, context, windowStart);
    Date nextCredit = null;
    if (used >= quota.getExecutionTimeLimitMillis()) {
      // Only worth a second query when someone is actually waiting for the window to move.
      Date earliest = rSessionActivityRepository.findEarliestUpdated(principal, context, windowStart);
      if (earliest != null) nextCredit = new Date(earliest.getTime() + quota.getPeriod().getDurationMillis());
    }
    return RQuotaUsage.of(context, principal, quota, used, windowStart, nextCredit);
  }

  public boolean isExceeded(String context, String principal) {
    return getUsage(context, principal).isExceeded();
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
