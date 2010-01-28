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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.obiba.magma.crypt.support.CacheablePasswordCallback;
import org.springframework.util.Assert;

/**
 * Contains a study id and its associated KeyStore.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "studyId" }) })
public class StudyKeyStore {

  @SuppressWarnings("unused")
  @Id
  @GeneratedValue
  @Column
  private long id;

  @Column(nullable = false)
  private String studyId;

  @Column(nullable = false)
  private byte[] javaKeyStore;

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

  private char[] getKeyPassword(PasswordCallback passwordCallback) throws UnsupportedCallbackException, IOException {
    callbackHandler.handle(new Callback[] { passwordCallback });
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

    private char[] getKeyPassword(PasswordCallback passwordCallback) throws UnsupportedCallbackException, IOException {
      callbackHandler.handle(new Callback[] { passwordCallback });
      return passwordCallback.getPassword();
    }

    public StudyKeyStore build() {
      Assert.hasText(studyId, "studyId must not be null or empty");
      Assert.notNull(callbackHandler, "callbackHander must not be null");

      CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback(studyId, "Password for key " + studyId, false);
      KeyStore keyStore = null;
      try {
        keyStore = KeyStore.getInstance("JCEKS");
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(callbackHandler);
        // System.out.println("password:  " + Arrays.toString(getKeyPassword(passwordCallback)));
        keyStore.load(null, getKeyPassword(passwordCallback));
        // KeyStore.Builder b = KeyStore.Builder.newInstance(keyStore, protectionParameter);
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

}
