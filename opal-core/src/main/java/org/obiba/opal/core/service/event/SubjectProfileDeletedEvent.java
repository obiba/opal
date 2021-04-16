/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.event;

import org.obiba.opal.core.domain.security.SubjectProfile;

public class SubjectProfileDeletedEvent {

  private final SubjectProfile profile;

  public SubjectProfileDeletedEvent(SubjectProfile profile) {
    this.profile = profile;
  }

  public SubjectProfile getProfile() {
    return profile;
  }
}
