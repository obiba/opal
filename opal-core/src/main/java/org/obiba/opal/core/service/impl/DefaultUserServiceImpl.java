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

import javax.annotation.PostConstruct;

import org.obiba.opal.core.cfg.OrientDbService;
import org.obiba.opal.core.cfg.OrientDbTransactionCallback;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * Default implementation of User Service
 */
@Component
public class DefaultUserServiceImpl implements UserService {

  @Autowired
  private SubjectAclService aclService;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.registerEntityClass(User.class, Group.class);
    orientDbService.createUniqueIndex(User.class, "name", OType.STRING);
    orientDbService.createUniqueIndex(Group.class, "name", OType.STRING);
  }

  @Override
  public void stop() {

  }

  @Override
  public Iterable<User> list() {
    return orientDbService.list(User.class);
  }

  @Override
  public long getUserCount() {
    return orientDbService.count(User.class);
  }

  @Override
  public long getGroupCount() {
    return orientDbService.count(Group.class);
  }

  @Override
  public User getUserWithName(String name) {
    return orientDbService.uniqueResult("select from User where name = ?", name);
  }

  @Override
  public void createOrUpdateUser(final User user) throws UserAlreadyExistsException {

    //TODO bean validation

    try {
      orientDbService.execute(new OrientDbTransactionCallback<Object>() {
        @Override
        public Object doInTransaction(OObjectDatabaseTx db) {
          return db.save(user);
        }
      });
    } catch(ORecordDuplicatedException e) {
      throw new UserAlreadyExistsException(user.getName());
    }
  }

  @Override
  public void deleteUser(final User user) {
    SubjectAclService.Subject aclSubject = SubjectAclService.SubjectType
        .valueOf(SubjectAclService.SubjectType.USER.name()).subjectFor(user.getName());

    orientDbService.execute(new OrientDbTransactionCallback<Object>() {
      @Override
      public Object doInTransaction(OObjectDatabaseTx db) {
        return db.delete(user);
      }
    });

    // Delete user's permissions
    aclService.deleteSubjectPermissions("opal", null, aclSubject);
  }

  @Override
  public void createOrUpdateGroup(final Group group) throws GroupAlreadyExistsException {
    //TODO bean validation
    try {
      orientDbService.execute(new OrientDbTransactionCallback<Object>() {
        @Override
        public Object doInTransaction(OObjectDatabaseTx db) {
          return db.save(group);
        }
      });
    } catch(Exception e) {
      throw new GroupAlreadyExistsException(group.getName());
    }
  }

  @Override
  public Iterable<Group> getGroups() {
    return orientDbService.list(Group.class);
  }

  @Override
  public Group getGroupWithName(String name) {
    return orientDbService.uniqueResult("select from Group where name = ?", name);
  }

  @Override
  public void deleteGroup(final Group group) {
    SubjectAclService.Subject aclSubject = SubjectAclService.SubjectType
        .valueOf(SubjectAclService.SubjectType.GROUP.name()).subjectFor(group.getName());

    orientDbService.execute(new OrientDbTransactionCallback<Object>() {
      @Override
      public Object doInTransaction(OObjectDatabaseTx db) {
        return db.delete(group);
      }
    });

    // Delete group's permissions
    aclService.deleteSubjectPermissions("opal", null, aclSubject);

  }

}
