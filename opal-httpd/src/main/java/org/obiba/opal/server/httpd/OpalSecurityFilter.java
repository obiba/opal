package org.obiba.opal.server.httpd;

import io.buji.pac4j.filter.SecurityFilter;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.config.KeycloakOidcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import org.pac4j.oidc.client.KeycloakOidcClient;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component("opalSecurityFilter")
public class OpalSecurityFilter extends SecurityFilter {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private OpalRuntime opalRuntime;
  
  @Autowired
  private Set<Realm> realms;

  @Autowired
  private Set<SessionListener> sessionListeners;

  @Autowired
  private Set<AuthenticationListener> authenticationListeners;

  @Autowired
  private RolePermissionResolver rolePermissionResolver;

  @PostConstruct
  public void init() {
    KeycloakOidcConfiguration kcConfig = new KeycloakOidcConfiguration();
    kcConfig.setClientId("opal");
    kcConfig.setBaseUri("http://localhost:8888/auth");
    kcConfig.setRealm("obiba");
    kcConfig.setSecret("1aa43945-7166-4292-8f46-c4b836054676");
    KeycloakOidcClient kcClient = new KeycloakOidcClient();
    kcClient.setConfiguration(kcConfig);

    Config config = new Config();
    config.setClients(new Clients("http://localhost:8080/callback", kcClient));
    setConfig(config);
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("opalSecurityFilter");
    }
  }

}
