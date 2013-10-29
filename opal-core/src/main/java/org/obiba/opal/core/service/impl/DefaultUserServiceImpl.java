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
import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of User Service
 */
@Component
public class DefaultUserServiceImpl implements UserService {

  public static final String UNIQUE_INDEX = "name";

  @Autowired
  private SubjectAclService aclService;

  @Autowired
  private OrientDbDocumentService orientDbDocumentService;

  @Override
  @PostConstruct
  public void start() {
    orientDbDocumentService.createUniqueStringIndex(User.class, UNIQUE_INDEX);
    orientDbDocumentService.createUniqueStringIndex(Group.class, UNIQUE_INDEX);
  }

  @Override
  public void stop() {
  }

  @Override
  public Iterable<User> listUsers() {
    return orientDbDocumentService.list(User.class);
  }

  @Override
  public long countUsers() {
    return orientDbDocumentService.count(User.class);
  }

  @Override
  public long countGroups() {
    return orientDbDocumentService.count(Group.class);
  }

  @Override
  public User getUser(String name) {
    return orientDbDocumentService.findUnique(User.class, UNIQUE_INDEX, name);
  }

  @Override
  public void save(User user) throws ConstraintViolationException {
    orientDbDocumentService.save(user, UNIQUE_INDEX);
  }

  @Override
  public void deleteUser(User user) {
    SubjectAclService.Subject aclSubject = SubjectAclService.SubjectType
        .valueOf(SubjectAclService.SubjectType.USER.name()).subjectFor(user.getName());

    orientDbDocumentService.deleteUnique(User.class, UNIQUE_INDEX, user.getName());

    // Delete user's permissions
    aclService.deleteSubjectPermissions("opal", null, aclSubject);
  }

  @Override
  public void save(Group group) throws ConstraintViolationException {
    orientDbDocumentService.save(group, UNIQUE_INDEX);
  }

  @Override
  public Iterable<Group> listGroups() {
    return orientDbDocumentService.list(Group.class);
  }

  @Override
  public Group getGroup(String name) {
    return orientDbDocumentService.findUnique(Group.class, UNIQUE_INDEX, name);
  }

  @Override
  public void deleteGroup(Group group) {
    SubjectAclService.Subject aclSubject = SubjectAclService.SubjectType
        .valueOf(SubjectAclService.SubjectType.GROUP.name()).subjectFor(group.getName());

    orientDbDocumentService.deleteUnique(Group.class, UNIQUE_INDEX, group.getName());

    // Delete group's permissions
    aclService.deleteSubjectPermissions("opal", null, aclSubject);
  }

}
