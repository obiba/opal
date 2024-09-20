/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.cfg;

import com.google.common.base.Strings;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.util.ByteSource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.GlobalMethodProvider;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.magma.js.OpalGlobalMethodProvider;
import org.obiba.opal.core.service.security.CryptoService;
import org.obiba.shiro.crypto.LegacyAesCipherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DefaultOpalConfigurationService implements OpalConfigurationService, CryptoService {

  private static final Logger log = LoggerFactory.getLogger(DefaultOpalConfigurationService.class);

  private static final int DATABASE_PASSWORD_LENGTH = 15;

  @Autowired
  private OpalConfigurationIo opalConfigIo;

  private final AesCipherService cipherService = new AesCipherService();

  private final LegacyAesCipherService legacyCipherService = new LegacyAesCipherService();


  private final Lock opalConfigurationLock = new ReentrantLock();

  private final Condition opalConfigAvailable = opalConfigurationLock.newCondition();

  protected OpalConfiguration opalConfiguration;

  @Override
  public void start() {
    configureMagma();
    readOpalConfiguration();
    configureSecretKey();
    configureDatabasePassword();
  }

  private void configureMagma() {
    // Add opal specific javascript methods
    Collection<GlobalMethodProvider> providers = new HashSet<>();
    providers.add(new OpalGlobalMethodProvider());
    MagmaContextFactory ctxFactory = new MagmaContextFactory();
    ctxFactory.setGlobalMethodProviders(providers);

    MagmaJsExtension jsExtension = new MagmaJsExtension();
    jsExtension.setMagmaContextFactory(ctxFactory);

    // We need these two extensions to read the opal config file
    new MagmaEngine().extend(new MagmaXStreamExtension()).extend(jsExtension);
  }

  private void configureSecretKey() {
    if (Strings.isNullOrEmpty(opalConfiguration.getSecretKey())) {
      log.info("Generate new secret key");
      modifyConfiguration(new ConfigModificationTask() {
        @Override
        public void doWithConfig(OpalConfiguration config) {
          config.setSecretKey(generateSecretKey());
        }
      });
    }
  }

  private void configureDatabasePassword() {
    if (Strings.isNullOrEmpty(opalConfiguration.getDatabasePassword())) {
      log.info("Generate new database password");
      modifyConfiguration(new ConfigModificationTask() {
        @Override
        public void doWithConfig(OpalConfiguration config) {
          String password = new SecureRandomNumberGenerator().nextBytes(DATABASE_PASSWORD_LENGTH).toString();
          config.setDatabasePassword(encrypt(password));
        }
      });
    }
  }

  @Override
  public void stop() {
    MagmaEngine.get().shutdown();
  }

  @Override
  public void readOpalConfiguration() {
    opalConfigurationLock.lock();
    try {
      opalConfiguration = opalConfigIo.readConfiguration();
      opalConfigAvailable.signalAll();
    } finally {
      opalConfigurationLock.unlock();
    }
  }

  @Override
  public OpalConfiguration getOpalConfiguration() {
    opalConfigurationLock.lock();
    try {
      while (opalConfiguration == null) {
        opalConfigAvailable.await();
      }
      return opalConfiguration;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      opalConfigurationLock.unlock();
    }
  }

  @Override
  public void modifyConfiguration(ConfigModificationTask task) {
    opalConfigurationLock.lock();
    try {
      task.doWithConfig(getOpalConfiguration());
      opalConfigIo.writeConfiguration(opalConfiguration);
    } finally {
      opalConfigurationLock.unlock();
    }
  }

  @Override
  public String encrypt(String plain) {
    ByteSource encrypted = cipherService.encrypt(CodecSupport.toBytes(plain), getSecretKey());
    return encrypted.toHex();
  }

  @Override
  public String decrypt(String encrypted) {
    ByteSource decrypted;
    try {
      decrypted = cipherService.decrypt(Hex.decode(encrypted), getSecretKey());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.warn("Falling back on legacy crypto service", e);
      }
      decrypted = legacyCipherService.decrypt(Hex.decode(encrypted), getSecretKey());
    }
    return CodecSupport.toString(decrypted.getBytes());
  }

  @Override
  public String generateSecretKey() {
    Key key = cipherService.generateNewKey();
    return Hex.encodeToString(key.getEncoded());
  }

  @Override
  public InputStream newCipherInputStream(InputStream in) {
    try {
      return new CipherInputStream(in, getAESCipher(Cipher.DECRYPT_MODE));
    } catch (GeneralSecurityException e) {
      log.error("Cipher error", e);
      return new CipherInputStream(in, new NullCipher());
    }
  }

  @Override
  public OutputStream newCipherOutputStream(OutputStream out) {
    try {
      return new CipherOutputStream(out, getAESCipher(Cipher.ENCRYPT_MODE));
    } catch (GeneralSecurityException e) {
      log.error("Cipher error", e);
      return new CipherOutputStream(out, new NullCipher());
    }
  }

  //
  // Private methods
  //

  private byte[] getSecretKey() {
    return Hex.decode(getOpalConfiguration().getSecretKey());
  }

  private Cipher getAESCipher(int mode) throws GeneralSecurityException {
    byte[] decodedKey = getSecretKey();
    SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(mode, secretKey); // Mode: Cipher.DECRYPT_MODE
    return cipher;
  }
}
