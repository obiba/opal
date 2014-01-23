package org.obiba.opal.core.service.security.authc;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalSessionListener implements SessionListener {

  private static final Logger log = LoggerFactory.getLogger(OpalSessionListener.class);

  private static final String HOME_PERM = "FILES_SHARE";

  private static final String ENSURED_PROFILE = "ensuredProfile";

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Override
  public void onStart(Session session) {
    Subject subject = SecurityUtils.getSubject();
    Object principal = subject.getPrincipal();

    if(!subjectProfileService.supportProfile(principal)) {
      return;
    }

    Session subjectSession = subject.getSession(false);
    boolean ensuredProfile = subjectSession != null && subjectSession.getAttribute(ENSURED_PROFILE) != null;
    if(!ensuredProfile) {
      String username = principal.toString();
      log.info("Ensure HOME folder for {}", username);
      subjectProfileService.ensureProfile(subject.getPrincipals());
      ensureUserHomeExists(username);
      ensureFolderPermissions(username, "/home/" + username);
      ensureFolderPermissions(username, "/tmp");

      if(subjectSession != null) {
        subjectSession.setAttribute(ENSURED_PROFILE, true);
      }
    }
  }

  @Override
  public void onStop(Session session) {
    subjectAclService.deleteNodePermissions("/auth/session/" + session.getId());
  }

  @Override
  public void onExpiration(Session session) {
    subjectAclService.deleteNodePermissions("/auth/session/" + session.getId());
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
        .getNodePermissions("opal", folderNode, SubjectAcl.SubjectType.USER)) {
      found = findPermission(acl, HOME_PERM);
      if(found) break;
    }
    if(!found) {
      subjectAclService
          .addSubjectPermission("opal", folderNode, SubjectAcl.SubjectType.USER.subjectFor(username), HOME_PERM);
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
