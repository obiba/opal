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

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of User Service
 */
@Transactional
public abstract class DefaultUserServiceImpl extends PersistenceManagerAwareService implements UserService {

  @Override
  public User getUserWithName(String name) {
    User template = new User();
    template.setName(name);
    return getPersistenceManager().matchOne(template);
  }

  @Override
  public void updateEnabled(User user, boolean enabled) {
    user.setEnabled(enabled);
    getPersistenceManager().save(user);
  }

  @Override
  public void updatePassword(User user, String password) {
    user.setPassword(password);
    getPersistenceManager().save(user);
  }

  @Override
  public void createOrUpdateUser(User user) {
    getPersistenceManager().save(user);
  }

  @Override
  public void deleteUser(User user) {
    getPersistenceManager().delete(user);
  }

  @Override
  public Group createGroup(Group group) {
    return getPersistenceManager().save(group);
  }

  @Override
  public List<Group> getGroups() {
    return getPersistenceManager().list(Group.class);
  }

  @Override
  public Group getGroupWithName(String name) {
    Group template = new Group();
    template.setName(name);
    return getPersistenceManager().matchOne(template);
  }

  @Override
  public void deleteGroup(Group group) {
    getPersistenceManager().delete(group);
  }
}
