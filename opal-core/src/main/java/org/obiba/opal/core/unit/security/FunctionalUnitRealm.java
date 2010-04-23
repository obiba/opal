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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.KeyStore.Entry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.security.auth.x500.X500Principal;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 *
 */
@Component
public class FunctionalUnitRealm extends AuthorizingRealm {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitRealm.class);

  public static class X509CertificateAuthenticationToken implements AuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final X509Certificate certificate;

    public X509CertificateAuthenticationToken(final X509Certificate certificate) {
      if(certificate == null) throw new IllegalArgumentException("certificate cannot be null");
      this.certificate = certificate;
    }

    @Override
    public X509Certificate getCredentials() {
      return certificate;
    }

    @Override
    public X500Principal getPrincipal() {
      return certificate.getSubjectX500Principal();
    }

  }

  @Autowired
  private OpalRuntime opalRuntime;

  private Multimap<FunctionalUnit, String> grants = HashMultimap.create();

  public void grant(FunctionalUnit fu, String... permissions) {
    grants.putAll(fu, Arrays.asList(permissions));
    super.getAuthorizationCache().clear();
  }

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
    for(FunctionalUnit unit : opalRuntime.getFunctionalUnits()) {
      UnitKeyStore keyStore = unit.getKeyStore();
      for(String alias : keyStore.listAliases()) {
        Entry keyEntry = keyStore.getEntry(alias);
        if(keyEntry instanceof TrustedCertificateEntry) {
          TrustedCertificateEntry tce = (TrustedCertificateEntry) keyEntry;
          Certificate cert = tce.getTrustedCertificate();
          try {
            x509Token.getCredentials().verify(cert.getPublicKey());
            log.info("Checking signature");
            SimplePrincipalCollection principals = new SimplePrincipalCollection();
            principals.add(x509Token.getPrincipal(), getName());
            principals.add(unit.getName(), getName());
            return new SimpleAuthenticationInfo(principals, x509Token.getCredentials());
          } catch(InvalidKeyException e) {
          } catch(CertificateException e) {
          } catch(NoSuchAlgorithmException e) {
          } catch(NoSuchProviderException e) {
          } catch(SignatureException e) {
          }
        }
      }
    }
    throw new IncorrectCredentialsException();
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    for(Object principal : principals.fromRealm(getName())) {
      if(principal instanceof String) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(ImmutableSet.of((String) principal));
        for(String perm : this.grants.get(opalRuntime.getFunctionalUnit((String) principal))) {
          log.info("Allowing {}", perm);
          info.addStringPermission(perm);
        }
        return info;
      }
    }
    return null;
  }
}
