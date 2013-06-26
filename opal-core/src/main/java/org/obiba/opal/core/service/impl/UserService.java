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

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.opal.core.user.Group;
import org.obiba.opal.core.user.Status;
import org.obiba.opal.core.user.User;

@SuppressWarnings("UnusedDeclaration")
public interface UserService {

  /**
   * Returns the list of users
   *
   * @param template a template instance for matching users
   * @param paging paging clause, can be null
   * @param clauses sorting clause(s), can be null
   * @return a list of user instances that match the template
   */
  List<User> getUsers(User template, PagingClause paging, SortingClause... clauses);

  /**
   * Returns the count of users that match the specified template
   *
   * @return the number of users that match the template
   */
  int getUserCount(User template);

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
   * @param status
   */
  void updateStatus(User user, Status status);

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
  List<Group> getGroups(SortingClause... clauses);
}
