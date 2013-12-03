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

import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;

public interface UserService extends SystemService {

  /**
   * Returns the list of users
   *
   * @return a list of user instances
   */
  Iterable<User> getUsers();

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
  void delete(User user);

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

  long countGroups();
}
