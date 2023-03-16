/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.security.realm;

import org.apache.shiro.authc.*;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.security.CredentialsKeyStoreService;
import org.obiba.shiro.authc.X509CertificateAuthenticationToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Realm for applications authenticated by SSL certificate.
 */
@Component
public class OpalApplicationRealm extends OpalBaseRealm implements InitializingBean {

//  private static final Logger log = LoggerFactory.getLogger(ApplicationRealm.class);

  public static final String APPLICATION_REALM = "opal-application-realm";

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  @Override
  public void afterPropertiesSet() {
    setAuthenticationTokenClass(X509CertificateAuthenticationToken.class);
  }

  @Override
  public String getName() {
    return APPLICATION_REALM;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    X509CertificateAuthenticationToken x509Token = (X509CertificateAuthenticationToken) token;
    OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
    for (Map.Entry<String, Certificate> entry : keyStore.getCertificates().entrySet()) {
      String certificateAlias = entry.getKey();
      Certificate certificate = entry.getValue();
      try {
        X509Certificate x509Cert = x509Token.getCredentials();
        x509Cert.verify(certificate.getPublicKey());
        SubjectCredentials sc = subjectCredentialsService.getSubjectCredentialsByCertificateAlias(certificateAlias);

        if (sc != null) {
          SimplePrincipalCollection principals = new SimplePrincipalCollection();
          principals.add(sc.getName(), getName());
          principals.add(x509Token.getPrincipal(), getName());
          return new SimpleAuthenticationInfo(principals, x509Token.getCredentials());
        }
      } catch (GeneralSecurityException e) {
        // Ignore
      }
    }
    throw new IncorrectCredentialsException();
  }

}
