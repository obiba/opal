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
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.util.Assert;

/**
 * Contains a study id and its associated KeyStore.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "study_id" }) })
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

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public KeyStore getKeyStore(char[] password) {
    KeyStore keyStore = null;
    try {
      keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(new ByteArrayInputStream(javaKeyStore), password);
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
    }

    return keyStore;
  }

  public void setKeyStore(KeyStore keyStore, char[] password) {
    ByteArrayOutputStream b = new ByteArrayOutputStream();

    try {
      keyStore.store(b, password);
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
    }
    this.javaKeyStore = b.toByteArray();
  }

  public static class Builder {
    private String studyId;

    private char[] password;

    public static Builder newStore() {
      return new Builder();
    }

    public Builder studyId(String studyId) {
      this.studyId = studyId;
      return this;
    }

    public Builder password(char[] password) {
      this.password = password;
      return this;
    }

    public StudyKeyStore build() {
      Assert.hasText(studyId, "studyId must not be null or empty");
      Assert.notNull(password, "password must not be null");
      KeyStore keyStore = null;
      try {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, password);
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
      }

      StudyKeyStore studyKeyStore = new StudyKeyStore();
      studyKeyStore.setStudyId(studyId);
      studyKeyStore.setKeyStore(keyStore, password);
      Arrays.fill(password, ' '); // Zero password to remove it from memory.
      return studyKeyStore;
    }
  }

}
