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

import javax.validation.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;

import javax.validation.constraints.NotNull;

public class RActivity extends AbstractTimestamped {

  @NotNull
  @NotBlank
  private String user;

  private String context;

  private String profile;

  private long executionTimeMillis = 0;

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

  public long getTotalTimeMillis() {
    return getUpdated().getTime() - getCreated().getTime();
  }

}
