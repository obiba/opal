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
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Default implementation of User Service
 */
@Component
public class UserServiceImpl implements UserService {

  private static final String OPAL_DOMAIN = "opal";

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
  public Iterable<User> getUsers() {
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
    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    // Copy current password if password is empty
    if(user.getPassword() == null) {
      user.setPassword(getUser(user.getName()).getPassword());
    }

    toSave.put(user, user);
    for(Group group : findImpactedGroups(user)) {
      toSave.put(group, group);
    }
    orientDbService.save(toSave);
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
  public void delete(User user) {

    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    for(String groupName : user.getGroups()) {
      Group group = getGroup(groupName);
      group.removeUser(user.getName());
      toSave.put(group, group);
    }
    // TODO we should execute these steps in a single transaction
    orientDbService.delete(user);
    if(!toSave.isEmpty()) orientDbService.save(toSave);
    aclService.deleteSubjectPermissions(OPAL_DOMAIN, null,
        SubjectAclService.SubjectType.valueOf(SubjectAclService.SubjectType.USER.name())
            .subjectFor(user.getName())); // Delete user's permissions
  }

  @Override
  public void createGroup(String name) throws ConstraintViolationException {
    orientDbService.save(null, new Group(name));
  }

  @Override
  public Iterable<Group> getGroups() {
    return orientDbService.list(Group.class);
  }

  @Override
  public Group getGroup(String name) {
    return orientDbService.findUnique(new Group(name));
  }

  @Override
  public void delete(Group group) {
    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    for(String userName : group.getUsers()) {
      User user = getUser(userName);
      user.removeGroup(group.getName());
      toSave.put(user, user);
    }

    // TODO we should execute these steps in a single transaction
    orientDbService.delete(group);
    if(!toSave.isEmpty()) orientDbService.save(toSave);
    aclService.deleteSubjectPermissions(OPAL_DOMAIN, null,
        SubjectAclService.SubjectType.valueOf(SubjectAclService.SubjectType.GROUP.name())
            .subjectFor(group.getName())); // Delete group's permissions
  }

}
