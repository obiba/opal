/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;

import static org.fest.assertions.api.Assertions.assertThat;

public class CryptoServiceImplTest {

  @Test
  public void testEncryptDecrypt() {

    CryptoServiceImpl cryptoService = new CryptoServiceImpl();

    OpalConfiguration opalConfiguration = new OpalConfiguration();
    opalConfiguration.setSecretKey(cryptoService.generateSecretKey());

    OpalConfigurationService configurationService = EasyMock.createMock(OpalConfigurationService.class);
    EasyMock.expect(configurationService.getOpalConfiguration()).andReturn(opalConfiguration).atLeastOnce();
    EasyMock.replay(configurationService);

    cryptoService.setConfigurationService(configurationService);

    String plain = "text to encrypt";
    String encrypt = cryptoService.encrypt(plain);
    assertThat(encrypt).isNotNull();
    assertThat(encrypt).isNotEqualTo(plain);

    String decrypt = cryptoService.decrypt(encrypt);
    assertThat(decrypt).isEqualTo(plain);
  }

}
