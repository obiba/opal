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

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;

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
    orientDbService.createUniqueStringIndex(User.class, "name");
    orientDbService.createUniqueStringIndex(Group.class, "name");
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
  public void createOrUpdateUser(User user) throws UserAlreadyExistsException {
    try {
      orientDbService.save(user);
    } catch(ORecordDuplicatedException e) {
      throw new UserAlreadyExistsException(user.getName());
    }
  }

  @Override
  public void deleteUser(User user) {
    SubjectAclService.Subject aclSubject = SubjectAclService.SubjectType
        .valueOf(SubjectAclService.SubjectType.USER.name()).subjectFor(user.getName());

    orientDbService.delete(user);

    // Delete user's permissions
    aclService.deleteSubjectPermissions("opal", null, aclSubject);
  }

  @Override
  public void createOrUpdateGroup(Group group) throws GroupAlreadyExistsException {
    try {
      orientDbService.save(group);
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
  public void deleteGroup(Group group) {
    SubjectAclService.Subject aclSubject = SubjectAclService.SubjectType
        .valueOf(SubjectAclService.SubjectType.GROUP.name()).subjectFor(group.getName());

    orientDbService.delete(group);

    // Delete group's permissions
    aclService.deleteSubjectPermissions("opal", null, aclSubject);

  }

}
