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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
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
      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key [" + studyId + "]:  ", false);
      keyStore.load(new ByteArrayInputStream(javaKeyStore), getKeyPassword(passwordCallback));
    } catch(KeyStoreException e) {
      clearPasswordCache(studyId);
      throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(CertificateException e) {
      throw new RuntimeException(e);
    } catch(IOException ex) {
      clearPasswordCache(studyId);
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
      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key [" + studyId + "]:  ", false);
      keyStore.store(b, getKeyPassword(passwordCallback));
    } catch(KeyStoreException e) {
      clearPasswordCache(studyId);
      throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(CertificateException e) {
      throw new RuntimeException(e);
    } catch(IOException ex) {
      clearPasswordCache(studyId);
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

      StudyKeyStore.loadBouncyCastle();

      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Enter '" + studyId + "' keystore password:  ", false);
      passwordCallback.setConfirmationPrompt("Re-enter '" + studyId + "' keystore password:  ");
      KeyStore keyStore = null;
      try {
        keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, getKeyPassword(passwordCallback));
      } catch(KeyStoreException e) {
        clearPasswordCache(studyId);
        throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
      } catch(NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch(CertificateException e) {
        throw new RuntimeException(e);
      } catch(IOException ex) {
        clearPasswordCache(studyId);
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

    public void clearPasswordCache(String alias) {
      if(callbackHandler instanceof CachingCallbackHandler) {
        ((CachingCallbackHandler) callbackHandler).clearPasswordCache(alias);
      }
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

      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key [" + studyId + "]:  ", false);

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

  /**
   * Deletes the key associated with the provided alias.
   * @param alias key to delete
   */
  public void deleteKey(String alias) {
    KeyStore keyStore = getKeyStore();
    try {
      keyStore.deleteEntry(alias);
      setKeyStore(keyStore);
    } catch(KeyStoreException e) {
      throw new KeyProviderException(e);
    }

  }

  /**
   * Returns true if the provided alias exists.
   * @param alias check if this alias exists in the KeyStore.
   * @return true if the alias exists
   */
  public boolean aliasExists(String alias) {
    KeyStore keyStore = getKeyStore();
    try {
      return keyStore.containsAlias(alias);
    } catch(KeyStoreException e) {
      throw new KeyProviderException(e);
    }
  }

  public static void loadBouncyCastle() {
    if(java.security.Security.getProvider("BC") == null) java.security.Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * Import a private key and it's associated certificate into the keystore at the given alias.
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   */
  public void importKey(String alias, File privateKey, File certificate) {
    Key key = getPrivateKeyFile(privateKey);
    X509Certificate cert = getCertificateFromFile(certificate);
    KeyStore keyStore = getKeyStore();
    CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key [" + alias + "]:  ", false);
    try {
      keyStore.setKeyEntry(alias, key, getKeyPassword(passwordCallback), new X509Certificate[] { cert });
      setKeyStore(keyStore);
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Import a private key into the keystore and generate an associated certificate at the given alias.
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  public void importKey(String alias, File privateKey, String certificateInfo) {
    KeyPair keyPair = getKeyPairFromFile(privateKey);
    X509Certificate cert;
    try {
      cert = StudyKeyStore.makeCertificate(keyPair.getPrivate(), keyPair.getPublic(), certificateInfo, chooseSignatureAlgorithm(keyPair.getPrivate().getAlgorithm()));
      KeyStore keyStore = getKeyStore();
      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key [" + alias + "]:  ", false);
      keyStore.setKeyEntry(alias, keyPair.getPrivate(), getKeyPassword(passwordCallback), new X509Certificate[] { cert });
      setKeyStore(keyStore);
    } catch(InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch(CertificateEncodingException e) {
      throw new RuntimeException(e);
    } catch(SignatureException e) {
      throw new RuntimeException(e);
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }

  }

  private KeyPair getKeyPairFromFile(File privateKey) {
    try {
      PEMReader pemReader = new PEMReader(new InputStreamReader(new FileInputStream(privateKey)), new PasswordFinder() {

        public char[] getPassword() {
          return System.console().readPassword("%s:  ", "Password for imported private key");
        }
      });
      Object object = pemReader.readObject();
      if(object == null) {
        throw new RuntimeException("The file [" + privateKey.getName() + "] does not contain a PEM file.");
      } else if(object instanceof KeyPair) {
        return (KeyPair) object;
      }
      throw new RuntimeException("Unexpected type [" + object + "]. Expected KeyPair.");
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Key getPrivateKeyFile(File privateKey) {
    try {
      PEMReader pemReader = new PEMReader(new InputStreamReader(new FileInputStream(privateKey)), new PasswordFinder() {

        public char[] getPassword() {
          return System.console().readPassword("%s:  ", "Password for imported private key");
        }
      });
      Object object = pemReader.readObject();
      if(object == null) {
        throw new RuntimeException("The file [" + privateKey.getName() + "] does not contain a PEM file.");
      } else if(object instanceof KeyPair) {
        KeyPair keyPair = (KeyPair) object;
        return keyPair.getPrivate();
      } else if(object instanceof Key) {
        return (Key) object;
      }
      throw new RuntimeException("Unexpected type [" + object + "]. Expected KeyPair or Key.");
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private X509Certificate getCertificateFromFile(File certificate) {
    try {
      PEMReader pemReader = new PEMReader(new InputStreamReader(new FileInputStream(certificate)), new PasswordFinder() {

        public char[] getPassword() {
          return System.console().readPassword("%s:  ", "Password for imported certificate");
        }
      });
      Object object = pemReader.readObject();
      if(object instanceof X509Certificate) {
        return (X509Certificate) object;
      }
      throw new RuntimeException("Unexpected type [" + object + "]. Expected X509Certificate.");
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void clearPasswordCache(String alias) {
    if(callbackHandler instanceof CachingCallbackHandler) {
      ((CachingCallbackHandler) callbackHandler).clearPasswordCache(alias);
    }
  }
}
