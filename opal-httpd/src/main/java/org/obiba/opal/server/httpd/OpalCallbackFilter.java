package org.obiba.opal.server.httpd;

import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component("opalCallbackFilter")
public class OpalCallbackFilter extends CallbackFilter {

  @Autowired
  private ApplicationContext applicationContext;

  @PostConstruct
  public void init() {
    SecurityFilter securityFilter = (SecurityFilter) applicationContext.getBean("opalSecurityFilter");
    setConfig(securityFilter.getConfig());
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("opalCallbackFilter");
    }
  }

}
