/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service.event;

import java.util.Date;

public class RServerSessionStartedEvent implements RServerSessionEvent {
  private final String id;

  private final String user;

  private final String executionContext;

  private final String profile;

  private final Date created;

  public RServerSessionStartedEvent(String id, String user, String executionContext, String profile, Date created) {
    this.id = id;
    this.user = user;
    this.executionContext = executionContext;
    this.profile = profile;
    this.created = created;
  }

  public String getId() {
    return id;
  }

  @Override
  public String getUser() {
    return user;
  }

  public String getExecutionContext() {
    return executionContext;
  }

  public String getProfile() {
    return profile;
  }

  public Date getCreated() {
    return created;
  }
}
