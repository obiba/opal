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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.ehcache.integrations.shiro.EhcacheShiroManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.shiro.realm.OIDCRealm;
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.opal.core.service.security.realm.OpalModularRealmAuthorizer;
import org.obiba.opal.core.service.security.realm.OpalPermissionResolver;
import org.obiba.shiro.EhCache3ShiroManager;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.realm.ObibaRealm;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

@Component
public class OpalSecurityManagerFactory implements FactoryBean<SessionsSecurityManager>, DisposableBean {

  private static final long SESSION_VALIDATION_INTERVAL = 300000l; // 5 minutes

  private final Set<Realm> realms;

  private final Set<SessionListener> sessionListeners;

  private final Set<AuthenticationListener> authenticationListeners;

  private final RolePermissionResolver rolePermissionResolver;

  private final SubjectTokenService subjectTokenService;

  @NotNull
  @Value("${org.obiba.realm.url}")
  private String obibaRealmUrl;

  @NotNull
  @Value("${org.obiba.realm.service.name}")
  private String serviceName;

  @NotNull
  @Value("${org.obiba.realm.service.key}")
  private String serviceKey;

  private final PermissionResolver permissionResolver = new OpalPermissionResolver();

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private SessionsSecurityManager securityManager;

  @Autowired
  @Lazy
  public OpalSecurityManagerFactory(Set<Realm> realms, Set<SessionListener> sessionListeners, Set<AuthenticationListener> authenticationListeners,
                                    RolePermissionResolver rolePermissionResolver, SubjectTokenService subjectTokenService,
                                    OIDCConfigurationProvider oidcConfigurationProvider) {
    this.realms = realms;
    this.sessionListeners = sessionListeners;
    this.authenticationListeners = authenticationListeners;
    this.rolePermissionResolver = rolePermissionResolver;
    this.subjectTokenService = subjectTokenService;
    this.oidcConfigurationProvider = oidcConfigurationProvider;
  }

  @Override
  public SessionsSecurityManager getObject() {
    if (securityManager == null) {
      securityManager = doCreateSecurityManager();
      SecurityUtils.setSecurityManager(securityManager);
    }
    return securityManager;
  }

  @Override
  public Class<?> getObjectType() {
    return DefaultSecurityManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public void destroy() throws Exception {
    destroySecurityManager();
  }

  public void destroySecurityManager() {
    // Destroy the security manager.
    SecurityUtils.setSecurityManager(null);
    LifecycleUtils.destroy(securityManager);
    securityManager = null;
  }

  private SessionsSecurityManager doCreateSecurityManager() {
    List<Realm> shiroRealms = Lists.newArrayList(realms);
    if (!Strings.isNullOrEmpty(obibaRealmUrl)) {
      ObibaRealm oRealm = new ObibaRealm();
      oRealm.setBaseUrl(obibaRealmUrl);
      oRealm.setServiceName(serviceName);
      oRealm.setServiceKey(serviceKey);
      shiroRealms.add(oRealm);
    }
    for (OIDCConfiguration configuration : oidcConfigurationProvider.getConfigurations()) {
      boolean enabled = false;
      if (configuration.getCustomParams().containsKey("enabled")) {
        try {
          enabled = Boolean.parseBoolean(configuration.getCustomParam("enabled"));
        } catch (Exception e) {
          // ignore
        }
      }
      if (enabled) {
        OIDCRealm realm = new OIDCRealm(configuration);
        shiroRealms.add(realm);
      }
    }
    DefaultWebSecurityManager dsm = new DefaultWebSecurityManager(shiroRealms);
    initializeCacheManager(dsm);
    initializeSessionManager(dsm);
    initializeSubjectDAO(dsm);
    initializeAuthorizer(dsm);
    initializeAuthenticator(dsm);
    return dsm;
  }

  private void initializeCacheManager(DefaultSecurityManager dsm) {
    if (dsm.getCacheManager() == null) {
      EhcacheShiroManager ehCacheManager = new EhCache3ShiroManager();
      ehCacheManager.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
      dsm.setCacheManager(ehCacheManager);
    }
  }

  private void initializeSessionManager(DefaultWebSecurityManager dsm) {
    DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
    sessionManager.setSessionListeners(sessionListeners);
    sessionManager.setSessionDAO(new EnterpriseCacheSessionDAO());
    sessionManager.setSessionValidationInterval(SESSION_VALIDATION_INTERVAL);
    sessionManager.setSessionValidationSchedulerEnabled(true);
    dsm.setSessionManager(sessionManager);
  }

  private void initializeSubjectDAO(DefaultSecurityManager dsm) {
    if (dsm.getSubjectDAO() instanceof DefaultSubjectDAO) {
      ((DefaultSubjectDAO) dsm.getSubjectDAO()).setSessionStorageEvaluator(new OpalSessionStorageEvaluator());
    }
  }

  private void initializeAuthorizer(DefaultSecurityManager dsm) {
    ModularRealmAuthorizer authorizer = new OpalModularRealmAuthorizer(dsm.getRealms(), subjectTokenService);
    authorizer.setRolePermissionResolver(rolePermissionResolver);
    authorizer.setPermissionResolver(permissionResolver);
    dsm.setAuthorizer(authorizer);
  }

  private void initializeAuthenticator(DefaultSecurityManager dsm) {
    ((AbstractAuthenticator) dsm.getAuthenticator()).setAuthenticationListeners(authenticationListeners);

    if (dsm.getAuthenticator() instanceof ModularRealmAuthenticator) {
      ((ModularRealmAuthenticator) dsm.getAuthenticator()).setAuthenticationStrategy(new OtpSuccessfulStrategy());
    }
  }

  private class OtpSuccessfulStrategy extends FirstSuccessfulStrategy {
    @Override
    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
      if (t instanceof NoSuchOtpException) throw (NoSuchOtpException) t;
      return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
    }
  }

}
