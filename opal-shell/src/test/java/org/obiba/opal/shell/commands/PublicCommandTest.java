/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.Test;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.IOpalRuntime;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.PublicCommandOptions;

import com.google.common.collect.Sets;

/**
 * Unit tests for {@link PublicCommandTest}.
 */
public class PublicCommandTest extends AbstractMagmaTest {

  @Test
  public void testPrintsErrorOnInvalidAlias() throws NoSuchFunctionalUnitException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    UnitKeyStoreService unitKeystoreServiceMock = createMock(UnitKeyStoreService.class);
    FunctionalUnit unit = createFunctionalUnit(unitKeystoreServiceMock, "my-unit");
    KeyStore keystore = createKeystoreForUnit(unit);

    expect(unitKeystoreServiceMock.getOrCreateUnitKeyStore(unit.getName())).andReturn(new UnitKeyStore(unit.getName(), keystore)).atLeastOnce();

    PublicCommandOptions mockOptions = createMockOptionsForInvalidAlias(unit.getName(), "invalid-alias");
    IOpalRuntime mockRuntime = createMockRuntime(createMock(OpalFileSystem.class), unit.getName(), unit);
    OpalShell mockShell = createMockShellForInvalidInvalidAlias();

    OpalConfiguration opalConfiguration = new OpalConfiguration();
    opalConfiguration.setFunctionalUnits(Sets.newHashSet(unit));

    replay(mockOptions, mockRuntime, mockShell, unitKeystoreServiceMock);

    PublicCommand publicCommand = createPublicCommand(mockRuntime, opalConfiguration);
    publicCommand.setOptions(mockOptions);
    publicCommand.setShell(mockShell);
    publicCommand.execute();

    verify(mockOptions, mockRuntime, mockShell, unitKeystoreServiceMock);
  }

  @Test
  public void testPrintsErrorOnInvalidFunctionalUnit() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    UnitKeyStoreService unitKeystoreServiceMock = createMock(UnitKeyStoreService.class);
    FunctionalUnit unit = createFunctionalUnit(unitKeystoreServiceMock, "my-unit");

    PublicCommandOptions mockOptions = createMockOptionsForInvalidUnit("invalid-unit", "unknown");
    IOpalRuntime mockRuntime = createMock(IOpalRuntime.class);

    expect(mockRuntime.getFunctionalUnit("invalid-unit")).andReturn(null);

    OpalShell mockShell = createMockShellForInvalidUnit();
    OpalConfiguration opalConfiguration = new OpalConfiguration();
    opalConfiguration.setFunctionalUnits(Sets.newHashSet(unit));

    replay(mockOptions, mockRuntime, mockShell, unitKeystoreServiceMock);

    PublicCommand publicCommand = createPublicCommand(mockRuntime, opalConfiguration);
    publicCommand.setOptions(mockOptions);
    publicCommand.setShell(mockShell);
    publicCommand.execute();

    verify(mockOptions, mockRuntime, mockShell, unitKeystoreServiceMock);

  }

  private OpalShell createMockShellForInvalidInvalidAlias() {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("No certificate was found for alias '%s'.\n", "invalid-alias");
    return mockShell;
  }

  private OpalShell createMockShellForInvalidUnit() {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Functional unit '%s' does not exist. Cannot decrypt.\n", "invalid-unit");
    return mockShell;
  }

  private KeyStore createKeystoreForUnit(FunctionalUnit unit) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    keystore.load(null, "password".toCharArray());
    keystore.setCertificateEntry(unit.getName(), null);
    return keystore;
  }

  private FunctionalUnit createFunctionalUnit(UnitKeyStoreService unitKeystoreServiceMock, String unitName) {
    FunctionalUnit unit = new FunctionalUnit(unitName, unitName);
    unit.setUnitKeyStoreService(unitKeystoreServiceMock);
    return unit;
  }

  private PublicCommand createPublicCommand(final IOpalRuntime mockRuntime, final OpalConfiguration mockConfig) {
    return new PublicCommand() {
      @Override
      protected IOpalRuntime getOpalRuntime() {
        return mockRuntime;
      }

      @Override
      protected OpalConfiguration getOpalConfiguration() {
        return mockConfig;
      }
    };
  }

  private PublicCommandOptions createMockOptionsForInvalidAlias(String unitName, String alias) {
    PublicCommandOptions mockOptions = createMock(PublicCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.isOut()).andReturn(false).atLeastOnce();

    return mockOptions;
  }

  private PublicCommandOptions createMockOptionsForInvalidUnit(String unitName, String alias) {
    PublicCommandOptions mockOptions = createMock(PublicCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();

    return mockOptions;
  }

  private IOpalRuntime createMockRuntime(OpalFileSystem mockFileSystem, String unitName, FunctionalUnit unit) {
    IOpalRuntime mockRuntime = createMock(IOpalRuntime.class);
    expect(mockRuntime.getFunctionalUnit(unitName)).andReturn(unit);
    return mockRuntime;
  }
}
