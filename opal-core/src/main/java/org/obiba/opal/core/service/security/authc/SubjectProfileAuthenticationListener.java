/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.authc;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.service.security.realm.BackgroundJobRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubjectProfileAuthenticationListener implements AuthenticationListener {

  private static final Logger log = LoggerFactory.getLogger(SubjectProfileAuthenticationListener.class);

  private static final String HOME_PERM = "FILES_SHARE";

  private static final String PROFILE_CHECKED = "profileChecked";

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private OpalRuntime opalRuntime;

  /**
   * Check user home folder
   */
  @Override
  public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
    // TODO should move this to OpalSessionListener.onStart() but we can't access username on Session start
    Subject subject = new Subject.Builder().principals(info.getPrincipals()).buildSubject();
    Object principal = subject.getPrincipal();
    if(principal != null) {
      String username = principal.toString();
      if(!BackgroundJobRealm.SystemPrincipal.PRINCIPAL.equals(username)) {
        log.debug("Ensure HOME folder for {}", username);
        subjectProfileService.ensureProfile(subject);
        ensureUserHomeExists(username);
        ensureFolderPermissions(username, "/home/" + username);
        ensureFolderPermissions(username, "/tmp");
      }
    }
  }

  @Override
  public void onFailure(AuthenticationToken token, AuthenticationException ae) {
  }

  @Override
  public void onLogout(PrincipalCollection principals) {
  }

  private void ensureUserHomeExists(String username) {
    try {
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
    boolean found = false;
    for(SubjectAclService.Permissions acl : subjectAclService
        .getNodePermissions("opal", folderNode, SubjectAcl.SubjectType.SUBJECT_CREDENTIALS)) {
      found = findPermission(acl, HOME_PERM);
      if(found) break;
    }
    if(!found) {
      subjectAclService
          .addSubjectPermission("opal", folderNode, SubjectAcl.SubjectType.SUBJECT_CREDENTIALS.subjectFor(username),
              HOME_PERM);
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
