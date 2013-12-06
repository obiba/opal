/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.easymock.EasyMock;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.domain.unit.KeyStoreState;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.unit.OpalKeyStore;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link KeyStoreServiceImpl}.
 */
public class KeyStoreServiceImplTest {

  private KeyStoreServiceImpl unitKeyStoreService;

  private OrientDbService mockOrientDbService;

  @Before
  public void setUp() {
    mockOrientDbService = createMock(OrientDbService.class);
    unitKeyStoreService = new KeyStoreServiceImpl();
    unitKeyStoreService.setOrientDbService(mockOrientDbService);
    unitKeyStoreService.setCallbackHandler(createPasswordCallbackHandler());
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void testGetUnitKeyStoreThrowsExceptionOnNullUnitName() {
    unitKeyStoreService.getUnitKeyStore(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUnitKeyStoreThrowsExceptionOnZeroLengthUnitName() {
    unitKeyStoreService.getUnitKeyStore("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUnitKeyStoreThrowsExceptionOnWhitespaceOnlyUnitName() {
    unitKeyStoreService.getUnitKeyStore(" \t \n \r\n");
  }

  @Test
  public void testGetUnitKeyStore() throws IOException {
    KeyStoreState expectedKeyStoreStateTemplate = new KeyStoreState();
    expectedKeyStoreStateTemplate.setName("my-unit");
    KeyStoreState state = new KeyStoreState();
    state.setName("my-unit");
    state.setKeyStore(getTestKeyStoreByteArray());

    expect(mockOrientDbService.findUnique(state)) //
        .andReturn(state) //
        .once();

    replay(mockOrientDbService);

    OpalKeyStore opalKeyStore = unitKeyStoreService.getUnitKeyStore("my-unit");
    verify(mockOrientDbService);

    assertThat(opalKeyStore, IsNull.notNullValue());
    //noinspection ConstantConditions
    assertThat(opalKeyStore.getUnitName(), is(state.getName()));
  }

  @Test
  public void testGetOrCreateUnitKeyStoreCreatesTheKeyStoreIfItDoesNotExist() throws Exception {

    KeyStoreState state = new KeyStoreState();
    state.setName("my-unit");

    expect(mockOrientDbService.findUnique(state)) //
        .andReturn(null) //
        .times(2);

    mockOrientDbService.save(state, state);
    EasyMock.expectLastCall().once();

    replay(mockOrientDbService);

    OpalKeyStore opalKeyStore = unitKeyStoreService.getOrCreateUnitKeyStore("my-unit");

    verify(mockOrientDbService);

    assertNotNull(opalKeyStore);
  }

  //
  // Helper Methods
  //

  private CallbackHandler createPasswordCallbackHandler() {
    return new CallbackHandler() {
      @Override
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(Callback callback : callbacks) {
          if(callback instanceof PasswordCallback) {
            ((PasswordCallback) callback).setPassword("password".toCharArray());
          }
        }
      }
    };
  }

  private byte[] getTestKeyStoreByteArray() throws IOException {

    byte[] barray;
    ByteArrayOutputStream baos = null;
    InputStream testKeyStoreStream = null;
    try {
      baos = new ByteArrayOutputStream();

      testKeyStoreStream = new FileInputStream(
          FileUtil.getFileFromResource("DefaultUnitKeyStoreServiceImplTest/opal.jks"));
      while(testKeyStoreStream.available() != 0) {
        byte[] buf = new byte[1024];
        int bytesRead = testKeyStoreStream.read(buf);
        baos.write(buf, 0, bytesRead);
      }

      barray = baos.toByteArray();
    } finally {
      if(baos != null) baos.close();
      if(testKeyStoreStream != null) testKeyStoreStream.close();
    }
    return barray;

  }

}
