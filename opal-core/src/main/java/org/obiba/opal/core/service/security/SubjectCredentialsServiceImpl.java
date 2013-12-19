/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.security;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolationException;

import org.apache.shiro.crypto.hash.Sha512Hash;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.DuplicateSubjectProfileException;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.realm.OpalUserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;
import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType.USER;

@Component
public class SubjectCredentialsServiceImpl implements SubjectCredentialsService {

  private static final String OPAL_DOMAIN = "opal";

  /**
   * Number of times the user password is hashed for attack resiliency
   */
  @Value("${org.obiba.opal.security.password.nbHashIterations}")
  private int nbHashIterations;

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

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
  public Iterable<SubjectCredentials> getSubjectCredentials(SubjectCredentials.AuthenticationType authenticationType) {
    return orientDbService.list(SubjectCredentials.class,
        "select from " + SubjectCredentials.class.getSimpleName() + " where authenticationType = ?",
        authenticationType);
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
  public String hashPassword(String password) {
    return new Sha512Hash(password, opalConfigurationService.getOpalConfiguration().getSecretKey(), nbHashIterations)
        .toString();
  }

  @Override
  public void save(SubjectCredentials subjectCredentials)
      throws ConstraintViolationException, DuplicateSubjectProfileException {

    SubjectCredentials existing = getSubjectCredentials(subjectCredentials.getName());
    boolean newSubject = existing == null;
    if(newSubject) {
      validateProfile(subjectCredentials);
    }

    OpalKeyStore keyStore = null;
    switch(subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        // Copy current password if password is empty for existing user
        if(subjectCredentials.getPassword() == null && !newSubject) {
          subjectCredentials.setPassword(existing.getPassword());
        }
        break;
      case CERTIFICATE:
        if(subjectCredentials.getCertificate() != null) {
          keyStore = credentialsKeyStoreService.getKeyStore();
          keyStore.importCertificate(subjectCredentials.getName(),
              new ByteArrayInputStream(subjectCredentials.getCertificate()));
        }
        break;
    }

    persist(subjectCredentials, keyStore);

    if(newSubject) {
      subjectProfileService.ensureProfile(subjectCredentials.getName(), OpalUserRealm.OPAL_REALM);
    }
  }

  private void persist(SubjectCredentials subjectCredentials, @Nullable OpalKeyStore keyStore) {
    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    toSave.put(subjectCredentials, subjectCredentials);
    for(Group group : findImpactedGroups(subjectCredentials)) {
      toSave.put(group, group);
    }
    orientDbService.save(toSave);

    if(keyStore != null) {
      credentialsKeyStoreService.saveKeyStore(keyStore);
    }
  }

  private void validateProfile(SubjectCredentials subjectCredentials) {
    SubjectProfile profile = subjectProfileService.getProfile(subjectCredentials.getName());
    if(profile != null && !OpalUserRealm.OPAL_REALM.equals(profile.getRealm())) {
      throw new DuplicateSubjectProfileException(profile);
    }
  }

  private Iterable<Group> findImpactedGroups(final SubjectCredentials subjectCredentials) {

    Collection<Group> groups = new ArrayList<>();

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
              group.removeSubjectCredential(subjectCredentials.getName());
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
          group.addSubjectCredential(subjectCredentials.getName());
          return group;
        }
        if(!group.hasSubjectCredential(subjectCredentials.getName())) {
          group.addSubjectCredential(subjectCredentials.getName());
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
      group.removeSubjectCredential(subjectCredentials.getName());
      toSave.put(group, group);
    }
    // TODO we should execute these steps in a single transaction
    orientDbService.delete(subjectCredentials);
    if(!toSave.isEmpty()) orientDbService.save(toSave);
    // Delete subjectCredentials's permissions
    subjectAclService
        .deleteSubjectPermissions(OPAL_DOMAIN, null, USER.subjectFor(subjectCredentials.getName()));
    subjectProfileService.deleteProfile(subjectCredentials.getName());

    if(subjectCredentials.getAuthenticationType() == SubjectCredentials.AuthenticationType.CERTIFICATE) {
      OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
      keyStore.deleteKey(subjectCredentials.getName());
      credentialsKeyStoreService.saveKeyStore(keyStore);
    }
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
    for(String userName : group.getSubjectCredentials()) {
      SubjectCredentials subjectCredentials = getSubjectCredentials(userName);
      subjectCredentials.removeGroup(group.getName());
      toSave.put(subjectCredentials, subjectCredentials);
    }

    // TODO we should execute these steps in a single transaction
    orientDbService.delete(group);
    if(!toSave.isEmpty()) orientDbService.save(toSave);
    // Delete group's permissions
    subjectAclService.deleteSubjectPermissions(OPAL_DOMAIN, null,
        SubjectType.valueOf(SubjectType.GROUP.name()).subjectFor(group.getName()));
  }

}
