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

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.security.CredentialsKeyStoreService;
import org.obiba.opal.core.security.X509CertificateAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Realm for applications authenticated by SSL certificate.
 */
@Component
public class ApplicationRealm extends AuthorizingRealm {

//  private static final Logger log = LoggerFactory.getLogger(ApplicationRealm.class);

  public static final String APPLICATION_REALM = "opal-application-realm";

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  @Override
  public String getName() {
    return APPLICATION_REALM;
  }

  @Override
  public Class<X509CertificateAuthenticationToken> getAuthenticationTokenClass() {
    return X509CertificateAuthenticationToken.class;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    X509CertificateAuthenticationToken x509Token = (X509CertificateAuthenticationToken) token;
    OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
    for(Map.Entry<String, Certificate> entry : keyStore.getCertificates().entrySet()) {
      String alias = entry.getKey();
      Certificate certificate = entry.getValue();
      try {
        X509Certificate x509Cert = x509Token.getCredentials();
        x509Cert.verify(certificate.getPublicKey());
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(alias, getName());
        principals.add(x509Token.getPrincipal(), getName());
        return new SimpleAuthenticationInfo(principals, x509Token.getCredentials());
      } catch(GeneralSecurityException e) {
        // Ignore
      }
    }
    throw new IncorrectCredentialsException();
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return null;
  }

}
