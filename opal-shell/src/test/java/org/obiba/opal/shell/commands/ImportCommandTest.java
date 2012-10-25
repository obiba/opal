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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;

/**
 * Unit tests for {@link ImportCommand}.
 */
public class ImportCommandTest {
  //
  // Test Methods
  //

  @Test
  public void testImportIntoOpalInstanceNotAllowed() {
    ImportCommandOptions mockOptions = createMock(ImportCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(FunctionalUnit.OPAL_INSTANCE).atLeastOnce();

    OpalShell mockShell = createMockShellForOpalInstanceNotAllowed();

    OpalRuntime mockRuntime = createMock(OpalRuntime.class);
    FunctionalUnitService mockService = createMock(FunctionalUnitService.class);
    expect(mockService.hasFunctionalUnit(FunctionalUnit.OPAL_INSTANCE)).andReturn(false).atLeastOnce();

    test(mockOptions, mockShell, mockRuntime, mockService, null);

  }

  @Test
  public void testImportIntoBogusUnitNotAllowed() {
    ImportCommandOptions mockOptions = createMock(ImportCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn("bogus").atLeastOnce();

    OpalShell mockShell = createMockShellForBogusUnitNotAllowed("bogus");

    OpalRuntime mockRuntime = createMock(OpalRuntime.class);

    FunctionalUnitService mockService = createMock(FunctionalUnitService.class);
    expect(mockService.hasFunctionalUnit("bogus")).andReturn(false).atLeastOnce();

    test(mockOptions, mockShell, mockRuntime, mockService, null);
  }

  /**
   * When the import command is executed with a relative file path, the path is treated as relative to the specified
   * functional unit's directory.
   */
  @Test
  public void testRelativePathIsRelativeToUnitDirectory() throws IOException, InterruptedException {
    ImportCommandOptions mockOptions = createMockOptionsForRelativePathImport("my-unit", "opal-data", "test.zip");
    FileObject mockFile = createMockFileForRelativePathImport("my-unit", "test.zip");
    FileObject mockUnitDir = createMockUnitDirectoryForRelativePathImport(mockFile, "test.zip");
    OpalRuntime mockRuntime = createMock(OpalRuntime.class);
    OpalShell mockShell = createMockShellForRelativePathImport("my-unit", "test.zip");
    ImportService mockImportService = createMock(ImportService.class);
    mockImportService.importData("my-unit", mockFile, "opal-data", true, false);
    FunctionalUnitService mockService = createMockUnitService(mockUnitDir, "my-unit");

    test(mockOptions, mockShell, mockRuntime, mockService, mockImportService, mockUnitDir, mockFile);
  }

  /**
   * When the import command is executed with no file specified, all files (*.zip) in the functional unit's directory
   * are imported.
   */
  @Test
  public void testWhenNoFileIsSpecifiedAllFilesInUnitDirectoryAreImported() throws IOException, InterruptedException {
    ImportCommandOptions mockOptions = createMockOptionsForImportWithNoFile("my-unit", "opal-data");
    FileObject[] mockFilesInUnitDir = createMockFilesInUnitDirectory("my-unit", "test1.zip", "test2.zip");
    FileObject mockUnitDir = createMockUnitDirectoryForImportWithNoFile(mockFilesInUnitDir);
    OpalRuntime mockRuntime = createMock(OpalRuntime.class);
    OpalShell mockShell = createMockShellForImportWithNoFile("my-unit", "test1.zip", "test2.zip");
    FunctionalUnitService mockService = createMockUnitService(mockUnitDir, "my-unit");
    ImportService mockImportService = createMock(ImportService.class);
    for(FileObject mockFile : mockFilesInUnitDir) {
      mockImportService.importData("my-unit", mockFile, "opal-data", true, false);
    }

    test(mockOptions, mockShell, mockRuntime, mockService, mockImportService, mockUnitDir);
  }

  private void test(ImportCommandOptions mockOptions, OpalShell mockShell, OpalRuntime mockRuntime, FunctionalUnitService mockUnitService, ImportService mockImportService, Object... otherMocks) {

    replay(mockOptions, mockShell, mockRuntime, mockUnitService);
    if(mockImportService != null) replay(mockImportService);
    if(otherMocks != null) {
      for(Object mock : otherMocks)
        replay(mock);
    }

    ImportCommand importCommand = createImportCommand(mockRuntime, mockUnitService);
    importCommand.setImportService(mockImportService);
    importCommand.setOptions(mockOptions);
    importCommand.setShell(mockShell);
    importCommand.execute();

    verify(mockOptions, mockShell, mockRuntime, mockUnitService);
    if(mockImportService != null) verify(mockImportService);
    if(otherMocks != null) verify(otherMocks);
  }

  //
  // Methods
  //

  private ImportCommand createImportCommand(final OpalRuntime mockRuntime, final FunctionalUnitService service) {
    return new ImportCommand() {
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

  private OpalShell createMockShellForOpalInstanceNotAllowed() {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Functional unit '%s' does not exist.\n", FunctionalUnit.OPAL_INSTANCE);

    return mockShell;
  }

  private OpalShell createMockShellForBogusUnitNotAllowed(String unitName) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Functional unit '%s' does not exist.\n", unitName);

    return mockShell;
  }

  private ImportCommandOptions createMockOptionsForRelativePathImport(String unitName, String destination, String relativeFilePath) {
    ImportCommandOptions mockOptions = createMock(ImportCommandOptions.class);

    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getDestination()).andReturn(destination).atLeastOnce();
    expect(mockOptions.isFiles()).andReturn(true).atLeastOnce();
    expect(mockOptions.getFiles()).andReturn(Arrays.asList(relativeFilePath)).atLeastOnce();
    expect(mockOptions.isForce()).andReturn(true).atLeastOnce();
    expect(mockOptions.isIgnore()).andReturn(false).atLeastOnce();
    expect(mockOptions.isArchive()).andReturn(false).atLeastOnce();
    expect(mockOptions.isSource()).andReturn(false).atLeastOnce();
    expect(mockOptions.isTables()).andReturn(false).atLeastOnce();

    return mockOptions;
  }

  private FunctionalUnitService createMockUnitService(FileObject mockUnitDir, String unitName) throws FileSystemException {
    FunctionalUnitService mockService = createMock(FunctionalUnitService.class);
    expect(mockService.hasFunctionalUnit(unitName)).andReturn(true).atLeastOnce();
    expect(mockService.getFunctionalUnit(unitName)).andReturn(new FunctionalUnit(unitName, null)).anyTimes();
    expect(mockService.getUnitDirectory(unitName)).andReturn(mockUnitDir).atLeastOnce();

    return mockService;
  }

  private OpalShell createMockShellForRelativePathImport(String unitName, String relativeFilePath) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Importing %d file%s :\n", 1, "");
    mockShell.printf("  Importing file: %s ...\n", "units" + "/" + unitName + "/" + relativeFilePath);
    mockShell.printf("  Importing in unit: %s\n", unitName);
    mockShell.printf("  Allow identifier generation: %s\n", true);
    mockShell.printf("Import done.\n");

    return mockShell;
  }

  private FileObject createMockFileForRelativePathImport(String unitName, String relativeFilePath) throws IOException {
    FileObject mockFile = createMock(FileObject.class);
    expect(mockFile.exists()).andReturn(true).atLeastOnce();
    expect(mockFile.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(mockFile.getName()).andReturn(createMockFileName(unitName, relativeFilePath)).atLeastOnce();

    return mockFile;
  }

  private FileObject createMockUnitDirectoryForRelativePathImport(FileObject mockFile, String relativeFilePath) throws IOException {
    FileObject mockUnitDir = createMock(FileObject.class);
    expect(mockUnitDir.resolveFile(relativeFilePath)).andReturn(mockFile);

    return mockUnitDir;
  }

  private FileName createMockFileName(String unitName, String relativeFilePath) {
    FileName mockFileName = createMock(FileName.class);
    expect(mockFileName.getPath()).andReturn("units" + "/" + unitName + "/" + relativeFilePath).atLeastOnce();
    replay(mockFileName);

    return mockFileName;
  }

  private ImportCommandOptions createMockOptionsForImportWithNoFile(String unitName, String destination) {
    ImportCommandOptions mockOptions = createMock(ImportCommandOptions.class);

    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getDestination()).andReturn(destination).atLeastOnce();
    expect(mockOptions.isFiles()).andReturn(false).atLeastOnce();
    expect(mockOptions.isForce()).andReturn(true).atLeastOnce();
    expect(mockOptions.isIgnore()).andReturn(false).atLeastOnce();
    expect(mockOptions.isArchive()).andReturn(false).atLeastOnce();
    expect(mockOptions.isSource()).andReturn(false).atLeastOnce();
    expect(mockOptions.isTables()).andReturn(false).atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForImportWithNoFile(String unitName, String... fileNames) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Importing %d file%s :\n", 2, "s");

    for(String fileName : fileNames) {
      mockShell.printf("  Importing file: %s ...\n", "units" + "/" + unitName + "/" + fileName);
    }
    mockShell.printf("  Importing in unit: %s\n", unitName);
    mockShell.printf("  Importing in unit: %s\n", unitName);
    mockShell.printf("  Allow identifier generation: %s\n", true);
    mockShell.printf("  Allow identifier generation: %s\n", true);

    mockShell.printf("Import done.\n");

    return mockShell;
  }

  private FileObject createMockUnitDirectoryForImportWithNoFile(FileObject[] filesInUnitDir) throws IOException {
    FileObject mockUnitDir = createMock(FileObject.class);
    expect(mockUnitDir.findFiles((FileSelector) anyObject())).andReturn(filesInUnitDir);

    return mockUnitDir;
  }

  private FileObject[] createMockFilesInUnitDirectory(String unitName, String... fileNames) throws IOException {
    FileObject[] filesInUnitDir = new FileObject[fileNames.length];

    for(int i = 0; i < fileNames.length; i++) {
      FileObject mockFile = createMock(FileObject.class);
      expect(mockFile.exists()).andReturn(true).atLeastOnce();
      expect(mockFile.getType()).andReturn(FileType.FILE).atLeastOnce();
      expect(mockFile.getName()).andReturn(createMockFileName(unitName, fileNames[i])).atLeastOnce();
      replay(mockFile);

      filesInUnitDir[i] = mockFile;
    }

    return filesInUnitDir;
  }
}
