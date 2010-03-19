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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.IOpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.DecryptCommandOptions;

/**
 * Unit tests for {@link DecryptCommand}.
 */
public class DecryptCommandTest {
  //
  // Test Methods
  //

  @Test
  public void testPrintsErrorOnInvalidOutputDirectory() throws FileSystemException {
    DecryptCommandOptions mockOptions = createMockOptionsForInvalidOutputDirectory("my-unit", "$%@%$@#");
    OpalFileSystem mockFileSystem = createMockFileSystem("$%@%$@#");
    IOpalRuntime mockRuntime = createMockRuntime(mockFileSystem);
    OpalShell mockShell = createMockShellForInvalidOutputDirectory();

    replay(mockOptions, mockFileSystem, mockRuntime, mockShell);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystem, mockRuntime, mockShell);
  }

  @Test
  public void testPrintsErrorIfNoFileSpecified() throws FileSystemException {
    DecryptCommandOptions mockOptions = createMockOptionsForNoFileSpecified();
    OpalFileSystem mockFileSystem = createMockFileSystem("$%@%$@#");
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
    DecryptCommandOptions mockOptions = createMockOptionsForInvalidFunctionalUnit("my-unit");
    OpalFileSystem mockFileSystem = createMockFileSystem("root");
    IOpalRuntime mockRuntime = createMockRuntimeForInvalidFunctionalUnit(mockFileSystem, "my-unit");
    OpalShell mockShell = createMockShellForInvalidFunctionalUnit("my-unit");

    replay(mockOptions, mockFileSystem, mockRuntime, mockShell);

    DecryptCommand decryptCommand = createDecryptCommand(mockRuntime);
    decryptCommand.setOptions(mockOptions);
    decryptCommand.setShell(mockShell);
    decryptCommand.execute();

    verify(mockOptions, mockFileSystem, mockRuntime, mockShell);
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
    expect(mockOptions.isOutput()).andReturn(true).atLeastOnce();
    expect(mockOptions.getOutput()).andReturn(invalidOutputDirPath).atLeastOnce();

    return mockOptions;
  }

  private OpalFileSystem createMockFileSystem(String rootPath) throws FileSystemException {
    OpalFileSystem mockFileSystem = createMock(OpalFileSystem.class);
    expect(mockFileSystem.getRoot()).andReturn(createMockFileSystemRoot(rootPath)).atLeastOnce();

    return mockFileSystem;
  }

  private FileObject createMockFileSystemRoot(String invalidOutputDirPath) throws FileSystemException {
    FileObject mockFileSystemRoot = createMock(FileObject.class);
    expect(mockFileSystemRoot.resolveFile(invalidOutputDirPath)).andThrow(new FileSystemException(invalidOutputDirPath));

    replay(mockFileSystemRoot);

    return mockFileSystemRoot;
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
}
