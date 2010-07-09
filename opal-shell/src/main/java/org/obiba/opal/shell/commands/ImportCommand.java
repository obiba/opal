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
import org.obiba.opal.core.crypt.KeyProviderException;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Imports one or more Onyx data files into a datasource.", syntax = "Syntax: import --unit NAME --destination NAME [--archive FILE] [_FILE_...]")
public class ImportCommand extends AbstractOpalRuntimeDependentCommand<ImportCommandOptions> {

  //
  // AbstractContextLoadingCommand Methods
  //

  private static final Logger log = LoggerFactory.getLogger(ImportCommand.class);

  @Autowired
  private ImportService importService;

  public int execute() {
    int errorCode = 0;

    if(getOpalRuntime().getFunctionalUnit(options.getUnit()) == null) {
      getShell().printf("Functional unit '%s' does not exist.\n", options.getUnit());
      return 1; // error!
    }

    List<FileObject> filesToImport = getFilesToImport();

    if(!filesToImport.isEmpty()) {
      errorCode = importFiles(filesToImport);
    } else {
      // TODO: Should this be considered success or an error? Will treat as an error for now.
      getShell().printf("No file found. Import canceled.\n");
      errorCode = 1;
    }

    return errorCode;
  }

  //
  // Methods
  //

  public void setImportService(ImportService importService) {
    this.importService = importService;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("import");
    sb.append(" --unit ");
    sb.append(options.getUnit());
    sb.append(" --destination ");
    sb.append(options.getDestination());

    if(options.isArchive()) {
      sb.append(" --archive ");
      sb.append(options.getArchive());
    }

    if(options.isFiles()) {
      for(String file : options.getFiles()) {
        sb.append(' ');
        sb.append(file);
      }
    }

    return sb.toString();
  }

  private int importFiles(List<FileObject> filesToImport) {
    int errorCode = 0;

    getShell().printf("Importing %d file%s :\n", filesToImport.size(), (filesToImport.size() > 1 ? "s" : ""));
    for(FileObject file : filesToImport) {
      if(Thread.interrupted()) {
        errorCode = 1;
        break;
      }

      // If lastErrorCode == 0 (success), do NOT update errorCode since it might have
      // been equal to 2 (non-critical error). We want to remember that there was an earlier error.
      int lastErrorCode = importFile(file);
      if(lastErrorCode == 1) {
        errorCode = lastErrorCode;
        break;
      } else if(lastErrorCode == 2) {
        errorCode = lastErrorCode;
      }
    }

    return errorCode;
  }

  /**
   * Imports the specified file. Called by <code>importFiles</code>.
   * 
   * @param file file to import
   * @return error code (<code>0</code> on success, <code>1</code> on critical errors, <code>2</code> on errors handled
   * by continuing with the next file)
   */
  private int importFile(FileObject file) {
    int errorCode = 1; // critical error (or interruption)!

    getShell().printf("  %s\n", file.getName().getPath());
    try {
      importService.importData(options.getUnit(), options.getDestination(), file);
      archive(file);
      errorCode = 0; // success!
    } catch(NoSuchFunctionalUnitException ex) {
      getShell().printf("Functional unit '%s' does not exist. Cannot import.\n", ex.getUnitName());
    } catch(NoSuchDatasourceException ex) {
      getShell().printf("Destination datasource '%s' does not exist. Cannot import.\n", ex.getDatasourceName());
    } catch(KeyProviderException ex) {
      getShell().printf("Decryption exception: %s\n", ex.getMessage());
    } catch(IOException ex) {
      // Report an error and continue with the next file.
      getShell().printf("Unrecoverable import exception: %s\n", ex.getMessage());
      errorCode = 2; // non-critical error!
    } catch(InterruptedException ex) {
      // Report the interrupted and continue; the test for interruption will detect this condition.
      getShell().printf("Thread interrupted");
    }

    return errorCode;
  }

  private void archive(FileObject file) throws IOException {
    if(!options.isArchive()) {
      return;
    }

    try {
      FileObject archiveDir;
      if(isRelativeFilePath(options.getArchive())) {
        archiveDir = getFileInUnitDirectory(options.getArchive());
      } else {
        archiveDir = getFile(options.getArchive());
      }
      archiveDir.createFolder();

      FileObject archiveFile = archiveDir.resolveFile(file.getName().getBaseName());
      file.moveTo(archiveFile);
    } catch(FileSystemException ex) {
      throw new IOException("Failed to archive file " + file.getName().getPath());
    }
  }

  private List<FileObject> getFilesToImport() {
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
    return filesToImport;
  }

  private List<FileObject> resolveFiles(List<String> filePaths) {
    List<FileObject> files = new ArrayList<FileObject>();
    FileObject file;
    for(String filePath : filePaths) {
      try {
        file = getFileToImport(filePath);
        if(file.exists() == false) {
          getShell().printf("'%s' does not exist\n", filePath);
          continue;
        }
        addFile(files, file);
      } catch(FileSystemException e) {
        getShell().printf("Cannot resolve the following path : %s, skipping import...");
        log.warn("Cannot resolve the following path : {}, skipping import...", e);
        continue;
      }
    }
    return files;
  }

  private FileObject getFileToImport(String filePath) throws FileSystemException {
    FileObject file;
    if(isRelativeFilePath(filePath)) {
      file = getFileInUnitDirectory(filePath);
    } else {
      file = getFile(filePath);
    }
    return file;
  }

  private void addFile(List<FileObject> files, FileObject file) throws FileSystemException {
    FileType fileType = file.getType();
    if(fileType == FileType.FOLDER) {
      files.addAll(getFilesInFolder(file));
    } else if(fileType == FileType.FILE) {
      files.add(file);
    }
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

  private FileObject getFileInUnitDirectory(String filePath) throws FileSystemException {
    FileObject unitDir = getOpalRuntime().getUnitDirectory(options.getUnit());
    return unitDir.resolveFile(filePath);
  }

  private boolean isRelativeFilePath(String filePath) {
    return !(new File(filePath).isAbsolute());
  }
}
