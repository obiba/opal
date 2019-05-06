package org.obiba.opal.core.service.security;

import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalWebEnvironment extends DefaultWebEnvironment {

  @Autowired
  public OpalWebEnvironment(SessionsSecurityManager securityManager) {
    super();
    setSecurityManager(securityManager);
  }

}
