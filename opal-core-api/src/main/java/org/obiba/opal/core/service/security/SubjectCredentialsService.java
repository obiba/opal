/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.security;

import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.DuplicateSubjectProfileException;
import org.obiba.opal.core.service.SystemService;

import jakarta.validation.ConstraintViolationException;

public interface SubjectCredentialsService extends SystemService {

  /**
   * Get all the credentials.
   *
   * @return
   */
  Iterable<SubjectCredentials> getSubjectCredentials();

  /**
   * Given a certificate alias, returns the corresponding SubjectCredentials
   *
   * @param certificateAlias the certificate alias
   * @return returns the corresponding a SubjectCredentials
   */
  SubjectCredentials getSubjectCredentialsByCertificateAlias(String certificateAlias);

  /**
   * Returns the list of users
   *
   * @return a list of subjectCredentials instances
   */
  Iterable<SubjectCredentials> getSubjectCredentials(SubjectCredentials.AuthenticationType authenticationType);

  /**
   * Returns the subjectCredentials with the specified login
   *
   * @param name the unique login to match
   * @return the subjectCredentials with the specified login or null if none exist
   */
  SubjectCredentials getSubjectCredentials(String name);

  /**
   * Validate and hash a password.
   *
   * @param password
   * @return
   */
  String hashPassword(String password);

  /**
   * Create a subjectCredentials when id is not provided, otherwise, updates the changed fields
   *
   * @param subjectCredentials
   */
  void save(SubjectCredentials subjectCredentials)
      throws ConstraintViolationException, DuplicateSubjectProfileException;

  /**
   * Changes the principal's current password
   *
   * @param principal   the subject principal
   * @param oldPassword the current password
   * @param newPassword the new password
   */
  void changePassword(String principal, String oldPassword, String newPassword)
      throws PasswordException, SubjectPrincipalNotFoundException;

  /**
   * Deletes a subjectCredentials from subjectCredentials table and from subject_acl
   *
   * @param subjectCredentials
   */
  void delete(SubjectCredentials subjectCredentials);

  /**
   * Create the given group.
   *
   * @return
   */
  void createGroup(String name) throws ConstraintViolationException;

  /**
   * Returns the list of groups
   *
   * @return
   */
  Iterable<Group> getGroups();

  /**
   * Returns the group with the specified name
   *
   * @param name
   * @return the group with the specified name or null if none exist
   */
  Group getGroup(String name);

  /**
   * Deletes a group from group table and from subject_acl
   *
   * @param group
   */
  void delete(Group group);

}
