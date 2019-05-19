/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.service.security.realm.BackgroundJobRealm;
import org.obiba.shiro.realm.SudoRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubjectProfileServiceImpl implements SubjectProfileService {

  private static final Logger log = LoggerFactory.getLogger(SubjectProfileServiceImpl.class);

  static final String FILES_SHARE_PERM = "FILES_SHARE";

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(SubjectProfile.class);
  }

  @Override
  public void stop() {}

  @Override
  public boolean supportProfile(@Nullable Object principal) {
    return principal != null && //
        !principal.equals(BackgroundJobRealm.SystemPrincipal.INSTANCE) && //
        !principal.equals(SudoRealm.SudoPrincipal.INSTANCE);
  }

  @Override
  public void ensureProfile(@NotNull String principal, @NotNull String realm) {
    log.debug("ensure profile of user {} from realm: {}", principal, realm);

    try {
      SubjectProfile profile = getProfile(principal);
      if(!profile.getRealm().equals(realm)) {
        throw new AuthenticationException(
            "Wrong realm for subject '" + principal + "': " + realm + " (" + profile.getRealm() +
                " expected). Make sure the same subject is not defined in several realms."
        );
      }
    } catch(SubjectProfileNotFoundException e) {
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
  public void deleteProfile(@NotNull String principal) {
    try {
      orientDbService.delete(getProfile(principal));
    } catch(SubjectProfileNotFoundException ignored) {
      // ignore
    }
  }

  @NotNull
  @Override
  public SubjectProfile getProfile(@Nullable String principal) throws SubjectProfileNotFoundException {
    if(principal == null) throw new SubjectProfileNotFoundException(principal);
    SubjectProfile subjectProfile = orientDbService.findUnique(SubjectProfile.Builder.create(principal).build());
    if(subjectProfile == null) {
      throw new SubjectProfileNotFoundException(principal);
    }
    return subjectProfile;
  }

  @Override
  public void updateProfile(@NotNull String principal) throws SubjectProfileNotFoundException {
    SubjectProfile profile = getProfile(principal);
    profile.setUpdated(new Date());
    orientDbService.save(profile, profile);
  }

  @Override
  public Iterable<SubjectProfile> getProfiles() {
    return orientDbService.list(SubjectProfile.class);
  }

  @Override
  public void addBookmarks(String principal, List<String> resources) throws SubjectProfileNotFoundException {
    SubjectProfile profile = getProfile(principal);
    for(String resource : resources) {
      profile.addBookmark(resource);
    }
    orientDbService.save(profile, profile);
  }

  @Override
  public void deleteBookmark(String principal, String path) throws SubjectProfileNotFoundException {
    SubjectProfile profile = getProfile(principal);
    if(profile.hasBookmark(path) && profile.removeBookmark(path)) {
      orientDbService.save(profile, profile);
    }
  }

  @Override
  public void deleteBookmarks(String path) throws SubjectProfileNotFoundException{
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

  private void ensureUserHomeExists(String username) {
    try {
      if(!opalRuntime.hasFileSystem()) return;
      FileObject home = opalRuntime.getFileSystem().getRoot().resolveFile("/home/" + username);
      if(!home.exists()) {
        log.info("Creating user home: /home/{}", username);
        home.createFolder();
      }
    } catch(FileSystemException e) {
      log.error("Failed creating user home.", e);
    }
  }

  private void ensureFolderPermissions(String username, String path) {
    String folderNode = "/files" + path;
    SubjectAclService.Permissions acl = subjectAclService
        .getSubjectNodePermissions("opal", folderNode, new SubjectAcl.Subject(username, SubjectAcl.SubjectType.USER));
    if(!findPermission(acl, FILES_SHARE_PERM)) {
      subjectAclService
          .addSubjectPermission("opal", folderNode, SubjectAcl.SubjectType.USER.subjectFor(username), FILES_SHARE_PERM);
    }
  }

  private boolean findPermission(SubjectAclService.Permissions acl, String permission) {
    boolean found = false;
    for(String perm : acl.getPermissions()) {
      if(perm.equals(permission)) {
        found = true;
        break;
      }
    }
    return found;
  }
}
