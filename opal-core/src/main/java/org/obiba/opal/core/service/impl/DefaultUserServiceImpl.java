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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

/**
 * Default implementation of User Service
 */
@Component
public class DefaultUserServiceImpl implements UserService {

  public static final String OPAL_DOMAIN = "opal";

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
    List<HasUniqueProperties> toSave = new ArrayList<HasUniqueProperties>();
    toSave.add(user);
    Iterables.addAll(toSave, findImpactedGroups(user));
    orientDbService.save(toSave.toArray(new HasUniqueProperties[toSave.size()]));
  }

  private Iterable<Group> findImpactedGroups(final User user) {

    Collection<Group> groups = new ArrayList<Group>();

    // check removed group
    User previousUser = orientDbService.findUnique(user);
    if(previousUser != null) {
      Iterables.addAll(groups, Iterables.transform(previousUser.getGroups(), new Function<String, Group>() {
        @Nullable
        @Override
        public Group apply(String groupName) {
          if(user.hasGroup(groupName)) return null;
          Group group = getGroup(groupName);
          group.removeUser(user.getName());
          return group;
        }
      }));
    }

    // check added group
    Iterables.addAll(groups, Iterables.transform(user.getGroups(), new Function<String, Group>() {
      @Nullable
      @Override
      public Group apply(String groupName) {
        Group group = getGroup(groupName);
        if(group == null) {
          group = new Group(groupName);
          group.addUser(user.getName());
          return group;
        }
        if(!group.hasUser(user.getName())) {
          group.addUser(user.getName());
          return group;
        }
        return null;
      }
    }));

    return Iterables.filter(groups, Predicates.notNull());
  }

  @Override
  public void deleteUser(final User user) {

    Iterable<Group> groups = Iterables.transform(user.getGroups(), new Function<String, Group>() {
      @Override
      public Group apply(String groupName) {
        Group group = getGroup(groupName);
        group.removeUser(user.getName());
        return group;
      }
    });

    // TODO we should execute these steps in a single transaction
    orientDbService.delete(user);
    orientDbService.save(Iterables.toArray(groups, Group.class));
    aclService.deleteSubjectPermissions(OPAL_DOMAIN, null,
        SubjectAclService.SubjectType.valueOf(SubjectAclService.SubjectType.USER.name())
            .subjectFor(user.getName())); // Delete user's permissions
  }

  @Override
  public void createGroup(String name) throws ConstraintViolationException {
    orientDbService.save(new Group(name));
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
  public void deleteGroup(final Group group) {
    Iterable<User> users = Iterables.transform(group.getUsers(), new Function<String, User>() {
      @Override
      public User apply(String userName) {
        User user = getUser(userName);
        user.removeGroup(group.getName());
        return user;
      }
    });

    // TODO we should execute these steps in a single transaction
    orientDbService.delete(group);
    orientDbService.save(Iterables.toArray(users, User.class));
    aclService.deleteSubjectPermissions(OPAL_DOMAIN, null,
        SubjectAclService.SubjectType.valueOf(SubjectAclService.SubjectType.GROUP.name())
            .subjectFor(group.getName())); // Delete group's permissions
  }

}
