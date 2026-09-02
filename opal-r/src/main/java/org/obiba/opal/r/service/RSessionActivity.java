/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

/**
 * An activity log rather than a piece of configuration, and by far the largest of these tables on a busy server.
 */
@Entity
@Table(name = "r_session_activities",
    indexes = {
        @Index(name = "idx_r_session_activities_user", columnList = "user_name"),
        @Index(name = "idx_r_session_activities_context", columnList = "context"),
        @Index(name = "idx_r_session_activities_profile", columnList = "profile")
    })
public class RSessionActivity extends RActivity {

  /**
   * The R session's own identifier, assigned when the session is created.
   */
  @Id
  @NotNull
  @NotBlank
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Clamped, because rows written before session time was recorded keep a session time of zero while their execution
   * time is whatever it was: the subtraction is meaningless there, and a negative idle time is worse than none.
   */
  public long getIdleTimeMillis() {
    return Math.max(0, getSessionTimeMillis() - getExecutionTimeMillis());
  }
}
