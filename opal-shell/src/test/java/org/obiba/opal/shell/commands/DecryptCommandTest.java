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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.junit.Test;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.IOpalRuntime;
import org.obiba.opal.core.service.DecryptService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.DecryptCommandOptions;

/**
 * Unit tests for {@link DecryptCommand}.
 */
public class DecryptCommandTest extends AbstractMagmaTest {
  //
  // Test Methods
  //

  @Test
  public void testPrintsErrorOnInvalidOutputDirectory() throws FileSystemException {
    DecryptCommandOptions mockOptions = createMockOptionsForInvalidOutputDirectory("my-unit", "$%@%$@#");

    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);

    FileObject mockUnitDir = createMockFile("my-unit", true, false);
    expect(mockUnitDir.resolveFile("$%@%$@#")).andThrow(new FileSystemException("cannot resolve")).atLeastOnce();

    IOpalRuntime mockRuntime = createMockRuntime(mockFileSystem);
    expect(mockRuntime.getUnitDirectory("my-unit")).andReturn(mockUnitDir).atLeastOnce();

    OpalShell mockShell = createMockShellForInvalidOutputDirectory();

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockShell);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockShell);
  }

  @Test
  public void testPrintsErrorIfNoFileSpecified() throws FileSystemException {
    DecryptCommandOptions mockOptions = createMockOptionsForNoFileSpecified();
    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);
    IOpalRuntime mockRuntime = createMockRuntime(mockFileSystem);
    OpalShell mockShell = createMockShellForNoFileSpecified();

    replay(mockOptions, mockFileSystem, mockRuntime, mockShell);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystem, mockRuntime, mockShell);
  }

  @Test
  public void testPrintsErrorOnInvalidFunctionalUnit() throws FileSystemException {
    DecryptCommandOptions mockOptions = createMockOptionsForInvalidFunctionalUnit("bogus");
    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);
    IOpalRuntime mockRuntime = createMockRuntimeForInvalidFunctionalUnit(mockFileSystem, "bogus");
    OpalShell mockShell = createMockShellForInvalidFunctionalUnit("bogus");

    replay(mockOptions, mockFileSystem, mockRuntime, mockShell);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystem, mockRuntime, mockShell);
  }

  @Test
  public void testOutputDirectoryDefaultsToOpalFileSystemRoot() throws IOException {
    DecryptCommandOptions mockOptions = createMockOptionsForDefaultOutputDirectory("my-unit");

    FileObject inputFile = createMockFile("encrypted.zip", true, true);
    FileObject outputFile = createMockFile("encrypted-plaintext.zip", false, true);

    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    expect(mockFileSystemRoot.resolveFile("encrypted-plaintext.zip")).andReturn(outputFile).atLeastOnce();

    FileObject mockUnitDir = createMockFile("my-unit", true, false);
    expect(mockUnitDir.resolveFile("encrypted.zip")).andReturn(inputFile).atLeastOnce();

    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);
    expect(mockFileSystem.getLocalFile(outputFile)).andReturn(new File("encrypted.zip")).atLeastOnce();

    IOpalRuntime mockRuntime = createMockRuntimeForDefaultOutputDirectory(mockFileSystem, "my-unit");
    expect(mockRuntime.getUnitDirectory("my-unit")).andReturn(mockUnitDir).atLeastOnce();

    OpalShell mockShell = createMockShellForDefaultOutputDirectory("encrypted.zip");

    DecryptService mockDecryptService = createMock(DecryptService.class);
    mockDecryptService.decryptData("my-unit", DecryptCommand.DECRYPT_DATASOURCE_NAME, inputFile);

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockShell, mockDecryptService);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime);
    decryptCommand.setDecryptService(mockDecryptService);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockShell, mockDecryptService);
  }

  //
  // Helper Methods
  //

  private DecryptCommand createDecryptCommand(final IOpalRuntime mockRuntime) {
    return new DecryptCommand() {
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

  private DecryptCommandOptions createMockOptionsForInvalidOutputDirectory(String unitName, String invalidOutputDirPath) {
    DecryptCommandOptions mockOptions = createMock(DecryptCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.isOutput()).andReturn(true).atLeastOnce();
    expect(mockOptions.getOutput()).andReturn(invalidOutputDirPath).atLeastOnce();

    return mockOptions;
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

  private IOpalRuntime createMockRuntime(OpalFileSystem mockFileSystem) {
    IOpalRuntime mockRuntime = createMock(IOpalRuntime.class);
    expect(mockRuntime.getFileSystem()).andReturn(mockFileSystem).atLeastOnce();

    return mockRuntime;
  }

  private OpalShell createMockShellForInvalidOutputDirectory() {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Invalid output directory");

    return mockShell;
  }

  private DecryptCommandOptions createMockOptionsForNoFileSpecified() {
    DecryptCommandOptions mockOptions = createMock(DecryptCommandOptions.class);
    expect(mockOptions.isOutput()).andReturn(false).atLeastOnce();
    expect(mockOptions.isFiles()).andReturn(false).atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForNoFileSpecified() {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("No input file specified.\n");

    return mockShell;
  }

  private DecryptCommandOptions createMockOptionsForInvalidFunctionalUnit(String unitName) {
    DecryptCommandOptions mockOptions = createMock(DecryptCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.isOutput()).andReturn(false).atLeastOnce();
    expect(mockOptions.isFiles()).andReturn(true).atLeastOnce();

    return mockOptions;
  }

  private IOpalRuntime createMockRuntimeForInvalidFunctionalUnit(OpalFileSystem mockFileSystem, String invalidUnitName) {
    IOpalRuntime mockRuntime = createMock(IOpalRuntime.class);
    expect(mockRuntime.getFileSystem()).andReturn(mockFileSystem).atLeastOnce();
    expect(mockRuntime.getFunctionalUnit(invalidUnitName)).andReturn(null).atLeastOnce();

    return mockRuntime;
  }

  private OpalShell createMockShellForInvalidFunctionalUnit(String unitName) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Functional unit '%s' does not exist. Cannot decrypt.\n", unitName);

    return mockShell;
  }

  private DecryptCommandOptions createMockOptionsForDefaultOutputDirectory(String unitName) {
    DecryptCommandOptions mockOptions = createMock(DecryptCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.isOutput()).andReturn(false).atLeastOnce();
    expect(mockOptions.isFiles()).andReturn(true).atLeastOnce();
    expect(mockOptions.getFiles()).andReturn(Arrays.asList("encrypted.zip")).atLeastOnce();

    return mockOptions;
  }

  private IOpalRuntime createMockRuntimeForDefaultOutputDirectory(OpalFileSystem mockFileSystem, String unitName) {
    IOpalRuntime mockRuntime = createMock(IOpalRuntime.class);
    expect(mockRuntime.getFileSystem()).andReturn(mockFileSystem).atLeastOnce();
    expect(mockRuntime.getFunctionalUnit(unitName)).andReturn(new FunctionalUnit(unitName, null)).atLeastOnce();

    return mockRuntime;
  }

  private OpalShell createMockShellForDefaultOutputDirectory(String filePath) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Decrypting input file %s\n", filePath);

    return mockShell;
  }
}
