package org.obiba.opal.core.service.impl;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalSessionListener implements SessionListener {

  @Autowired
  private SubjectAclService subjectAclService;

  @Override
  public void onStart(Session session) {

  }

  @Override
  public void onStop(Session session) {
    subjectAclService.deleteNodePermissions("/auth/session/" + session.getId());
  }

  @Override
  public void onExpiration(Session session) {
    subjectAclService.deleteNodePermissions("/auth/session/" + session.getId());
  }
}
