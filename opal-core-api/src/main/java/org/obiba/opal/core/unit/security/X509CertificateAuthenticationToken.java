package org.obiba.opal.core.unit.security;

import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.apache.shiro.authc.AuthenticationToken;

public class X509CertificateAuthenticationToken implements AuthenticationToken {

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