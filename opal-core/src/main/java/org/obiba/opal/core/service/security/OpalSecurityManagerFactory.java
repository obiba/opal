/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.security;

import java.util.Collection;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import net.sf.ehcache.CacheManager;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.PermissionResolverAware;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.RolePermissionResolverAware;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler;
import org.apache.shiro.session.mgt.SessionValidationScheduler;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.util.LifecycleUtils;
import org.obiba.opal.core.service.security.realm.OpalPermissionResolver;
import org.obiba.shiro.realm.ObibaRealm;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Component
public class OpalSecurityManagerFactory implements FactoryBean<SecurityManager> {

  public static final String INI_REALM = "opal-ini-realm";

  private static final long SESSION_VALIDATION_INTERVAL = 3600000l; // 1 hour

  @Autowired
  private Set<Realm> realms;

  @Autowired
  private Set<SessionListener> sessionListeners;

  @Autowired
  private Set<AuthenticationListener> authenticationListeners;

  @Autowired
  private RolePermissionResolver rolePermissionResolver;

  @NotNull
  @Value("${org.obiba.realm.url}")
  private String obibaRealmUrl;

  @NotNull
  @Value("${org.obiba.realm.service.name}")
  private String serviceName;

  @NotNull
  @Value("${org.obiba.realm.service.key}")
  private String serviceKey;

  @Autowired
  private CacheManager cacheManager;

  private final PermissionResolver permissionResolver = new OpalPermissionResolver();

  private SecurityManager securityManager;

  @Override
  public SecurityManager getObject() throws Exception {
    if(securityManager == null) {
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

  private SecurityManager doCreateSecurityManager() {
    return new CustomIniSecurityManagerFactory(System.getProperty("OPAL_HOME") + "/conf/shiro.ini").createInstance();
  }

  private class CustomIniSecurityManagerFactory extends IniSecurityManagerFactory {

    private CustomIniSecurityManagerFactory(String resourcePath) {
      super(resourcePath);
    }

    @Override
    @SuppressWarnings("ChainOfInstanceofChecks")
    protected SecurityManager createDefaultInstance() {
      DefaultSecurityManager dsm = (DefaultSecurityManager) super.createDefaultInstance();

      initializeCacheManager(dsm);
      initializeSessionManager(dsm);
      initializeSubjectDAO(dsm);
      initializeAuthorizer(dsm);
      initializeAuthenticator(dsm);

      return dsm;
    }

    private void initializeCacheManager(DefaultSecurityManager dsm) {
      if(dsm.getCacheManager() == null) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager(cacheManager);
        dsm.setCacheManager(ehCacheManager);
      }
    }

    private void initializeSessionManager(DefaultSecurityManager dsm) {
      if(dsm.getSessionManager() instanceof DefaultSessionManager) {
        setDefaultSessionManager(dsm);
      }
    }

    private void initializeSubjectDAO(DefaultSecurityManager dsm) {
      if(dsm.getSubjectDAO() instanceof DefaultSubjectDAO) {
        ((DefaultSubjectDAO) dsm.getSubjectDAO()).setSessionStorageEvaluator(new OpalSessionStorageEvaluator());
      }
    }

    private void initializeAuthorizer(DefaultSecurityManager dsm) {
      if(dsm.getAuthorizer() instanceof ModularRealmAuthorizer) {
        ((RolePermissionResolverAware) dsm.getAuthorizer()).setRolePermissionResolver(rolePermissionResolver);
        ((PermissionResolverAware) dsm.getAuthorizer()).setPermissionResolver(permissionResolver);
      }
    }

    private void initializeAuthenticator(DefaultSecurityManager dsm) {
      ((AbstractAuthenticator) dsm.getAuthenticator()).setAuthenticationListeners(authenticationListeners);

      if(dsm.getAuthenticator() instanceof ModularRealmAuthenticator) {
        ((ModularRealmAuthenticator) dsm.getAuthenticator()).setAuthenticationStrategy(new FirstSuccessfulStrategy());
      }
    }

    @Override
    protected void applyRealmsToSecurityManager(Collection<Realm> shiroRealms, @SuppressWarnings(
        "ParameterHidesMemberVariable") SecurityManager securityManager) {
      ImmutableList.Builder<Realm> builder = ImmutableList.<Realm>builder().addAll(realms).addAll(shiroRealms);
      if(!Strings.isNullOrEmpty(obibaRealmUrl)) {
        ObibaRealm oRealm = new ObibaRealm();
        oRealm.setBaseUrl(obibaRealmUrl);
        oRealm.setServiceName(serviceName);
        oRealm.setServiceKey(serviceKey);
        builder.add(oRealm);
      }
      super.applyRealmsToSecurityManager(builder.build(), securityManager);
    }

    @Override
    protected Realm createRealm(Ini ini) {
      // Set the resolvers first, because IniRealm is initialized before the resolvers are
      // applied by the ModularRealmAuthorizer
      IniRealm realm = new IniRealm();
      realm.setName(INI_REALM);
      realm.setRolePermissionResolver(rolePermissionResolver);
      realm.setPermissionResolver(permissionResolver);
      realm.setResourcePath(System.getProperty("OPAL_HOME") + "/conf/shiro.ini");
      realm.setCredentialsMatcher(new PasswordMatcher());
      return realm;
    }
  }

  private void setDefaultSessionManager(DefaultSecurityManager dsm) {
    DefaultSessionManager sessionManager = (DefaultSessionManager) dsm.getSessionManager();
    sessionManager.setSessionListeners(sessionListeners);
    sessionManager.setSessionDAO(new EnterpriseCacheSessionDAO());
    SessionValidationScheduler sessionValidationScheduler = new ExecutorServiceSessionValidationScheduler();
    sessionValidationScheduler.enableSessionValidation();
    sessionManager.setSessionValidationScheduler(sessionValidationScheduler);
    sessionManager.setSessionValidationInterval(SESSION_VALIDATION_INTERVAL);
  }
}
