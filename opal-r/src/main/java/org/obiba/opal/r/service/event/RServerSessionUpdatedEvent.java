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

public class RServerSessionUpdatedEvent implements RServerSessionEvent {

  private final String id;

  private final String user;

  private final long executionTimeMillis;

  public RServerSessionUpdatedEvent(String id, String user, long executionTimeMillis) {
    this.id = id;
    this.user = user;
    this.executionTimeMillis = executionTimeMillis;
  }

  public String getId() {
    return id;
  }

  @Override
  public String getUser() {
    return user;
  }

  public long getExecutionTimeMillis() {
    return executionTimeMillis;
  }
}
