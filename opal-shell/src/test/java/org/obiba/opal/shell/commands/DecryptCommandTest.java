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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.DecryptService;
import org.obiba.opal.core.unit.FunctionalUnitService;
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

    OpalRuntime mockRuntime = createMockRuntime(mockFileSystem);
    FunctionalUnitService mockUnitService = createMock(FunctionalUnitService.class);
    expect(mockUnitService.getUnitDirectory("my-unit")).andReturn(mockUnitDir).atLeastOnce();

    OpalShell mockShell = createMockShellForInvalidOutputDirectory();

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockUnitService, mockShell);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime, mockUnitService);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockShell, mockUnitService);
  }

  @Test
  public void testPrintsErrorIfNoFileSpecified() throws FileSystemException {
    DecryptCommandOptions mockOptions = createMockOptionsForNoFileSpecified();
    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);
    OpalRuntime mockRuntime = createMockRuntime(mockFileSystem);
    OpalShell mockShell = createMockShellForNoFileSpecified();
    FunctionalUnitService mockUnitService = createMock(FunctionalUnitService.class);

    replay(mockOptions, mockFileSystem, mockRuntime, mockShell, mockUnitService);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime, mockUnitService);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystem, mockRuntime, mockShell, mockUnitService);
  }

  @Test
  public void testPrintsErrorOnInvalidFunctionalUnit() throws FileSystemException {
    DecryptCommandOptions mockOptions = createMockOptionsForInvalidFunctionalUnit("bogus");
    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);

    OpalRuntime mockRuntime = createMockRuntime(mockFileSystem);
    OpalShell mockShell = createMockShellForInvalidFunctionalUnit("bogus");

    FunctionalUnitService mockUnitService = createMock(FunctionalUnitService.class);
    expect(mockUnitService.hasFunctionalUnit("bogus")).andReturn(false);

    replay(mockOptions, mockFileSystem, mockRuntime, mockShell, mockUnitService);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime, mockUnitService);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystem, mockRuntime, mockShell, mockUnitService);
  }

  @Test
  public void testOutputDirectoryDefaultsToOpalFileSystemRoot() throws IOException {
    String unitName = "my-unit";
    DecryptCommandOptions mockOptions = createMockOptionsForDefaultOutputDirectory(unitName);

    FileObject inputFile = createMockFile("encrypted.zip", true, true);
    FileObject outputFile = createMockFile("encrypted-plaintext.zip", false, true);

    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    expect(mockFileSystemRoot.resolveFile("encrypted-plaintext.zip")).andReturn(outputFile).atLeastOnce();

    FileObject mockUnitDir = createMockFile(unitName, true, false);
    expect(mockUnitDir.resolveFile("encrypted.zip")).andReturn(inputFile).atLeastOnce();

    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);
    expect(mockFileSystem.getLocalFile(outputFile)).andReturn(new File("target", "encrypted.zip")).atLeastOnce();

    OpalRuntime mockRuntime = createMockRuntime(mockFileSystem);

    FunctionalUnitService mockUnitService = createMock(FunctionalUnitService.class);

    expect(mockUnitService.hasFunctionalUnit(unitName)).andReturn(true).atLeastOnce();
    expect(mockUnitService.getUnitDirectory(unitName)).andReturn(mockUnitDir).atLeastOnce();

    OpalShell mockShell = createMockShellForDefaultOutputDirectory("encrypted.zip");

    DecryptService mockDecryptService = createMock(DecryptService.class);
    mockDecryptService.decryptData(unitName, DecryptCommand.DECRYPT_DATASOURCE_NAME, inputFile);

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockShell, mockDecryptService, mockUnitService);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime, mockUnitService);
    decryptCommand.setDecryptService(mockDecryptService);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystemRoot, mockFileSystem, mockUnitDir, mockRuntime, mockShell, mockDecryptService, mockUnitService);
  }

  //
  // Helper Methods
  //

  private DecryptCommand createDecryptCommand(final OpalRuntime mockRuntime, final FunctionalUnitService service) {
    return new DecryptCommand() {
      @Override
      protected OpalRuntime getOpalRuntime() {
        return mockRuntime;
      }

      @Override
      protected FunctionalUnitService getFunctionalUnitService() {
        return service;
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

  private OpalRuntime createMockRuntime(OpalFileSystem mockFileSystem) {
    OpalRuntime mockRuntime = createMock(OpalRuntime.class);
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

  private OpalShell createMockShellForDefaultOutputDirectory(String filePath) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Decrypting input file %s\n", filePath);

    return mockShell;
  }
}
