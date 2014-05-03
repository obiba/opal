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

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.core.crypt.KeyProviderException;
import org.obiba.opal.core.service.DataImportService;
import org.obiba.opal.core.service.NonExistentVariableEntitiesException;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.obiba.opal.shell.commands.CommandResultCode.CRITICAL_ERROR;
import static org.obiba.opal.shell.commands.CommandResultCode.NON_CRITICAL_ERROR;
import static org.obiba.opal.shell.commands.CommandResultCode.SUCCESS;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
@CommandUsage(description = "Imports one or more Onyx data files into a datasource.",
    syntax = "Syntax: import [--unit NAME] [--incremental] [--force] [--source NAME] [--tables NAMES] --destination NAME [--archive FILE] [FILES]")
public class ImportCommand extends AbstractOpalRuntimeDependentCommand<ImportCommandOptions> {

  //
  // AbstractContextLoadingCommand Methods
  //

  private static final Logger log = LoggerFactory.getLogger(ImportCommand.class);

  @Autowired
  private DataImportService dataImportService;

  @Override
  public int execute() {
    int errorCode;

    Stopwatch stopwatch = Stopwatch.createStarted();

    List<FileObject> filesToImport = getFilesToImport();
    errorCode = executeImports(filesToImport);

    if(!options.isSource() & !options.isTables() & filesToImport.isEmpty()) {
      // Should this be considered success or an error? Will treat as an error for now.
      getShell().printf("No file, source or tables provided. Import canceled.\n");
      errorCode = CRITICAL_ERROR;
    } else if(errorCode != SUCCESS) {
      getShell().printf("Import failed.\n");
      log.info("Import failed in {}", stopwatch.stop());
    } else {
      getShell().printf("Import done.\n");
      log.info("Import succeed in {}", stopwatch.stop());
    }
    return errorCode;
  }

  private int executeImports(List<FileObject> filesToImport) {
    if(options.isSource()) {
      return importFromDatasource(filesToImport.isEmpty() ? null : filesToImport.get(0));
    }
    if(options.isTables()) {
      return importFromTables(filesToImport.isEmpty() ? null : filesToImport.get(0));
    }
    if(!filesToImport.isEmpty()) {
      getShell().printf("Import from files not supported any more.\n");
      return CRITICAL_ERROR;
    }
    return SUCCESS;
  }

  //
  // Methods
  //

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("import");
    if(options.isSource()) {
      sb.append(" --source ").append(options.getSource());
    }
    sb.append(" --destination ").append(options.getDestination());

    if(options.isArchive()) {
      sb.append(" --archive ").append(options.getArchive());
    }

    if(options.isFiles()) {
      for(String file : options.getFiles()) {
        sb.append(' ').append(file);
      }
    }

    return sb.toString();
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private int importFromDatasource(@Nullable FileObject file) {
    int errorCode = CRITICAL_ERROR;
    getShell().printf("  Importing datasource %s in %s...\n", options.getSource(), options.getDestination());
    try {
      dataImportService.importData(options.getSource(), options.getDestination(), options.isForce(), options.isIgnore(),
          new ImportProgressListener());
      if(file != null) archive(file);
      errorCode = SUCCESS;
    } catch(NoSuchDatasourceException ex) {
      getShell().printf("Datasource '%s' does not exist. Cannot import.\n", ex.getDatasourceName());
    } catch(KeyProviderException ex) {
      getShell().printf("Decryption exception: %s\n", ex.getMessage());
    } catch(IOException ex) {
      // Report an error and continue with the next file.
      getShell().printf("Unrecoverable import exception: %s\n", ex.getMessage());
      errorCode = NON_CRITICAL_ERROR;
    } catch(InterruptedException ex) {
      // Report the interrupted and continue; the test for interruption will detect this condition.
      getShell().printf("Thread interrupted");
    } catch(RuntimeException ex) {
      runtimeExceptionHandler(ex);
    }
    return errorCode;
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private int importFromTables(@Nullable FileObject file) {
    int errorCode = CRITICAL_ERROR;
    getShell().printf("  Importing tables [%s] in %s ...\n", getTableNames(), options.getDestination());
    try {
      dataImportService.importData(options.getTables(), options.getDestination(), options.isForce(), options.isIgnore(),
          new ImportProgressListener());
      if(file != null) archive(file);
      errorCode = SUCCESS;
    } catch(NoSuchDatasourceException | NoSuchValueTableException ex) {
      getShell().printf("'%s'. Cannot import.\n", ex.getMessage());
    } catch(KeyProviderException ex) {
      getShell().printf("Decryption exception: %s\n", ex.getMessage());
    } catch(IOException ex) {
      // Report an error and continue with the next file.
      getShell().printf("Unrecoverable import exception: %s\n", ex.getMessage());
      errorCode = NON_CRITICAL_ERROR;
    } catch(InterruptedException ex) {
      // Report the interrupted and continue; the test for interruption will detect this condition.
      getShell().printf("Thread interrupted");
    } catch(RuntimeException ex) {
      runtimeExceptionHandler(ex);
    }
    return errorCode;
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
          "Datasource '%s' cannot be imported 'as-is'. It contains the following entity ids which are not present " +
              "as public identifiers in the keys database. %s\n", options.getSource(),
          ((NonExistentVariableEntitiesException) ex.getCause()).getNonExistentIdentifiers()
      );
    } else {
      printThrowable(ex, false);
    }
  }

  private void printThrowable(Throwable ex, boolean withStack) {
    if(!Strings.isNullOrEmpty(ex.getMessage())) getShell().printf(ex.getMessage());
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

    String archivePath = options.getArchive();
    try {
      FileObject archiveDir = getFile(archivePath);
      if(archiveDir == null) {
        throw new IOException(
            "Cannot archive file " + file.getName().getPath() + ". Archive directory is null: " + archivePath);
      }
      archiveDir.createFolder();
      FileObject archiveFile = archiveDir.resolveFile(file.getName().getBaseName());
      file.moveTo(archiveFile);
    } catch(FileSystemException ex) {
      throw new IOException("Failed to archive file " + file.getName().getPath() + " to " + archivePath);
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
    }
    if(filesToImport == null) return Lists.newArrayList();
    return filesToImport;
  }

  private List<FileObject> resolveFiles(Iterable<String> filePaths) {
    List<FileObject> files = new ArrayList<>();
    FileObject file;
    for(String filePath : filePaths) {
      try {
        file = getFileToImport(filePath);
        if(file == null || !file.exists()) {
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

  @Nullable
  private FileObject getFileToImport(String filePath) throws FileSystemException {
    return getFile(filePath);
  }

  private void addFile(Collection<FileObject> files, FileObject file) throws FileSystemException {
    FileType fileType = file.getType();
    if(fileType == FileType.FOLDER) {
      files.addAll(getFilesInFolder(file));
    } else if(fileType == FileType.FILE) {
      files.add(file);
    }
  }

  private Collection<FileObject> getFilesInFolder(FileObject file) throws FileSystemException {
    FileObject[] filesInDir = file.findFiles(new FileSelector() {
      @Override
      public boolean traverseDescendents(FileSelectInfo file) throws Exception {
        return true;
      }

      @Override
      public boolean includeFile(FileSelectInfo file) throws Exception {
        return file.getFile().getType() == FileType.FILE &&
            "zip".equals(file.getFile().getName().getExtension().toLowerCase());
      }
    });
    return Arrays.asList(filesInDir);
  }

  private class ImportProgressListener implements DatasourceCopierProgressListener {

    private int currentPercentComplete = -1;

    @Override
    public void status(String table, long entitiesCopied, long entitiesToCopy, int percentComplete) {
      if (percentComplete != currentPercentComplete) {
        getShell().progress(table, entitiesCopied, entitiesToCopy, percentComplete);
        currentPercentComplete = percentComplete;
      }
    }
  }
}
