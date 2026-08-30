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

import org.obiba.opal.r.service.RSessionActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * The R activity log: the largest of the configuration tables on a busy server, and the only one that grows without an
 * administrator doing anything.
 */
public interface RSessionActivityRepository extends JpaRepository<RSessionActivity, String> {

  List<RSessionActivity> findByContext(String context);

  List<RSessionActivity> findByContextAndProfile(String context, String profile);

  List<RSessionActivity> findByContextAndUser(String context, String user);

  List<RSessionActivity> findByContextAndUserAndProfile(String context, String user, String profile);

  /**
   * The activity is stored under the R session's own identifier, so it is already the key.
   */
  default RSessionActivity upsert(RSessionActivity activity) {
    return save(activity);
  }
}
