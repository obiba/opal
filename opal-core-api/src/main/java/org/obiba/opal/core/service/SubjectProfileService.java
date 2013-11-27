/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.apache.shiro.subject.Subject;

/**
 * Service to handle the profile of subjects.
 */
public interface SubjectProfileService extends SystemService {

  /**
   * Add or check profile of the subject: check will fail if a subject from a different realm has already a profile entry.
   *
   * @param subject
   */
  void ensureProfile(Subject subject);

  /**
   * Delete profile.
   *
   * @param name
   */
  void deleteProfile(String name);

}
