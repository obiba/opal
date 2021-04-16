/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.security.SubjectProfile;

/**
 * Thrown when attempting to add a user or a unit and a subject profile with same name already exists.
 */
public class DuplicateSubjectProfileException extends RuntimeException {

  private final SubjectProfile existing;

  public DuplicateSubjectProfileException(SubjectProfile existing) {
    super("A subject with name '" + existing.getPrincipal() + "' already exists in a different realm: " +
        existing.getRealm());
    this.existing = existing;
  }

  public SubjectProfile getExisting() {
    return existing;
  }
}
