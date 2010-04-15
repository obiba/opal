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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.IOpalRuntime;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;

/**
 * Unit tests for {@link KeyCommand}.
 */
public class KeyCommandTest {
  //
  // Constants
  //

  private static final String[] CERTIFICATE_INFO = { "Bob Friendly", "Department of Bob", "Bob Inc.", "Montreal", "Quebec", "CA" };

  //
  // Test Methods
  //

  @Test
  public void testCreateActionCreatesOrUpdatesKey() {
    KeyCommandOptions mockOptions = createMockOptionsForCreateAction("my-unit", "my-alias", "RSA", 2048);

    IOpalRuntime mockRuntime = createMockRuntime();
    expect(mockRuntime.getFunctionalUnit("my-unit")).andReturn(new FunctionalUnit("my-unit", null)).atLeastOnce();

    OpalShell mockShell = createMockShellForCreateAction("my-alias");

    UnitKeyStoreService mockUnitKeyStoreService = createMock(UnitKeyStoreService.class);
    expect(mockUnitKeyStoreService.aliasExists("my-unit", "my-alias")).andReturn(false).atLeastOnce();
    mockUnitKeyStoreService.createOrUpdateKey("my-unit", "my-alias", "RSA", 2048, getCertificateInfoAsString(CERTIFICATE_INFO));
    expectLastCall().atLeastOnce();

    replay(mockOptions, mockRuntime, mockShell, mockUnitKeyStoreService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime);
    keyCommand.setOptions(mockOptions);
    keyCommand.setShell(mockShell);
    keyCommand.setUnitKeyStoreService(mockUnitKeyStoreService);
    keyCommand.execute();
  }

  @Test
  public void testDeleteActionDeletesKey() {
    KeyCommandOptions mockOptions = createMockOptionsForDeleteAction("my-unit", "my-alias");

    IOpalRuntime mockRuntime = createMockRuntime();
    expect(mockRuntime.getFunctionalUnit("my-unit")).andReturn(new FunctionalUnit("my-unit", null)).atLeastOnce();

    OpalShell mockShell = createMockShellForDeleteAction("my-unit", "my-alias");

    UnitKeyStoreService mockUnitKeyStoreService = createMock(UnitKeyStoreService.class);
    expect(mockUnitKeyStoreService.aliasExists("my-unit", "my-alias")).andReturn(true).atLeastOnce();
    mockUnitKeyStoreService.deleteKey("my-unit", "my-alias");
    expectLastCall().atLeastOnce();

    replay(mockOptions, mockRuntime, mockShell, mockUnitKeyStoreService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime);
    keyCommand.setOptions(mockOptions);
    keyCommand.setShell(mockShell);
    keyCommand.setUnitKeyStoreService(mockUnitKeyStoreService);
    keyCommand.execute();
  }

  @Test
  public void testImportActionImportsKey() throws FileSystemException {
    KeyCommandOptions mockOptions = createMockOptionsForImportAction("my-unit", "my-alias", "private.pem", "certificate.pem");

    FileObject privateFile = createMockFile("private.pem", true, true);
    FileObject certificateFile = createMockFile("certificate.pem", true, true);

    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    expect(mockFileSystemRoot.resolveFile("private.pem")).andReturn(privateFile).atLeastOnce();
    expect(mockFileSystemRoot.resolveFile("certificate.pem")).andReturn(certificateFile).atLeastOnce();

    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);

    IOpalRuntime mockRuntime = createMockRuntime();
    expect(mockRuntime.getFunctionalUnit("my-unit")).andReturn(new FunctionalUnit("my-unit", null)).atLeastOnce();
    expect(mockRuntime.getFileSystem()).andReturn(mockFileSystem).atLeastOnce();

    OpalShell mockShell = createMockShellForImportAction("my-alias");

    UnitKeyStoreService mockUnitKeyStoreService = createMock(UnitKeyStoreService.class);
    expect(mockUnitKeyStoreService.aliasExists("my-unit", "my-alias")).andReturn(false).atLeastOnce();
    mockUnitKeyStoreService.importKey("my-unit", "my-alias", privateFile, certificateFile);
    expectLastCall().atLeastOnce();

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, mockRuntime, mockShell, mockUnitKeyStoreService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime);
    keyCommand.setOptions(mockOptions);
    keyCommand.setShell(mockShell);
    keyCommand.setUnitKeyStoreService(mockUnitKeyStoreService);
    keyCommand.execute();
  }

  @Test
  public void testExportActionExportsKey() throws IOException, GeneralSecurityException {
    KeyCommandOptions mockOptions = createMockOptionsForExportAction("my-unit", "my-alias", "certificate.pem");

    FileContent mockFileContent = createMock(FileContent.class);
    expect(mockFileContent.getOutputStream()).andReturn(new ByteArrayOutputStream()).atLeastOnce();

    FileObject certificateFile = createMockFile("certificate.pem", true, false);
    expect(certificateFile.getContent()).andReturn(mockFileContent).atLeastOnce();

    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    expect(mockFileSystemRoot.resolveFile("certificate.pem")).andReturn(certificateFile).atLeastOnce();

    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);

    OpalShell mockShell = createMockShellForExportAction("certificate.pem");
    mockShell.printf((String) EasyMock.anyObject(), (String) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();

    UnitKeyStoreService mockUnitKeyStoreService = createMock(UnitKeyStoreService.class);
    expect(mockUnitKeyStoreService.aliasExists("my-unit", "my-alias")).andReturn(false).atLeastOnce();
    expect(mockUnitKeyStoreService.getOrCreateUnitKeyStore("my-unit")).andReturn(new UnitKeyStore("my-unit", getKeyStore())).atLeastOnce();

    FunctionalUnit unit = new FunctionalUnit("my-unit", null);
    unit.setUnitKeyStoreService(mockUnitKeyStoreService);

    IOpalRuntime mockRuntime = createMockRuntime();
    expect(mockRuntime.getFunctionalUnit("my-unit")).andReturn(unit).atLeastOnce();
    expect(mockRuntime.getFileSystem()).andReturn(mockFileSystem).atLeastOnce();

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, certificateFile, mockFileContent, mockRuntime, mockShell, mockUnitKeyStoreService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime);
    keyCommand.setOptions(mockOptions);
    keyCommand.setShell(mockShell);
    keyCommand.setUnitKeyStoreService(mockUnitKeyStoreService);
    keyCommand.execute();
  }

  //
  // Helper Methods
  //

  private KeyCommand createKeyCommand(final IOpalRuntime mockRuntime) {
    return new KeyCommand() {
      @Override
      protected IOpalRuntime getOpalRuntime() {
        return mockRuntime;
      }

      @Override
      protected OpalConfiguration getOpalConfiguration() {
        return null;
      }
    };
  }

  private KeyCommandOptions createMockOptionsForCreateAction(String unitName, String alias, String algorithm, int size) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("create").atLeastOnce();
    expect(mockOptions.isAlgorithm()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAlgorithm()).andReturn("RSA").atLeastOnce();
    expect(mockOptions.isSize()).andReturn(true).atLeastOnce();
    expect(mockOptions.getSize()).andReturn(2048).atLeastOnce();

    return mockOptions;
  }

  private IOpalRuntime createMockRuntime() {
    IOpalRuntime mockRuntime = createMock(IOpalRuntime.class);

    return mockRuntime;
  }

  private OpalShell createMockShellForCreateAction(String alias) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("%s:\n", "Certificate creation");
    mockShell.printf(" %s\n", "What is your first and last name?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[0]);
    mockShell.printf(" %s\n", "What is the name of your organizational unit?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[1]);
    mockShell.printf(" %s\n", "What is the name of your organization?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[2]);
    mockShell.printf(" %s\n", "What is the name of your City or Locality?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[3]);
    mockShell.printf(" %s\n", "What is the name of your State or Province?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[4]);
    mockShell.printf(" %s\n", "What is the two-letter country code for this unit?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[5]);
    mockShell.printf(" %s\n", "Is CN=Bob Friendly, OU=Department of Bob, O=Bob Inc., L=Montreal, ST=Quebec, C=CA correct?");
    expect(mockShell.prompt("  [no]:  ")).andReturn("yes");
    mockShell.printf("Key generated with alias '%s'.\n", alias);

    return mockShell;
  }

  private String getCertificateInfoAsString(String[] certificateInfo) {
    StringBuilder sb = new StringBuilder();
    sb.append("CN=");
    sb.append(CERTIFICATE_INFO[0]);
    sb.append(", OU=");
    sb.append(CERTIFICATE_INFO[1]);
    sb.append(", O=");
    sb.append(CERTIFICATE_INFO[2]);
    sb.append(", L=");
    sb.append(CERTIFICATE_INFO[3]);
    sb.append(", ST=");
    sb.append(CERTIFICATE_INFO[4]);
    sb.append(", C=");
    sb.append(CERTIFICATE_INFO[5]);

    return sb.toString();
  }

  private KeyCommandOptions createMockOptionsForDeleteAction(String unitName, String alias) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("delete").atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForDeleteAction(String unitName, String alias) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Deleted key with alias '%s' from keystore '%s'.\n", alias, unitName);

    return mockShell;
  }

  private KeyCommandOptions createMockOptionsForImportAction(String unitName, String alias, String privateFile, String certificateFile) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("import").atLeastOnce();
    expect(mockOptions.isPrivate()).andReturn(true).atLeastOnce();
    expect(mockOptions.getPrivate()).andReturn(privateFile).atLeastOnce();
    expect(mockOptions.isCertificate()).andReturn(true).atLeastOnce();
    expect(mockOptions.getCertificate()).andReturn(certificateFile).atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForImportAction(String alias) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Key imported with alias '%s'.\n", alias);

    return mockShell;
  }

  private KeyCommandOptions createMockOptionsForExportAction(String unitName, String alias, String certificateFile) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("export").atLeastOnce();
    expect(mockOptions.isCertificate()).andReturn(true).atLeastOnce();
    expect(mockOptions.isPrivate()).andReturn(false).atLeastOnce();
    expect(mockOptions.getCertificate()).andReturn(certificateFile).atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForExportAction(String certificateFile) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Certificate written to the file [%s]\n", certificateFile);

    return mockShell;
  }

  private OpalFileSystem createMockFileSystem(FileObject mockFileSystemRoot) throws FileSystemException {
    OpalFileSystem mockFileSystem = createMock(OpalFileSystem.class);
    expect(mockFileSystem.getRoot()).andReturn(mockFileSystemRoot).atLeastOnce();

    return mockFileSystem;
  }

  private FileObject createMockFileSystemRoot() {
    FileObject mockFileSystemRoot = createMock(FileObject.class);
    return mockFileSystemRoot;
  }

  private FileObject createMockFile(String baseName, boolean exists, boolean withReplay) throws FileSystemException {
    FileObject mockFile = createMock(FileObject.class);
    expect(mockFile.exists()).andReturn(exists).anyTimes();
    expect(mockFile.getName()).andReturn(createMockFileName(baseName)).anyTimes();

    if(withReplay) {
      replay(mockFile);
    }

    return mockFile;
  }

  private FileName createMockFileName(String baseName) {
    FileName mockFileName = createMock(FileName.class);
    expect(mockFileName.getBaseName()).andReturn(baseName).atLeastOnce();

    replay(mockFileName);

    return mockFileName;
  }

  private KeyStore getKeyStore() throws IOException, GeneralSecurityException {
    KeyStore keyStore = KeyStore.getInstance("JKS");

    FileInputStream fis = null;
    try {
      fis = new FileInputStream("src/test/resources/KeyCommandTest/opal.jks");
      keyStore.load(fis, "password".toCharArray());
    } finally {
      if(fis != null) {
        fis.close();
      }
    }

    return keyStore;
  }
}
