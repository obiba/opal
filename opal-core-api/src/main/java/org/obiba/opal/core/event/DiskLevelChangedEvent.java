/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.event;

import org.obiba.opal.core.service.storage.DiskLevel;

/**
 * Posted when the free space on the volumes Opal writes to crosses a threshold, in either direction. A transition and
 * not a state: a full disk stays full, and a subscriber that acted on the state would act again every minute.
 */
public class DiskLevelChangedEvent {

  private final DiskLevel previousLevel;

  private final DiskLevel level;

  private final String detail;

  public DiskLevelChangedEvent(DiskLevel previousLevel, DiskLevel level, String detail) {
    this.previousLevel = previousLevel;
    this.level = level;
    this.detail = detail;
  }

  public DiskLevel getPreviousLevel() {
    return previousLevel;
  }

  public DiskLevel getLevel() {
    return level;
  }

  /**
   * The volume that decided the level, as a readable line for a log or a message.
   */
  public String getDetail() {
    return detail;
  }
}
