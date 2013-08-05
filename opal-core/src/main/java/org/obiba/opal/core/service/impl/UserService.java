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

import java.util.List;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;

@SuppressWarnings("UnusedDeclaration")
public interface UserService {

  /**
   * Returns the list of users
   *
   * @return a list of user instances
   */
  List<User> getUsers();

  /**
   * Returns the count of users that match the specified template
   *
   * @return the number of users that match the template
   */
  int getUserCount();

  /**
   * Returns the user with the specified login
   *
   * @param login the unique login to match
   * @return the user with the specified login or null if none exist
   */
  User getUserWithName(String name);

  /**
   * Change the status of the specified user
   *
   * @param user
   * @param enabled
   */
  void updateEnabled(User user, boolean enabled);

  /**
   * Update the password of the specified user
   *
   * @param template
   * @param password
   */
  void updatePassword(User user, String password);

  /**
   * Create a user when id is not provided, otherwise, updates the changed fields
   *
   * @param user
   */
  void createOrUpdateUser(User user);

  /**
   * Deletes a user from user table and from subject_acl
   *
   * @param user
   */
  void deleteUser(User user);

  /**
   * Create the given group.
   *
   * @param role
   * @return
   */
  Group createGroup(Group group);

  /**
   * Returns the list of groups
   *
   * @param clauses
   * @return
   */
  List<Group> getGroups();

  /**
   * Returns the group with the specified name
   *
   * @param name
   * @return the group with the specified name or null if none exist
   */
  Group getGroupWithName(String name);

  /**
   * Deletes a group from group table and from subject_acl
   *
   * @param group
   */
  void deleteGroup(Group group);
}
