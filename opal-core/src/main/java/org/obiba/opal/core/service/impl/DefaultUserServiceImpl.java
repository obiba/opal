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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of User Service
 */
@Transactional
public class DefaultUserServiceImpl extends PersistenceManagerAwareService implements UserService {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getCurrentSession() {
    return factory.getCurrentSession();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterable<User> list() {
    return getCurrentSession().createCriteria(User.class).list();
  }

  @Override
  public int getUserCount() {
    return 0;
  }

  @Override
  public User getUserWithName(String name) {
    return (User) getCurrentSession().createCriteria(User.class).add(Restrictions.eq("name", name)).uniqueResult();
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
    factory.getCurrentSession().saveOrUpdate(user);
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
    return (Group) getCurrentSession().createCriteria(Group.class).add(Restrictions.eq("name", name)).uniqueResult();
  }

  @Override
  public void deleteGroup(Group group) {
    getPersistenceManager().delete(group);
  }
}
