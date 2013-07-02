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
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.opal.core.user.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of hibernate specific methods of User Service
 *
 * @see#UserService.
 */
@Transactional
public class UserServiceHibernateImpl extends DefaultUserServiceImpl {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  public List<User> getUsers() {
    return getUserCriteria().list();
  }

  public int getUserCount() {
    return getUserCriteria().count();
  }

  private AssociationCriteria getUserCriteria() {
    AssociationCriteria criteria = AssociationCriteria.create(User.class, getSession());

    return criteria;
  }

}
