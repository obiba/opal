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

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;

import jakarta.validation.constraints.NotNull;

@MappedSuperclass
public class RActivity extends AbstractTimestamped {

  @NotNull
  @NotBlank
  @Column(name = "user_name")
  private String user;

  private String context;

  private String profile;

  @Column(name = "execution_time_millis", nullable = false)
  private long executionTimeMillis = 0;

  /**
   * The wall-clock life of the session, idle time included: what a session time quota is measured against. Stored
   * rather than derived from the timestamps, because summing it over a window has to be an indexed aggregate over a
   * numeric column and timestamp arithmetic inside an aggregate is dialect-specific.
   */
  @Column(name = "session_time_millis", nullable = false)
  private long sessionTimeMillis = 0;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public long getExecutionTimeMillis() {
    return executionTimeMillis;
  }

  public void setExecutionTimeMillis(long executionTimeMillis) {
    this.executionTimeMillis = executionTimeMillis;
  }

  public long getSessionTimeMillis() {
    return sessionTimeMillis;
  }

  public void setSessionTimeMillis(long sessionTimeMillis) {
    this.sessionTimeMillis = sessionTimeMillis;
  }

}
