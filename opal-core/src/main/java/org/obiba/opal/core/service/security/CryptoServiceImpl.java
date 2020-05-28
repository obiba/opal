/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import java.security.Key;

import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.DefaultBlockCipherService;
import org.apache.shiro.util.ByteSource;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CryptoServiceImpl implements CryptoService {

  private static final Logger log = LoggerFactory.getLogger(CryptoServiceImpl.class);

  private OpalConfigurationService configurationService;

  private final AesCipherService cipherService = new AesCipherService();

  private final LegacyAesCipherService legacyCipherService = new LegacyAesCipherService();

  @Autowired
  public void setConfigurationService(OpalConfigurationService configurationService) {
    this.configurationService = configurationService;
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

  private byte[] getSecretKey() {
    return Hex.decode(configurationService.getOpalConfiguration().getSecretKey());
  }

  private static class LegacyAesCipherService extends DefaultBlockCipherService {

    public LegacyAesCipherService() {
      super("AES");
    }
  }
}
