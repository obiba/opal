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

import java.security.KeyStore;

import javax.crypto.KeyGenerator;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.crypt.support.CacheablePasswordCallback;

/**
 * JUnit tests for {@link StudyKeyStore}.
 */
public class StudyKeyStoreTest {

  private final static char[] PASSWORD = "password".toCharArray();

  private StudyKeyStore studyKeyStore;

  private CallbackHandler callbackHandler;

  @Before
  public void setUp() throws Exception {
    callbackHandler = new StudyKeyStoreCallbackHandler(PASSWORD);
    studyKeyStore = StudyKeyStore.Builder.newStore().studyId("test").passwordPrompt(callbackHandler).build();
  }

  @Test
  public void testPasswordReturnedByCacheablePasswordCallback() throws Exception {
    CacheablePasswordCallback passwordCallback = new CacheablePasswordCallback("studyId", "Password for key studyId", false);
    callbackHandler.handle(new Callback[] { passwordCallback });
    Assert.assertArrayEquals(PASSWORD, passwordCallback.getPassword());
  }

  @Test
  public void testPasswordReturnedByTestPasswordCallback() throws Exception {
    StudyKeyStorePasswordCallback passwordCallback = new StudyKeyStorePasswordCallback("prompt", true);
    callbackHandler.handle(new Callback[] { passwordCallback });
    Assert.assertArrayEquals(PASSWORD, passwordCallback.getPassword());
  }

  @Test
  public void testEmptySaveAndRetrieve() throws Exception {
    KeyStore firstKeyStore = studyKeyStore.getKeyStore();
    Assert.assertEquals(0, firstKeyStore.size());
    studyKeyStore.setKeyStore(firstKeyStore);

    KeyStore secondKeyStore = studyKeyStore.getKeyStore();
    Assert.assertEquals(0, secondKeyStore.size());
  }

  @Test
  public void testSingleEntrySaveAndRetrieve() throws Exception {
    KeyStore firstKeyStore = studyKeyStore.getKeyStore();
    Assert.assertEquals(0, firstKeyStore.size());

    javax.crypto.SecretKey mySecretKey = KeyGenerator.getInstance("DES").generateKey();
    KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(mySecretKey);

    KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(PASSWORD);
    firstKeyStore.setEntry("secretKeyAlias", skEntry, protectionParameter);

    studyKeyStore.setKeyStore(firstKeyStore);

    KeyStore secondKeyStore = studyKeyStore.getKeyStore();
    Assert.assertEquals(1, secondKeyStore.size());
  }

  @Test
  public void testThreeEntriesSaveAndRetrieve() throws Exception {
    KeyStore firstKeyStore = studyKeyStore.getKeyStore();
    Assert.assertEquals(0, firstKeyStore.size());

    javax.crypto.SecretKey mySecretKey = KeyGenerator.getInstance("DES").generateKey();
    KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(mySecretKey);

    KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(PASSWORD);
    firstKeyStore.setEntry("secretKeyAlias", skEntry, protectionParameter);
    firstKeyStore.setEntry("secretKeyAliasTwo", skEntry, protectionParameter);
    firstKeyStore.setEntry("secretKeyAliasThree", skEntry, protectionParameter);

    studyKeyStore.setKeyStore(firstKeyStore);

    KeyStore secondKeyStore = studyKeyStore.getKeyStore();
    Assert.assertEquals(3, secondKeyStore.size());
  }

}
