/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
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
import net.sf.ehcache.CacheManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.PermissionResolverAware;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.RolePermissionResolverAware;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.obiba.opal.core.service.security.realm.OpalPermissionResolver;
import org.obiba.shiro.realm.ObibaRealm;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Component
public class OpalSecurityManagerFactory implements FactoryBean<SessionsSecurityManager> {

  private static final long SESSION_VALIDATION_INTERVAL = 300000l; // 5 minutes

  private final Set<Realm> realms;

  private final Set<SessionListener> sessionListeners;

  private final Set<AuthenticationListener> authenticationListeners;

  private final RolePermissionResolver rolePermissionResolver;

  @NotNull
  @Value("${org.obiba.realm.url}")
  private String obibaRealmUrl;

  @NotNull
  @Value("${org.obiba.realm.service.name}")
  private String serviceName;

  @NotNull
  @Value("${org.obiba.realm.service.key}")
  private String serviceKey;

  private final CacheManager cacheManager;

  private final PermissionResolver permissionResolver = new OpalPermissionResolver();

  private SessionsSecurityManager securityManager;

  @Autowired
  public OpalSecurityManagerFactory(Set<Realm> realms, Set<SessionListener> sessionListeners, Set<AuthenticationListener> authenticationListeners, RolePermissionResolver rolePermissionResolver, CacheManager cacheManager) {
    this.realms = realms;
    this.sessionListeners = sessionListeners;
    this.authenticationListeners = authenticationListeners;
    this.rolePermissionResolver = rolePermissionResolver;
    this.cacheManager = cacheManager;
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

  @PreDestroy
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
      EhCacheManager ehCacheManager = new EhCacheManager();
      ehCacheManager.setCacheManager(cacheManager);
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
    if (dsm.getAuthorizer() instanceof ModularRealmAuthorizer) {
      ((RolePermissionResolverAware) dsm.getAuthorizer()).setRolePermissionResolver(rolePermissionResolver);
      ((PermissionResolverAware) dsm.getAuthorizer()).setPermissionResolver(permissionResolver);
    }
  }

  private void initializeAuthenticator(DefaultSecurityManager dsm) {
    ((AbstractAuthenticator) dsm.getAuthenticator()).setAuthenticationListeners(authenticationListeners);

    if (dsm.getAuthenticator() instanceof ModularRealmAuthenticator) {
      ((ModularRealmAuthenticator) dsm.getAuthenticator()).setAuthenticationStrategy(new FirstSuccessfulStrategy());
    }
  }

}
