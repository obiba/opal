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
import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    orientDbService.createUniqueIndex(User.class);
    orientDbService.createUniqueIndex(Group.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public Iterable<User> listUsers() {
    return orientDbService.list(User.class);
  }

  @Override
  public long countUsers() {
    return orientDbService.count(User.class);
  }

  @Override
  public long countGroups() {
    return orientDbService.count(Group.class);
  }

  @Override
  public User getUser(String name) {
    return orientDbService.findUnique(new User(name));
  }

  @Override
  public void save(User user) throws ConstraintViolationException {
    orientDbService.save(user);
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
  public void save(Group group) throws ConstraintViolationException {
    orientDbService.save(group);
  }

  @Override
  public Iterable<Group> listGroups() {
    return orientDbService.list(Group.class);
  }

  @Override
  public Group getGroup(String name) {
    return orientDbService.findUnique(new Group(name));
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
