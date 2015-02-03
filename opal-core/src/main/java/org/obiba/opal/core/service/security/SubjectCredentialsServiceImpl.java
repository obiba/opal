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
import org.obiba.opal.core.service.SubjectProfileNotFoundException;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.realm.ApplicationRealm;
import org.obiba.opal.core.service.security.realm.OpalUserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;
import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType.USER;

@Component
public class SubjectCredentialsServiceImpl implements SubjectCredentialsService {

  private static final String OPAL_DOMAIN = "opal";

  private static final int MINIMUM_LEMGTH = 6;

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
  public SubjectCredentials getSubjectCredentialsByCertificateAlias(String certificateAlias) {
    return orientDbService.uniqueResult(SubjectCredentials.class,
        "select from " + SubjectCredentials.class.getSimpleName() + " where certificateAlias = ?", certificateAlias);
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
    } else {
      validateAuthenticationType(subjectCredentials, existing);
    }
    persist(subjectCredentials, ensureCredentials(subjectCredentials, existing));
    if(newSubject) {
      ensureProfile(subjectCredentials);
    }
  }

  @Override
  public void changePassword(String principal, String oldPassword, String newPassword)
      throws PasswordException, SubjectPrincipalNotFoundException {
    SubjectCredentials subjectCredentials = getSubjectCredentials(principal);

    if(subjectCredentials == null) {
      throw new SubjectPrincipalNotFoundException(principal);
    }

    String currentPassword = subjectCredentials.getPassword();

    if(!currentPassword.equals(hashPassword(oldPassword))) {
      throw new OldPasswordMismatchException();
    }

    if(newPassword.length() < MINIMUM_LEMGTH) {
      throw new PasswordTooShortException(MINIMUM_LEMGTH);
    }

    if(oldPassword.equals(newPassword)) {
      throw new PasswordNotChangedException();
    }

    subjectCredentials.setPassword(hashPassword(newPassword));
    save(subjectCredentials);
  }

  /**
   * Re-apply the credentials to the provided subject if it already exists.
   *
   * @param subjectCredentials
   * @param existing
   * @return
   */

  private OpalKeyStore ensureCredentials(SubjectCredentials subjectCredentials, SubjectCredentials existing) {
    boolean newSubject = existing == null;
    OpalKeyStore keyStore = null;
    switch(subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        // Copy current password if password is empty for existing user
        if(subjectCredentials.getPassword() == null && !newSubject) {
          subjectCredentials.setPassword(existing.getPassword());
        }
        break;
      case CERTIFICATE:
        // OPAL-2688
        if(newSubject) {
          subjectCredentials.setCertificateAlias(subjectCredentials.generateCertificateAlias());
        } else {
          subjectCredentials.setCertificateAlias(existing.getCertificateAlias());
        }
        if(subjectCredentials.getCertificate() != null) {
          keyStore = credentialsKeyStoreService.getKeyStore();
          keyStore.importCertificate(subjectCredentials.getCertificateAlias(),
              new ByteArrayInputStream(subjectCredentials.getCertificate()));
        }
        break;
    }
    return keyStore;
  }

  /**
   * Persist subject and related groups.
   *
   * @param subjectCredentials
   * @param keyStore
   */
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

  /**
   * Ensure subject has a profile.
   *
   * @param subjectCredentials
   */
  private void ensureProfile(SubjectCredentials subjectCredentials) {
    subjectProfileService
        .ensureProfile(subjectCredentials.getName(), getRealmFromType(subjectCredentials.getAuthenticationType()));
  }

  private String getRealmFromType(SubjectCredentials.AuthenticationType type) {
    switch(type) {
      case PASSWORD:
        return OpalUserRealm.OPAL_REALM;
      case CERTIFICATE:
        return ApplicationRealm.APPLICATION_REALM;
    }
    return "";
  }

  /**
   * Ensure that the subject does not conflict with another one in a different realm.
   *
   * @param subjectCredentials
   */
  private void validateProfile(SubjectCredentials subjectCredentials) {
    String realm = getRealmFromType(subjectCredentials.getAuthenticationType());
    try {
      SubjectProfile profile = subjectProfileService.getProfile(subjectCredentials.getName());
      if(!realm.equals(profile.getRealm())) {
        throw new DuplicateSubjectProfileException(profile);
      }
    } catch(SubjectProfileNotFoundException ignored) {
      // do nothing as this principal has no profile
    }
  }

  private void validateAuthenticationType(SubjectCredentials subjectCredentials, SubjectCredentials existing) {
    if(existing.getAuthenticationType() != subjectCredentials.getAuthenticationType()) {
      throw new IllegalArgumentException("Authentication type cannot be changed");
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
    subjectAclService.deleteSubjectPermissions(USER.subjectFor(subjectCredentials.getName()));
    subjectProfileService.deleteProfile(subjectCredentials.getName());

    if(subjectCredentials.getAuthenticationType() == SubjectCredentials.AuthenticationType.CERTIFICATE) {
      OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
      String alias = subjectCredentials.getCertificateAlias();
      // OPAL-2688
      if(!Strings.isNullOrEmpty(alias)) {
        keyStore.deleteKey(alias);
        credentialsKeyStoreService.saveKeyStore(keyStore);
      }
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
    subjectAclService
        .deleteSubjectPermissions(SubjectType.valueOf(SubjectType.GROUP.name()).subjectFor(group.getName()));
  }

}
