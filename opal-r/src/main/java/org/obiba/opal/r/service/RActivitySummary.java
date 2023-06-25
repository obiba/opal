/*
 * Copyright (c) 2023 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

public class RActivitySummary extends RActivity {

  private int sessionsCount = 0;

  public void setSessionsCount(int sessionsCount) {
    this.sessionsCount = sessionsCount;
  }

  public int getSessionsCount() {
    return sessionsCount;
  }
}
