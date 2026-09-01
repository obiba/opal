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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
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
   * Execution time a user has accumulated in a context since {@code from}.
   * <p>
   * A session is counted whole, at the instant of its last command: the record carries one cumulated total and not a
   * time series. That over-counts a session that was already running when the window opened and never under-counts,
   * which is the direction a quota check should err in. Backed by
   * {@code idx_r_session_activities_user_context_updated}, because this runs on the session creation path.
   */
  @Query("select coalesce(sum(a.executionTimeMillis), 0) from RSessionActivity a " +
      "where a.user = :user and a.context = :context and a.updated >= :from")
  long sumExecutionTimeMillis(@Param("user") String user, @Param("context") String context, @Param("from") Date from);

  /**
   * The last activity of the oldest session still inside the window, i.e. the next one to leave it.
   */
  @Query("select min(a.updated) from RSessionActivity a " +
      "where a.user = :user and a.context = :context and a.updated >= :from")
  Date findEarliestUpdated(@Param("user") String user, @Param("context") String context, @Param("from") Date from);

  /**
   * The activity is stored under the R session's own identifier, so it is already the key.
   */
  default RSessionActivity upsert(RSessionActivity activity) {
    return save(activity);
  }
}
