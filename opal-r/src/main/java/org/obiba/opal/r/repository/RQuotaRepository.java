/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.repository;

import org.obiba.opal.core.repository.EntityKeys;
import org.obiba.opal.r.service.RQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * The R usage quotas. A handful of rows at most: one system default per context and metric, plus the group and user
 * overrides an administrator has bothered to write.
 */
public interface RQuotaRepository extends JpaRepository<RQuota, Long> {

  List<RQuota> findByContext(String context);

  List<RQuota> findByContextAndEnabledTrue(String context);

  Optional<RQuota> findByContextAndSubjectTypeAndPrincipalAndMetric(String context, RQuota.SubjectType subjectType,
                                                                    String principal, RQuota.Metric metric);

  /**
   * The natural key is the context, the subject the quota is about and the metric it limits, so saving a second quota
   * for the same subject and metric replaces the first rather than adding a row the unique constraint would reject
   * anyway. The metric belongs in the key: giving a subject a session time quota must not overwrite the execution time
   * quota they already have.
   */
  default RQuota upsert(RQuota quota) {
    RQuota existing = findByContextAndSubjectTypeAndPrincipalAndMetric(quota.getContext(), quota.getSubjectType(),
        quota.getPrincipal(), quota.getMetric()).orElse(null);
    return save(EntityKeys.reuseKey(existing, quota));
  }
}
