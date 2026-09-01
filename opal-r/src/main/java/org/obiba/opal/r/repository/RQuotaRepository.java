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
 * The R execution time quotas. A handful of rows at most: one system default per context, plus the group and user
 * overrides an administrator has bothered to write.
 */
public interface RQuotaRepository extends JpaRepository<RQuota, Long> {

  List<RQuota> findByContext(String context);

  List<RQuota> findByContextAndEnabledTrue(String context);

  Optional<RQuota> findByContextAndSubjectTypeAndPrincipal(String context, RQuota.SubjectType subjectType, String principal);

  /**
   * The natural key is the context and the subject the quota is about, so saving a second quota for the same subject
   * replaces the first rather than adding a row the unique constraint would reject anyway.
   */
  default RQuota upsert(RQuota quota) {
    RQuota existing = findByContextAndSubjectTypeAndPrincipal(quota.getContext(), quota.getSubjectType(), quota.getPrincipal())
        .orElse(null);
    return save(EntityKeys.reuseKey(existing, quota));
  }
}
