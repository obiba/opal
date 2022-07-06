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

import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RSessionActivity extends AbstractTimestamped implements HasUniqueProperties {

  @NotNull
  @NotBlank
  private String id;

  @NotNull
  @NotBlank
  private String user;

  private String context;

  private String profile;

  private long executionTimeMillis;

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("id");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(id);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

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

  public long getIdleTimeMillis() {
    return getTotalTimeMillis() - executionTimeMillis;
  }
}
