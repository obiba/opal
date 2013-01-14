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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.core.crypt.KeyProviderException;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.NonExistentVariableEntitiesException;
import org.obiba.opal.core.support.TimedExecution;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@CommandUsage(description = "Imports one or more Onyx data files into a datasource.",
    syntax = "Syntax: import [--unit NAME] [--force] [--source NAME] [--tables NAMES] --destination NAME [--archive FILE] [FILES]")
public class ImportCommand extends AbstractOpalRuntimeDependentCommand<ImportCommandOptions> {

  //
  // AbstractContextLoadingCommand Methods
  //

  private static final Logger log = LoggerFactory.getLogger(ImportCommand.class);

  @Autowired
  private ImportService importService;

  @Override
  public int execute() {
    int errorCode = 0;

    if(options.isUnit() && getFunctionalUnitService().hasFunctionalUnit(options.getUnit()) == false) {
      getShell().printf("Functional unit '%s' does not exist.\n", options.getUnit());
      return 1; // error!
    }

    TimedExecution timedExecution = new TimedExecution().start();

    List<FileObject> filesToImport = getFilesToImport();
    errorCode = executeImports(filesToImport);

    if(options.isSource() == false & options.isTables() == false & filesToImport.isEmpty()) {
      // TODO: Should this be considered success or an error? Will treat as an error for now.
      getShell().printf("No file, source or tables provided. Import canceled.\n");
      errorCode = 1;
    } else if(errorCode != 0) {
      getShell().printf("Import failed.\n");
      log.info("Import failed in {}", timedExecution.end().formatExecutionTime());
    } else {
      getShell().printf("Import done.\n");
      log.info("Import succeed in {}", timedExecution.end().formatExecutionTime());
    }
    return errorCode;
  }

  private int executeImports(List<FileObject> filesToImport) {
    int errorCode = 0;

    if(options.isSource()) {
      errorCode = importFromDatasource(filesToImport.isEmpty() ? null : filesToImport.get(0));
    } else if(options.isTables()) {
      errorCode = importFromTables(filesToImport.isEmpty() ? null : filesToImport.get(0));
    } else if(!filesToImport.isEmpty()) {
      errorCode = importFiles(filesToImport);
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
    StringBuilder sb = new StringBuilder();

    sb.append("import");
    if(options.isUnit()) {
      sb.append(" --unit ");
      sb.append(options.getUnit());
    }
    if(options.isSource()) {
      sb.append(" --source ");
      sb.append(options.getSource());
    }
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

  private int importFiles(Collection<FileObject> filesToImport) {
    int errorCode = 0;

    getShell().printf("Importing %d file%s :\n", filesToImport.size(), filesToImport.size() > 1 ? "s" : "");
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
   *         by continuing with the next file)
   */
  @SuppressWarnings("PMD.NcssMethodCount")
  private int importFile(FileObject file) {
    int errorCode = 1; // critical error (or interruption)!
    getShell().printf("  Importing file: %s ...\n", file.getName().getPath());
    try {
      importService.importData(getUnitName(), file, options.getDestination(), options.isForce(), options.isIgnore());
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
    } catch(RuntimeException ex) {
      runtimeExceptionHandler(ex);
    }

    return errorCode;
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private int importFromDatasource(FileObject file) {
    int errorCode = 1; // critical error (or interruption)!
    getShell().printf("  Importing datasource: %s ...\n", options.getSource());
    try {
      importService.importData(getUnitName(), options.getSource(), options.getDestination(), options.isForce(),
          options.isIgnore());
      if(file != null) archive(file);
      errorCode = 0; // success!
    } catch(NoSuchDatasourceException ex) {
      getShell().printf("Datasource '%s' does not exist. Cannot import.\n", ex.getDatasourceName());
    } catch(KeyProviderException ex) {
      getShell().printf("Decryption exception: %s\n", ex.getMessage());
    } catch(IOException ex) {
      // Report an error and continue with the next file.
      getShell().printf("Unrecoverable import exception: %s\n", ex.getMessage());
      errorCode = 2; // non-critical error!
    } catch(InterruptedException ex) {
      // Report the interrupted and continue; the test for interruption will detect this condition.
      getShell().printf("Thread interrupted");
    } catch(RuntimeException ex) {
      runtimeExceptionHandler(ex);
    }
    return errorCode;
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private int importFromTables(FileObject file) {
    int errorCode = 1; // critical error (or interruption)!
    getShell().printf("  Importing tables: %s ...\n", getTableNames());
    try {
      importService.importData(getUnitName(), options.getTables(), options.getDestination(), options.isForce(),
          options.isIgnore());
      if(file != null) archive(file);
      errorCode = 0; // success!
    } catch(NoSuchDatasourceException ex) {
      getShell().printf("'%s'. Cannot import.\n", ex.getMessage());
    } catch(NoSuchValueTableException ex) {
      getShell().printf("'%s'. Cannot import.\n", ex.getMessage());
    } catch(KeyProviderException ex) {
      getShell().printf("Decryption exception: %s\n", ex.getMessage());
    } catch(IOException ex) {
      // Report an error and continue with the next file.
      getShell().printf("Unrecoverable import exception: %s\n", ex.getMessage());
      errorCode = 2; // non-critical error!
    } catch(InterruptedException ex) {
      // Report the interrupted and continue; the test for interruption will detect this condition.
      getShell().printf("Thread interrupted");
    } catch(RuntimeException ex) {
      runtimeExceptionHandler(ex);
    }
    return errorCode;
  }

  private String getUnitName() {
    String unitName = options.isUnit() ? options.getUnit() : null;
    printUnitOptions();
    return unitName;
  }

  private void printUnitOptions() {
    if(options.isUnit()) {
      getShell().printf("  Importing in unit: %s\n", options.getUnit());
      getShell().printf("  Allow identifier generation: %s\n", options.isForce());
      if(options.isForce() == false) {
        getShell().printf("  Ignore participants with unknown identifier: %s\n", options.isIgnore());
      }
    }
  }

  private String getTableNames() {
    return Joiner.on(", ").join(Iterables.transform(options.getTables(), new Function<String, String>() {

      @Override
      public String apply(String input) {
        return input.substring(input.indexOf('.') + 1);
      }
    }));
  }

  private void runtimeExceptionHandler(RuntimeException ex) {
    log.error("Runtime error while importing data", ex);
    if(ex.getCause() != null && ex.getCause() instanceof NonExistentVariableEntitiesException) {
      getShell().printf(
          "Datasource '%s' cannot be imported 'as-is'. It contains the following entity ids which are not present as public identifiers in the keys database. %s\n",
          options.getSource(), ((NonExistentVariableEntitiesException) ex.getCause()).getNonExistentIdentifiers());
    } else {
      printThrowable(ex, false);
    }
  }

  private void printThrowable(Throwable ex, boolean withStack) {
    getShell().printf(ex.getMessage());
    if(withStack) {
      for(StackTraceElement elem : ex.getStackTrace()) {
        getShell().printf(elem.toString());
      }
    }
    if(ex.getCause() != null) {
      printThrowable(ex.getCause(), withStack);
    }
  }

  private void archive(FileObject file) throws IOException {
    if(!options.isArchive()) {
      return;
    }

    try {
      FileObject archiveDir;
      archiveDir = isRelativeFilePath(options.getArchive()) //
          ? getFileInUnitDirectory(options.getArchive()) //
          : getFile(options.getArchive());
      archiveDir.createFolder();

      FileObject archiveFile = archiveDir.resolveFile(file.getName().getBaseName());
      file.moveTo(archiveFile);
    } catch(FileSystemException ex) {
      throw new IOException("Failed to archive file " + file.getName().getPath());
    }
  }

  /**
   * The user may have supplied a file or a list of files at the command line to be imported. If the user has not
   * supplied a file (or files) but has supplied a unit, then all the files that are available in the associated unit
   * directory will be imported.
   * <p/>
   * If data is imported from another datasource then it would seem that there is no reason to specify a file. But it is
   * permitted that a user can specify one file in this case. The specified file is used only for archiving purposes
   * only.
   *
   * @return A List of files to be imported. The List may be empty.
   */
  private List<FileObject> getFilesToImport() {
    List<FileObject> filesToImport = null;
    if(options.isFiles()) {
      filesToImport = resolveFiles(options.getFiles());
    } else {
      if(options.isUnit() && !options.isSource()) {
        // If we're importing from a datasource we don't want to read files from the unit directory.
        try {
          filesToImport = getFilesInFolder(getFunctionalUnitService().getUnitDirectory(options.getUnit()));
        } catch(IOException ex) {
          throw new RuntimeException(ex);
        }
      }
    }
    if(filesToImport == null) return Lists.newArrayList();
    return filesToImport;
  }

  private List<FileObject> resolveFiles(Iterable<String> filePaths) {
    List<FileObject> files = new ArrayList<FileObject>();
    FileObject file;
    for(String filePath : filePaths) {
      try {
        file = getFileToImport(filePath);
        if(file == null || file.exists() == false) {
          getShell().printf("'%s' does not exist\n", filePath);
          continue;
        }
        addFile(files, file);
      } catch(FileSystemException e) {
        getShell().printf("Cannot resolve the following path : %s, skipping import...");
        log.warn("Cannot resolve the following path : {}, skipping import...", e);
      }
    }
    return files;
  }

  private FileObject getFileToImport(String filePath) throws FileSystemException {
    return isRelativeFilePath(filePath) ? getFileInUnitDirectory(filePath) : getFile(filePath);
  }

  private void addFile(Collection<FileObject> files, FileObject file) throws FileSystemException {
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
        return file.getFile().getType() == FileType.FILE && "zip"
            .equals(file.getFile().getName().getExtension().toLowerCase());
      }
    });
    return Arrays.asList(filesInDir);
  }

  private FileObject getFileInUnitDirectory(String filePath) throws FileSystemException {
    if(options.isUnit()) {
      FileObject unitDir = getFunctionalUnitService().getUnitDirectory(options.getUnit());
      return unitDir.resolveFile(filePath);
    } else {
      return null;
    }
  }

  private boolean isRelativeFilePath(String filePath) {
    return !filePath.startsWith("/");
  }

}
