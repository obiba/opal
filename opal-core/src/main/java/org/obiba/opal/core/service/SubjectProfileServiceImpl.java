/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.service.event.SubjectProfileDeletedEvent;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.service.security.TotpService;
import org.obiba.opal.core.service.security.event.SubjectCredentialsDeletedEvent;
import org.obiba.opal.core.service.security.realm.BackgroundJobRealm;
import org.obiba.shiro.realm.SudoRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SubjectProfileServiceImpl implements SubjectProfileService {

  private static final Logger log = LoggerFactory.getLogger(SubjectProfileServiceImpl.class);

  static final String FILES_SHARE_PERM = "FILES_SHARE";

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private EventBus eventBus;

  @Autowired
  private TotpService totpService;

  @Value("#{new Boolean('${org.obiba.opal.security.multiProfile}')}")
  private boolean multiProfile;

  @Override
  public void start() {
    orientDbService.createUniqueIndex(SubjectProfile.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean supportProfile(@Nullable Object principal) {
    return principal != null && //
        !principal.equals(BackgroundJobRealm.SystemPrincipal.INSTANCE) && //
        !principal.equals(SudoRealm.SudoPrincipal.INSTANCE);
  }

  @Override
  public synchronized void ensureProfile(@NotNull String principal, @NotNull String realm) {
    log.debug("ensure profile of user {} from realm: {}", principal, realm);

    try {
      SubjectProfile profile = getProfile(principal);
      if (isMultipleRealms()) {
        List<String> realms = Splitter.on(",").splitToList(profile.getRealm());
        if (!realms.contains(realm)) {
          List<String> newRealms = Lists.newArrayList(realms);
          newRealms.add(realm);
          profile.setRealm(Joiner.on(",").join(newRealms));
        }
      } else if (!profile.getRealm().equals(realm)) {
        throw new AuthenticationException(
            "Wrong realm for subject '" + principal + "': " + realm + " (" + profile.getRealm() +
                " expected). Make sure the same subject is not defined in several realms."
        );
      }
      profile.setUpdated(new Date());
      orientDbService.save(profile, profile);
    } catch (NoSuchSubjectProfileException e) {
      HasUniqueProperties newProfile = new SubjectProfile(principal, realm);
      orientDbService.save(newProfile, newProfile);
    }
  }

  @Override
  public void ensureProfile(@NotNull PrincipalCollection principalCollection) {
    String principal = principalCollection.getPrimaryPrincipal().toString();
    String realm = principalCollection.getRealmNames().iterator().next();
    ensureProfile(principal, realm);
    ensureUserHomeExists(principal);
    ensureFolderPermissions(principal, "/home/" + principal);
    ensureFolderPermissions(principal, "/tmp");
  }

  @Override
  public synchronized void applyProfileGroups(@NotNull String principal, Set<String> groups) {
    try {
      SubjectProfile profile = getProfile(principal);
      profile.setGroups(groups);
      profile.setUpdated(new Date());
      orientDbService.save(profile, profile);
    } catch (NoSuchSubjectProfileException e) {
      // ignore
    }
  }

  @Override
  public void deleteProfile(@NotNull String principal) {
    try {
      SubjectProfile profile = getProfile(principal);
      orientDbService.delete(profile);
      eventBus.post(new SubjectProfileDeletedEvent(profile));
    } catch (NoSuchSubjectProfileException ignored) {
      // ignore
    }
  }

  @NotNull
  @Override
  public SubjectProfile getProfile(@Nullable String principal) throws NoSuchSubjectProfileException {
    if (principal == null) throw new NoSuchSubjectProfileException(principal);
    SubjectProfile subjectProfile = orientDbService.findUnique(SubjectProfile.Builder.create(principal).build());
    if (subjectProfile == null) {
      throw new NoSuchSubjectProfileException(principal);
    }
    return subjectProfile;
  }

  @Override
  public synchronized void updateProfile(@NotNull String principal) throws NoSuchSubjectProfileException {
    SubjectProfile profile = getProfile(principal);
    profile.setUpdated(new Date());
    orientDbService.save(profile, profile);
  }

  @Override
  public void updateProfileSecret(String principal, boolean enable) {
    SubjectProfile profile = getProfile(principal);
    profile.setSecret(enable ? (profile.hasTmpSecret() ? profile.getTmpSecret() : totpService.generateSecret()) : null);
    orientDbService.save(profile, profile);
  }

  @Override
  public void updateProfileTmpSecret(String principal, boolean enable) {
    SubjectProfile profile = getProfile(principal);
    profile.setTmpSecret(enable ? totpService.generateSecret() : null);
    orientDbService.save(profile, profile);
  }

  @Override
  public Iterable<SubjectProfile> getProfiles() {
    return orientDbService.list(SubjectProfile.class);
  }

  @Override
  public synchronized void addBookmarks(String principal, List<String> resources) throws NoSuchSubjectProfileException {
    SubjectProfile profile = getProfile(principal);
    for (String resource : resources) {
      profile.addBookmark(resource);
    }
    orientDbService.save(profile, profile);
  }

  @Override
  public synchronized void deleteBookmark(String principal, String path) throws NoSuchSubjectProfileException {
    SubjectProfile profile = getProfile(principal);
    if (profile.hasBookmark(path) && profile.removeBookmark(path)) {
      orientDbService.save(profile, profile);
    }
  }

  @Override
  public synchronized void deleteBookmarks(String path) throws NoSuchSubjectProfileException {
    for (SubjectProfile profile : orientDbService.list(SubjectProfile.class)) {
      if (!profile.hasBookmarks()) return;
      List<Bookmark> toRemove = profile.getBookmarks().stream()
          .filter(b -> b.getResource().equals(path) || b.getResource().startsWith(path + "/"))
          .collect(Collectors.toList());
      if (!toRemove.isEmpty()) {
        toRemove.forEach(profile::removeBookmark);
        orientDbService.save(profile, profile);
      }
    }
  }

  @Subscribe
  public void onSubjectCredentialsDeleted(SubjectCredentialsDeletedEvent event) {
    deleteProfile(event.getCredentials().getName());
  }

  //
  // Private methods
  //

  private boolean isMultipleRealms() {
    return multiProfile;
  }

  private void ensureUserHomeExists(String username) {
    try {
      if (!opalFileSystemService.hasFileSystem()) return;
      FileObject home = opalFileSystemService.getFileSystem().getRoot().resolveFile("/home/" + username);
      if (!home.exists()) {
        log.info("Creating user home: /home/{}", username);
        home.createFolder();
      }
    } catch (FileSystemException e) {
      log.error("Failed creating user home.", e);
    }
  }

  private void ensureFolderPermissions(String username, String path) {
    String folderNode = "/files" + path;
    SubjectAclService.Permissions acl = subjectAclService
        .getSubjectNodePermissions("opal", folderNode, new SubjectAcl.Subject(username, SubjectAcl.SubjectType.USER));
    if (!findPermission(acl, FILES_SHARE_PERM)) {
      subjectAclService
          .addSubjectPermission("opal", folderNode, SubjectAcl.SubjectType.USER.subjectFor(username), FILES_SHARE_PERM);
    }
  }

  private boolean findPermission(SubjectAclService.Permissions acl, String permission) {
    boolean found = false;
    for (String perm : acl.getPermissions()) {
      if (perm.equals(permission)) {
        found = true;
        break;
      }
    }
    return found;
  }
}
