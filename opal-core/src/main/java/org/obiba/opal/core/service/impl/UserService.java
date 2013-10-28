/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.impl;

import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.SystemService;

public interface UserService extends SystemService {

  /**
   * Returns the list of users
   *
   * @return a list of user instances
   */
  Iterable<User> listUsers();

  /**
   * Returns the count of users that match the specified template
   *
   * @return the number of users that match the template
   */
  long countUsers();

  /**
   * Returns the user with the specified login
   *
   * @param login the unique login to match
   * @return the user with the specified login or null if none exist
   */
  User getUser(String name);

  /**
   * Create a user when id is not provided, otherwise, updates the changed fields
   *
   * @param user
   */
  void save(User user) throws ConstraintViolationException;

  /**
   * Deletes a user from user table and from subject_acl
   *
   * @param user
   */
  void deleteUser(User user);

  /**
   * Create the given group.
   *
   * @return
   */
  void save(Group group) throws ConstraintViolationException;

  /**
   * Returns the list of groups
   *
   * @return
   */
  Iterable<Group> listGroups();

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
  void deleteGroup(Group group);

  long countGroups();
}
