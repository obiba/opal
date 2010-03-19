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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Imports one or more Onyx data files into a datasource.", syntax = "Syntax: import --unit NAME --destination NAME [_FILE_...]")
public class ImportCommand extends AbstractOpalRuntimeDependentCommand<ImportCommandOptions> {

  //
  // AbstractContextLoadingCommand Methods
  //

  private static final Logger log = LoggerFactory.getLogger(ImportCommand.class);

  @Autowired
  private ImportService importService;

  public void execute() {
    if(getOpalRuntime().getFunctionalUnit(options.getUnit()) == null) {
      getShell().printf("Functional unit '%s' does not exist. Cannot decrypt.\n", options.getUnit());
      return;
    }

    List<FileObject> filesToImport = null;
    if(options.isFiles()) {
      filesToImport = resolveFiles(options.getFiles());
    } else {
      try {
        filesToImport = getFilesInFolder(getOpalRuntime().getUnitDirectory(options.getUnit()));
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    if(!filesToImport.isEmpty()) {
      importFiles(filesToImport);
    } else {
      getShell().printf("No file found. Import canceled.\n");
    }
  }

  //
  // Methods
  //

  public void setImportService(ImportService importService) {
    this.importService = importService;
  }

  private void importFiles(List<FileObject> filesToImport) {
    getShell().printf("Importing %d file%s :\n", filesToImport.size(), (filesToImport.size() > 1 ? "s" : ""));
    for(FileObject file : filesToImport) {
      getShell().printf("  %s\n", file.getName().getPath());
      try {
        importService.importData(options.getUnit(), options.getDestination(), file);
      } catch(NoSuchFunctionalUnitException ex) {
        getShell().printf("Functional unit '%s' does not exist. Cannot import.\n", ex.getUnitName());
        break;
      } catch(NoSuchDatasourceException ex) {
        getShell().printf("Destination datasource '%s' does not exist. Cannot import.\n", ex.getDatasourceName());
        break;
      } catch(IOException ex) {
        // Report an error and continue with the next file.
        getShell().printf("Unrecoverable import exception: %s\n", ex.getMessage());
        ex.printStackTrace(System.err);
        continue;
      }
    }
  }

  private List<FileObject> resolveFiles(List<String> filePaths) {
    List<FileObject> files = new ArrayList<FileObject>();
    FileObject file;
    FileType fileType;
    for(String filePath : filePaths) {

      try {
        if(isRelativeFilePath(filePath)) {
          file = getFileInUnitDirectory(filePath);
        } else {
          file = getFile(filePath);
        }

        if(file.exists() == false) {
          getShell().printf("'%s' does not exist\n", filePath);
          continue;
        }
        fileType = file.getType();
        if(fileType == FileType.FOLDER) {
          files.addAll(getFilesInFolder(file));
        } else if(fileType == FileType.FILE) {
          files.add(file);
        }
      } catch(FileSystemException e) {
        getShell().printf("Cannot resolve the following path : %s, skipping import...");
        log.warn("Cannot resolve the following path : {}, skipping import...", e);
        continue;
      }

    }
    return files;
  }

  private List<FileObject> getFilesInFolder(FileObject file) throws FileSystemException {
    FileObject[] filesInDir = file.findFiles(new FileSelector() {
      @Override
      public boolean traverseDescendents(FileSelectInfo file) throws Exception {
        return true;
      }

      @Override
      public boolean includeFile(FileSelectInfo file) throws Exception {
        return file.getFile().getType() == FileType.FILE && file.getFile().getName().getExtension().toLowerCase().equals("zip");
      }
    });
    return Arrays.asList(filesInDir);
  }

  private boolean isRelativeFilePath(String filePath) {
    return !(new File(filePath).isAbsolute());
  }

  private FileObject getFileInUnitDirectory(String filePath) throws FileSystemException {
    FileObject unitDir = getOpalRuntime().getUnitDirectory(options.getUnit());
    return unitDir.resolveFile(filePath);
  }
}
