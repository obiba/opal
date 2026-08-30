/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.security;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.repository.GroupRepository;
import org.obiba.opal.core.repository.SubjectCredentialsRepository;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.DuplicateSubjectProfileException;
import org.obiba.opal.core.service.NoSuchSubjectProfileException;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.event.GroupDeletedEvent;
import org.obiba.opal.core.service.security.event.SubjectCredentialsDeletedEvent;
import org.obiba.opal.core.service.security.realm.OpalApplicationRealm;
import org.obiba.opal.core.service.security.realm.OpalUserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.regex.Pattern;

@Component
public class SubjectCredentialsServiceImpl implements SubjectCredentialsService {

  private static final int PWD_MINIMUM_LENGTH = 8;

  private static final int PWD_MAXIMUM_LENGTH = 64;

  static final Pattern PWD_PATTERN = Pattern.compile(
      "^(?=.*[0-9])"       // a digit must occur at least once
          + "(?=.*[a-z])"      // a lower case alphabet must occur at least once
          + "(?=.*[A-Z])"      // a upper case alphabet must occur at least once
          + "(?=.*[@#$%^&+=!])" // a special character that must occur at least once
          + "(?=\\S+$).{" + PWD_MINIMUM_LENGTH + "," + PWD_MAXIMUM_LENGTH + "}$");

  /**
   * Number of times the user password is hashed for attack resiliency
   */
  @Value("${org.obiba.opal.security.password.nbHashIterations}")
  private int nbHashIterations;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private SubjectCredentialsRepository subjectCredentialsRepository;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  @Autowired
  private EventBus eventBus;

  @Override
  public void start() {

  }

  @Override
  public void stop() {
  }

  @Override
  public Iterable<SubjectCredentials> getSubjectCredentials() {
    return subjectCredentialsRepository.findAll();
  }

  @Override
  public SubjectCredentials getSubjectCredentialsByCertificateAlias(String certificateAlias) {
    return subjectCredentialsRepository.findByCertificateAlias(certificateAlias).orElse(null);
  }

  @Override
  public Iterable<SubjectCredentials> getSubjectCredentials(SubjectCredentials.AuthenticationType authenticationType) {
    return subjectCredentialsRepository.findByAuthenticationType(authenticationType);
  }

  @Override
  public SubjectCredentials getSubjectCredentials(String name) {
    return subjectCredentialsRepository.findByName(name).orElse(null);
  }

  @Override
  public String hashPassword(String password) {
    return hashPassword(password, true);
  }

  @Override
  public void save(SubjectCredentials subjectCredentials)
      throws ConstraintViolationException, DuplicateSubjectProfileException {

    SubjectCredentials existing = getSubjectCredentials(subjectCredentials.getName());
    boolean newSubject = existing == null;
    if (newSubject) {
      validateProfile(subjectCredentials);
    } else {
      validateAuthenticationType(subjectCredentials, existing);
    }
    persist(subjectCredentials, ensureCredentials(subjectCredentials, existing));
    if (newSubject) {
      ensureProfile(subjectCredentials);
    }
  }

  @Override
  public void changePassword(String principal, String oldPassword, String newPassword)
      throws PasswordException, SubjectPrincipalNotFoundException {
    SubjectCredentials subjectCredentials = getSubjectCredentials(principal);

    if (subjectCredentials == null) throw new SubjectPrincipalNotFoundException(principal);

    String currentPassword = subjectCredentials.getPassword();
    if (!currentPassword.equals(hashPassword(oldPassword, false))) throw new OldPasswordMismatchException();
    if (oldPassword.equals(newPassword)) throw new PasswordNotChangedException();

    subjectCredentials.setPassword(hashPassword(newPassword));
    save(subjectCredentials);
  }

  private String hashPassword(String password, boolean validate) {
    if (validate)
      validatePassword(password);
    return new Sha512Hash(password, opalConfigurationService.getOpalConfiguration().getSecretKey(), nbHashIterations)
        .toString();
  }

  private void validatePassword(String newPassword) {
    if (newPassword.length() < PWD_MINIMUM_LENGTH) throw new PasswordTooShortException(PWD_MINIMUM_LENGTH);
    if (newPassword.length() > PWD_MAXIMUM_LENGTH) throw new PasswordTooLongException(PWD_MAXIMUM_LENGTH);
    if (!PWD_PATTERN.matcher(newPassword).matches()) throw new PasswordTooWeakException();
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
    switch (subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        // Copy current password if password is empty for existing user
        if (subjectCredentials.getPassword() == null && !newSubject) {
          subjectCredentials.setPassword(existing.getPassword());
        }
        break;
      case CERTIFICATE:
        // OPAL-2688
        if (newSubject) {
          subjectCredentials.setCertificateAlias(subjectCredentials.generateCertificateAlias());
        } else {
          subjectCredentials.setCertificateAlias(existing.getCertificateAlias());
        }
        if (subjectCredentials.getCertificate() != null) {
          keyStore = credentialsKeyStoreService.getKeyStore();
          try {
            keyStore.importCertificate(subjectCredentials.getCertificateAlias(), new ByteArrayInputStream(subjectCredentials.getCertificate()));
          } catch (Exception e) {
            throw new CertificateException(e);
          }
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
    // The impacted groups are worked out from the credentials as they are currently stored, so they have to be
    // collected before the new state replaces them.
    List<Group> impactedGroups = Lists.newArrayList(findImpactedGroups(subjectCredentials));
    subjectCredentialsRepository.upsert(subjectCredentials);
    impactedGroups.forEach(groupRepository::upsert);

    if (keyStore != null) {
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
    switch (type) {
      case PASSWORD:
        return OpalUserRealm.OPAL_REALM;
      case CERTIFICATE:
        return OpalApplicationRealm.APPLICATION_REALM;
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
      if (!realm.equals(profile.getRealm())) {
        throw new DuplicateSubjectProfileException(profile);
      }
    } catch (NoSuchSubjectProfileException ignored) {
      // do nothing as this principal has no profile
    }
  }

  private void validateAuthenticationType(SubjectCredentials subjectCredentials, SubjectCredentials existing) {
    if (existing.getAuthenticationType() != subjectCredentials.getAuthenticationType()) {
      throw new IllegalArgumentException("Authentication type cannot be changed");
    }
  }

  private Iterable<Group> findImpactedGroups(final SubjectCredentials subjectCredentials) {

    Collection<Group> groups = new ArrayList<>();

    // check removed group
    SubjectCredentials previousSubjectCredentials = getSubjectCredentials(subjectCredentials.getName());
    if (previousSubjectCredentials != null) {
      Iterables
          .addAll(groups, Iterables.transform(previousSubjectCredentials.getGroups(), new Function<String, Group>() {
            @Nullable
            @Override
            public Group apply(String groupName) {
              if (subjectCredentials.hasGroup(groupName)) return null;
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
        if (groupName.isEmpty()) return null;

        Group group = getGroup(groupName);
        if (group == null) {
          group = new Group(groupName);
          group.addSubjectCredential(subjectCredentials.getName());
          return group;
        }
        if (!group.hasSubjectCredential(subjectCredentials.getName())) {
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

    List<Group> toSave = Lists.newArrayList();
    for (String groupName : subjectCredentials.getGroups()) {
      Group group = getGroup(groupName);
      group.removeSubjectCredential(subjectCredentials.getName());
      toSave.add(group);
    }
    subjectCredentialsRepository.deleteByKey(subjectCredentials);
    toSave.forEach(groupRepository::upsert);
    eventBus.post(new SubjectCredentialsDeletedEvent(subjectCredentials));

    if (subjectCredentials.getAuthenticationType() == SubjectCredentials.AuthenticationType.CERTIFICATE) {
      OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
      String alias = subjectCredentials.getCertificateAlias();
      // OPAL-2688
      if (!Strings.isNullOrEmpty(alias)) {
        keyStore.deleteKey(alias);
        credentialsKeyStoreService.saveKeyStore(keyStore);
      }
    }
  }

  @Override
  public void createGroup(String name) throws ConstraintViolationException {
    groupRepository.upsert(new Group(name));
  }

  @Override
  public Iterable<Group> getGroups() {
    return groupRepository.findAll();
  }

  @Override
  public Group getGroup(String name) {
    return groupRepository.findByName(name).orElse(null);
  }

  @Override
  public void delete(Group group) {
    List<SubjectCredentials> toSave = Lists.newArrayList();
    for (String userName : group.getSubjectCredentials()) {
      SubjectCredentials subjectCredentials = getSubjectCredentials(userName);
      subjectCredentials.removeGroup(group.getName());
      toSave.add(subjectCredentials);
    }

    // TODO we should execute these steps in a single transaction
    groupRepository.deleteByKey(group);
    toSave.forEach(subjectCredentialsRepository::upsert);
    // Delete group's permissions
    eventBus.post(new GroupDeletedEvent(group));
  }

}
