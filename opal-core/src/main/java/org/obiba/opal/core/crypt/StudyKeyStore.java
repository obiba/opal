/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.obiba.core.domain.AbstractEntity;
import org.springframework.util.Assert;

/**
 * Contains a study id and its associated KeyStore.
 */
@Entity
@Table(name = "study_key_store", uniqueConstraints = { @UniqueConstraint(columnNames = { "studyId" }) })
public class StudyKeyStore extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String studyId;

  @Column(nullable = false, length = 1048576)
  private byte[] javaKeyStore;

  @Transient
  KeyStore store;

  @Transient
  private CallbackHandler callbackHandler;

  public void setCallbackHander(CallbackHandler callbackHander) {
    this.callbackHandler = callbackHander;
  }

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public KeyStore getKeyStore() {
    KeyStore keyStore = null;
    try {
      keyStore = KeyStore.getInstance("JCEKS");
      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key " + studyId, false);
      keyStore.load(new ByteArrayInputStream(javaKeyStore), getKeyPassword(passwordCallback));
    } catch(KeyStoreException e) {
      throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(CertificateException e) {
      throw new RuntimeException(e);
    } catch(IOException ex) {
      if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
        throw new KeyProviderSecurityException("Wrong keystore password");
      }
      throw new RuntimeException(ex);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    }

    return keyStore;
  }

  public void setKeyStore(KeyStore keyStore) {
    ByteArrayOutputStream b = new ByteArrayOutputStream();

    try {
      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key " + studyId, false);
      keyStore.store(b, getKeyPassword(passwordCallback));
    } catch(KeyStoreException e) {
      throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(CertificateException e) {
      throw new RuntimeException(e);
    } catch(IOException ex) {
      if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
        throw new KeyProviderSecurityException("Wrong keystore password");
      }
      throw new RuntimeException(ex);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    }
    this.javaKeyStore = b.toByteArray();
  }

  private char[] getKeyPassword(CacheablePasswordCallback passwordCallback) throws UnsupportedCallbackException, IOException {
    callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
    return passwordCallback.getPassword();
  }

  public static class Builder {
    private String studyId;

    private CallbackHandler callbackHandler;

    public static Builder newStore() {
      return new Builder();
    }

    public Builder studyId(String studyId) {
      this.studyId = studyId;
      return this;
    }

    public Builder passwordPrompt(CallbackHandler callbackHandler) {
      this.callbackHandler = callbackHandler;
      return this;
    }

    private char[] getKeyPassword(CacheablePasswordCallback passwordCallback) throws UnsupportedCallbackException, IOException {
      callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
      return passwordCallback.getPassword();
    }

    public StudyKeyStore build() {
      Assert.hasText(studyId, "studyId must not be null or empty");
      Assert.notNull(callbackHandler, "callbackHander must not be null");

      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for new keystore [" + studyId + "]:  ", false);
      KeyStore keyStore = null;
      try {
        keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, getKeyPassword(passwordCallback));
      } catch(KeyStoreException e) {
        throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
      } catch(NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch(CertificateException e) {
        throw new RuntimeException(e);
      } catch(IOException ex) {
        if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
          throw new KeyProviderSecurityException("Wrong keystore password");
        }
        throw new RuntimeException(ex);
      } catch(UnsupportedCallbackException e) {
        throw new RuntimeException(e);
      }

      StudyKeyStore studyKeyStore = new StudyKeyStore();
      studyKeyStore.setStudyId(studyId);
      studyKeyStore.setCallbackHander(callbackHandler);
      studyKeyStore.setKeyStore(keyStore);
      return studyKeyStore;
    }
  }

  public static X509Certificate makeCertificate(PrivateKey issuerPrivateKey, PublicKey subjectPublicKey, String certificateInfo, String signatureAlgorithm) throws SignatureException, InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException {

    final org.bouncycastle.asn1.x509.X509Name issuerDN = new org.bouncycastle.asn1.x509.X509Name(certificateInfo);
    final org.bouncycastle.asn1.x509.X509Name subjectDN = new org.bouncycastle.asn1.x509.X509Name(certificateInfo);
    final int daysTillExpiry = 30 * 365;

    final Calendar expiry = Calendar.getInstance();
    expiry.add(Calendar.DAY_OF_YEAR, daysTillExpiry);

    final org.bouncycastle.x509.X509V3CertificateGenerator certificateGenerator = new org.bouncycastle.x509.X509V3CertificateGenerator();

    certificateGenerator.setSerialNumber(java.math.BigInteger.valueOf(System.currentTimeMillis()));
    certificateGenerator.setIssuerDN(issuerDN);
    certificateGenerator.setSubjectDN(subjectDN);
    certificateGenerator.setPublicKey(subjectPublicKey);
    certificateGenerator.setNotBefore(new Date());
    certificateGenerator.setNotAfter(expiry.getTime());
    certificateGenerator.setSignatureAlgorithm(signatureAlgorithm);

    return certificateGenerator.generate(issuerPrivateKey);
  }

  public void createOfUpdateKey(String alias, String algorithm, int size, String certificateInfo) {
    try {
      KeyPairGenerator keyPairGenerator;

      keyPairGenerator = KeyPairGenerator.getInstance(algorithm);

      keyPairGenerator.initialize(size);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      X509Certificate cert = StudyKeyStore.makeCertificate(keyPair.getPrivate(), keyPair.getPublic(), certificateInfo, chooseSignatureAlgorithm(algorithm));

      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key " + studyId, false);

      KeyStore keyStore = getKeyStore();
      keyStore.setKeyEntry(alias, keyPair.getPrivate(), getKeyPassword(passwordCallback), new X509Certificate[] { cert });
      setKeyStore(keyStore);
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch(CertificateEncodingException e) {
      throw new RuntimeException(e);
    } catch(SignatureException e) {
      throw new RuntimeException(e);
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String chooseSignatureAlgorithm(String keyAlgorithm) {
    // TODO add more algorithms here.
    if(keyAlgorithm.equals("DSA")) {
      return "SHA1withDSA";
    }
    return "SHA1WithRSA";
  }
}
