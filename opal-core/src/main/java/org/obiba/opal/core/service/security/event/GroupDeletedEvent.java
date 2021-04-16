/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.event;

import org.obiba.opal.core.domain.security.Group;

public class GroupDeletedEvent {

  private final Group group;

  public GroupDeletedEvent(Group group) {
    this.group = group;
  }

  public Group getGroup() {
    return group;
  }
}
