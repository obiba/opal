/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.upgrade;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.obiba.core.util.HexUtil;
import org.obiba.runtime.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateOpalSecretKeyUpgradeStep extends AbstractConfigurationUpgradeStep {

  @Override
  public String getDescription() {
    return "Generate a secret key specific to this Opal instance.";
  }

  @Override
  protected void doWithConfig(Document opalConfig) {
    NodeList list = opalConfig.getElementsByTagName("fileSystemRoot");
    if(list.getLength() != 1) {
      throw new IllegalStateException("invalid opal configuration");
    }
    Node fileSystemRoot = list.item(0);
    Node secretKey = opalConfig.createElement("secretKey");
    secretKey.setTextContent(generateSecretKey());
    opalConfig.insertBefore(secretKey, fileSystemRoot);
  }

  private String generateSecretKey() {
    KeyGenerator keyGenerator;
    try {
      keyGenerator = KeyGenerator.getInstance("AES");
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException("Cannot generate AES key. Your JVM is non-standard.");
    }
    keyGenerator.init(128);
    SecretKey key = keyGenerator.generateKey();
    return HexUtil.bytesToHex(key.getEncoded());
  }

  @Override
  public Version getAppliesTo() {
    return new Version(1, 5, 0);
  }

}
