/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.install;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.obiba.core.util.HexUtil;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateOpalSecretKeyInstallStep implements InstallStep {

  @Autowired
  private OpalConfigurationService configurationService;

  @Override
  public String getDescription() {
    return "Generate a secret key specific to this Opal instance.";
  }

  @Override
  public void execute(Version currentVersion) {
    configurationService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {
      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.setSecretKey(generateSecretKey());
      }

      @SuppressWarnings("MagicNumber")
      private String generateSecretKey() {
        KeyGenerator keyGenerator;
        try {
          keyGenerator = KeyGenerator.getInstance("AES");
        } catch(NoSuchAlgorithmException e) {
          throw new RuntimeException("Cannot generate AES key. Your JVM is non-standard.", e);
        }
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();
        return HexUtil.bytesToHex(key.getEncoded());
      }
    });
  }

}
