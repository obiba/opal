/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.unit.security;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FunctionalUnitRealm extends AuthorizingRealm {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitRealm.class);

  @Autowired
  private FunctionalUnitService functionalUnitService;

  @Override
  public String getName() {
    return "functional-unit-realm";
  }

  @Override
  public Class<X509CertificateAuthenticationToken> getAuthenticationTokenClass() {
    return X509CertificateAuthenticationToken.class;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    X509CertificateAuthenticationToken x509Token = (X509CertificateAuthenticationToken) token;
    X509Certificate x509Cert = x509Token.getCredentials();
    for(FunctionalUnit unit : functionalUnitService.getFunctionalUnits()) {
      UnitKeyStore keyStore = unit.getKeyStore();
      for(Certificate cert : keyStore.getCertficateEntries()) {
        try {
          x509Cert.verify(cert.getPublicKey());
          SimplePrincipalCollection principals = new SimplePrincipalCollection();
          principals.add(unit.getName(), getName());
          principals.add(x509Token.getPrincipal(), getName());
          return new SimpleAuthenticationInfo(principals, x509Token.getCredentials());
        } catch(GeneralSecurityException e) {
          // Ignore
        }
      }

    }
    throw new IncorrectCredentialsException();
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return null;
  }

}
