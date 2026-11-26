/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.persistence;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.service.security.CryptoService;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Stands in for {@code DefaultOpalConfigurationService}, which is both the holder of the generated database password
 * and the service that decrypts it.
 */
class TestOpalConfigurationService implements OpalConfigurationService, CryptoService {

  private final OpalConfiguration configuration = new OpalConfiguration();

  TestOpalConfigurationService(String encryptedPassword) {
    configuration.setSecretKey("testsecretkey1234");
    configuration.setDatabasePassword(encryptedPassword);
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  @Override
  public void readOpalConfiguration() {
  }

  @Override
  public OpalConfiguration getOpalConfiguration() {
    return configuration;
  }

  @Override
  public void modifyConfiguration(ConfigModificationTask task) {
    task.doWithConfig(configuration);
  }

  @Override
  public String generateSecretKey() {
    return "testsecretkey1234";
  }

  @Override
  public String encrypt(String plain) {
    return "enc:" + plain;
  }

  @Override
  public String decrypt(String encrypted) {
    if(!encrypted.startsWith("enc:")) throw new IllegalArgumentException("Cannot decrypt " + encrypted);
    return encrypted.substring("enc:".length());
  }

  @Override
  public InputStream newCipherInputStream(InputStream in) {
    return in;
  }

  @Override
  public OutputStream newCipherOutputStream(OutputStream out) {
    return out;
  }
}
