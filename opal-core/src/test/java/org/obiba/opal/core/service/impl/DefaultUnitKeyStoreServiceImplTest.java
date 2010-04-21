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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.obiba.opal.core.runtime.DefaultOpalRuntime;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;

import com.google.common.collect.ImmutableSet;

/**
 * Unit tests for {@link DefaultUnitKeyStoreServiceImpl}.
 */
public class DefaultUnitKeyStoreServiceImplTest {
  //
  // Instance Variables
  //

  private DefaultUnitKeyStoreServiceImpl unitKeyStoreService;

  private PersistenceManager mockPersistenceManager;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    mockPersistenceManager = createMock(PersistenceManager.class);

    unitKeyStoreService = new DefaultUnitKeyStoreServiceImpl();
    unitKeyStoreService.setPersistenceManager(mockPersistenceManager);
    unitKeyStoreService.setOpalRuntime(createOpalRuntime());
    unitKeyStoreService.setCallbackHandler(createPasswordCallbackHandler());
  }

  //
  // Test Methods
  //

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

  @Test(expected = NoSuchFunctionalUnitException.class)
  public void testGetUnitKeyStoreThrowsExceptionIfUnitDoesNotExist() {
    unitKeyStoreService.setOpalRuntime(createOpalRuntime());
    unitKeyStoreService.getUnitKeyStore("no-such-unit");
  }

  @Test
  public void testGetUnitKeyStore() throws IOException {
    UnitKeyStoreState expectedUnitKeyStoreStateTemplate = new UnitKeyStoreState();
    expectedUnitKeyStoreStateTemplate.setUnit("my-unit");
    UnitKeyStoreState matchedUnitKeyStoreState = new UnitKeyStoreState();
    matchedUnitKeyStoreState.setUnit("my-unit");
    matchedUnitKeyStoreState.setKeyStore(getTestKeyStoreByteArray());
    expect(mockPersistenceManager.matchOne(eqUnitKeyStoreState(expectedUnitKeyStoreStateTemplate))).andReturn(matchedUnitKeyStoreState);

    replay(mockPersistenceManager);

    UnitKeyStore unitKeyStore = unitKeyStoreService.getUnitKeyStore("my-unit");

    verify(mockPersistenceManager);

    UnitKeyStore expectedUnitKeyStore = new UnitKeyStore("my-unit", null);
    assertEquals(expectedUnitKeyStore.getUnitName(), unitKeyStore.getUnitName());
  }

  @Test
  public void testGetOrCreateUnitKeyStoreCreatesTheKeyStoreIfItDoesNotExist() throws IOException, UnsupportedCallbackException {
    UnitKeyStoreState expectedUnitKeyStoreStateTemplate = new UnitKeyStoreState();
    expectedUnitKeyStoreStateTemplate.setUnit("my-unit");
    expect(mockPersistenceManager.matchOne(eqUnitKeyStoreState(expectedUnitKeyStoreStateTemplate))).andReturn(null).atLeastOnce();
    expect(mockPersistenceManager.save((UnitKeyStoreState) anyObject())).andReturn(new UnitKeyStoreState());

    replay(mockPersistenceManager);

    UnitKeyStore unitKeyStore = unitKeyStoreService.getOrCreateUnitKeyStore("my-unit");

    verify(mockPersistenceManager);

    assertNotNull(unitKeyStore);
  }

  //
  // Helper Methods
  //

  private DefaultOpalRuntime createOpalRuntime() {
    FunctionalUnit myUnit = new FunctionalUnit("my-unit", "my-keyVariable");

    OpalConfiguration opalConfiguration = new OpalConfiguration();
    opalConfiguration.setFunctionalUnits(ImmutableSet.of(myUnit));

    return new DefaultOpalRuntime(opalConfiguration);
  }

  private CallbackHandler createPasswordCallbackHandler() {
    return new CallbackHandler() {
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

      testKeyStoreStream = new FileInputStream("src/test/resources/DefaultUnitKeyStoreServiceImplTest/opal.jks");
      while(testKeyStoreStream.available() != 0) {
        byte[] buf = new byte[1024];
        int bytesRead = testKeyStoreStream.read(buf);
        baos.write(buf, 0, bytesRead);
      }

      barray = baos.toByteArray();
    } finally {
      baos.close();
      testKeyStoreStream.close();
    }
    return barray;

  }

  //
  // Inner Classes
  //

  static class UnitKeyStoreStateMatcher implements IArgumentMatcher {

    private UnitKeyStoreState expected;

    public UnitKeyStoreStateMatcher(UnitKeyStoreState expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof UnitKeyStoreState) {
        return ((UnitKeyStoreState) actual).getUnit().equals(expected.getUnit());
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqUnitKeyStoreState(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with unit \"");
      buffer.append(expected.getUnit());
      buffer.append("\")");
    }

  }

  static UnitKeyStoreState eqUnitKeyStoreState(UnitKeyStoreState in) {
    EasyMock.reportMatcher(new UnitKeyStoreStateMatcher(in));
    return null;
  }

}
