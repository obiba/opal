package org.obiba.opal.core.runtime.security;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

/**
 * Force to load {@code org.apache.shiro.mgt.DefaultSecurityManager}
 * via {@code org.obiba.opal.core.runtime.security.OpalSecurityManagerFactory} on context startup.
 */
@Component
public class RegisterSecurityManagerListener implements ApplicationListener<ContextStartedEvent> {

  @Autowired
  @SuppressWarnings("PMD.UnusedPrivateField")
  private DefaultSecurityManager securityManager;

  @Override
  public void onApplicationEvent(ContextStartedEvent event) {
    // do nothing
  }
}
