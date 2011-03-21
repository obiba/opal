/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import java.util.Collections;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

@Component
public class OpalSecurityManagerFactory implements FactoryBean<DefaultSecurityManager> {

  private final Set<Realm> realms;

  private final Set<SessionListener> sessionListeners;

  private final RolePermissionResolver rolePermissionResolver;

  private DefaultSecurityManager securityManager;

  @Autowired
  public OpalSecurityManagerFactory(Set<Realm> securityRealms, Set<SessionListener> sessionListeners, RolePermissionResolver rolePermissionResolver) {
    this.realms = securityRealms;
    this.sessionListeners = sessionListeners;
    this.rolePermissionResolver = rolePermissionResolver;
  }

  @Override
  public DefaultSecurityManager getObject() throws Exception {
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
    securityManager.destroy();
    securityManager = null;
  }

  private DefaultSecurityManager doCreateSecurityManager() {
    IniSecurityManagerFactory f = new IniSecurityManagerFactory(System.getProperty("OPAL_HOME") + "/conf/shiro.ini") {
      @Override
      protected boolean shouldImplicitlyCreateRealm(Ini ini) {
        return false;
      }
    };

    DefaultSecurityManager dsm = (DefaultSecurityManager) f.createInstance();
    if(dsm.getCacheManager() == null) {
      dsm.setCacheManager(new MemoryConstrainedCacheManager());
    }

    if(dsm.getSessionManager() instanceof DefaultSessionManager) {
      ((DefaultSessionManager) dsm.getSessionManager()).setSessionListeners(sessionListeners);
    }

    if(dsm.getAuthorizer() instanceof ModularRealmAuthorizer) {
      ((ModularRealmAuthorizer) dsm.getAuthorizer()).setRolePermissionResolver(rolePermissionResolver);
      ((ModularRealmAuthorizer) dsm.getAuthorizer()).setPermissionResolver(new OpalPermissionResolver());
    }

    Iterable<Realm> configuredRealms = dsm.getRealms() != null ? dsm.getRealms() : Collections.<Realm> emptyList();

    IniRealm ir = new IniRealm();
    ir.setResourcePath(System.getProperty("OPAL_HOME") + "/conf/shiro.ini");

    dsm.setRealms(ImmutableSet.<Realm> builder().addAll(configuredRealms).add(ir).addAll(this.realms).build());

    ir.init();
    return dsm;
  }
}
