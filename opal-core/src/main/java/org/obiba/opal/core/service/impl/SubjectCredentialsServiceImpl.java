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
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.SubjectCredentials;
import org.obiba.opal.core.runtime.security.OpalUserRealm;
import org.obiba.opal.core.service.DuplicateSubjectProfileException;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.SubjectCredentialsService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

@Component
public class SubjectCredentialsServiceImpl implements SubjectCredentialsService {

  private static final String OPAL_DOMAIN = "opal";

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(SubjectCredentials.class);
    orientDbService.createUniqueIndex(Group.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public Iterable<SubjectCredentials> getSubjectCredentials() {
    return orientDbService.list(SubjectCredentials.class);
  }

  @Override
  public long countGroups() {
    return orientDbService.count(Group.class);
  }

  @Override
  public SubjectCredentials getSubjectCredentials(String name) {
    return orientDbService.findUnique(new SubjectCredentials(name));
  }

  @Override
  public void save(SubjectCredentials subjectCredentials) throws ConstraintViolationException {
    boolean newUser = getSubjectCredentials(subjectCredentials.getName()) == null;
    if(newUser) {
      SubjectProfile profile = subjectProfileService.getProfile(subjectCredentials.getName());
      if(profile != null && !OpalUserRealm.OPAL_REALM.equals(profile.getRealm())) {
        throw new DuplicateSubjectProfileException(profile);
      }
    }

    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    // Copy current password if password is empty
    if(subjectCredentials.getPassword() == null) {
      subjectCredentials.setPassword(getSubjectCredentials(subjectCredentials.getName()).getPassword());
    }

    toSave.put(subjectCredentials, subjectCredentials);
    for(Group group : findImpactedGroups(subjectCredentials)) {
      toSave.put(group, group);
    }
    orientDbService.save(toSave);

    if(newUser) {
      subjectProfileService.ensureProfile(subjectCredentials.getName(), OpalUserRealm.OPAL_REALM);
    }
  }

  private Iterable<Group> findImpactedGroups(final SubjectCredentials subjectCredentials) {

    Collection<Group> groups = new ArrayList<Group>();

    // check removed group
    SubjectCredentials previousSubjectCredentials = orientDbService.findUnique(subjectCredentials);
    if(previousSubjectCredentials != null) {
      Iterables
          .addAll(groups, Iterables.transform(previousSubjectCredentials.getGroups(), new Function<String, Group>() {
            @Nullable
            @Override
            public Group apply(String groupName) {
              if(subjectCredentials.hasGroup(groupName)) return null;
              Group group = getGroup(groupName);
              group.removeUser(subjectCredentials.getName());
              return group;
            }
          }));
    }

    // check added group
    Iterables.addAll(groups, Iterables.transform(subjectCredentials.getGroups(), new Function<String, Group>() {
      @Nullable
      @Override
      public Group apply(String groupName) {
        Group group = getGroup(groupName);
        if(group == null) {
          group = new Group(groupName);
          group.addUser(subjectCredentials.getName());
          return group;
        }
        if(!group.hasUser(subjectCredentials.getName())) {
          group.addUser(subjectCredentials.getName());
          return group;
        }
        return null;
      }
    }));

    return Iterables.filter(groups, Predicates.notNull());
  }

  @Override
  public void delete(SubjectCredentials subjectCredentials) {

    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    for(String groupName : subjectCredentials.getGroups()) {
      Group group = getGroup(groupName);
      group.removeUser(subjectCredentials.getName());
      toSave.put(group, group);
    }
    // TODO we should execute these steps in a single transaction
    orientDbService.delete(subjectCredentials);
    if(!toSave.isEmpty()) orientDbService.save(toSave);
    subjectAclService.deleteSubjectPermissions(OPAL_DOMAIN, null, SubjectAclService.SubjectType.USER
        .subjectFor(subjectCredentials.getName())); // Delete subjectCredentials's permissions
    subjectProfileService.deleteProfile(subjectCredentials.getName());
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
      SubjectCredentials subjectCredentials = getSubjectCredentials(userName);
      subjectCredentials.removeGroup(group.getName());
      toSave.put(subjectCredentials, subjectCredentials);
    }

    // TODO we should execute these steps in a single transaction
    orientDbService.delete(group);
    if(!toSave.isEmpty()) orientDbService.save(toSave);
    subjectAclService.deleteSubjectPermissions(OPAL_DOMAIN, null,
        SubjectAclService.SubjectType.valueOf(SubjectAclService.SubjectType.GROUP.name())
            .subjectFor(group.getName())); // Delete group's permissions
  }

}
