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
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.opal.core.service.DecryptService;
import org.obiba.opal.shell.commands.options.DecryptCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Command to decrypt an Onyx data file.
 */
@CommandUsage(description = "Decrypts one or more Onyx data files.", syntax = "Syntax: decrypt [--out FILE] _FILE_...")
public class DecryptCommand extends AbstractOpalRuntimeDependentCommand<DecryptCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(DecryptCommand.class);

  //
  // Constants
  //

  public static final String DECRYPT_DATASOURCE_NAME = "decrypt-datasource";

  //
  // Instance Variables
  //
  @Autowired
  private DecryptService decryptService;

  public void execute() {
    if(options.getFiles() == null) {
      getShell().printf("No input file(s) specified.\n");
      return;
    }

    if(options.isUnit()) {
      if(getOpalRuntime().getFunctionalUnit(options.getUnit()) == null) {
        getShell().printf("Functional unit '%s' does not exist. Cannot decrypt.\n", options.getUnit());
        return;
      }
    }

    FileObject outputDir = getFileSystemRoot();
    if(options.isOutput()) {
      outputDir = getOutputDir(options.getOutput());
    }
    if(outputDir == null) {
      getShell().printf("Invalid output directory");
      return;
    }

    decryptFiles(options.getFiles(), outputDir);
  }

  private void decryptFiles(List<String> encryptedFilePaths, FileObject outputDir) {
    FileObject encryptedFile;
    for(String path : encryptedFilePaths) {
      try {
        encryptedFile = getFile(path);

        if(encryptedFile.exists() == false) {
          getShell().printf("Skipping non-existent input file %s\n", path);
        } else {
          getShell().printf("Decrypting input file %s\n", path);
          try {
            decryptFile(encryptedFile, outputDir);
          } catch(IOException ex) {
            // Report an error and continue with the next file.
            getShell().printf("Unexpected decrypt exception: %s\n", ex.getMessage());
            ex.printStackTrace(System.err);
          }
        }
      } catch(FileSystemException ex) {
        getShell().printf("Skipping non-existent input file %s\n", path);
        log.warn("Cannot resolve the following file path : {}, skipping file...", ex);
      }
    }
  }

  //
  // Methods
  //

  private void decryptFile(FileObject inputFile, FileObject outputDir) throws IOException {
    FileObject outputFile = null;
    try {
      outputFile = getFile(outputDir, getOutputFileName(inputFile));
      outputFile.createFile();
    } catch(FileSystemException e) {
      throw new RuntimeException("Could not create the output file for the decrypted data", e);
    }

    FsDatasource outputDatasource = new FsDatasource(DECRYPT_DATASOURCE_NAME, getLocalFile(outputFile));
    MagmaEngine.get().addDatasource(outputDatasource);
    try {
      if(options.isUnit()) {
        decryptService.decryptData(options.getUnit(), DECRYPT_DATASOURCE_NAME, inputFile);
      } else {
        decryptService.decryptData(DECRYPT_DATASOURCE_NAME, inputFile);
      }
    } finally {
      try {
        MagmaEngine.get().removeDatasource(outputDatasource);
      } catch(Exception e) {
        log.warn("Could not remove the following datasource : {}", outputDatasource.getName(), e);
      }
    }
  }

  /**
   * Given the name/path of a directory, returns that directory (creating it if necessary).
   * 
   * @param outputDirPath the name/path of the directory.
   * @return the directory, as a <code>FileObject</code> object (or <code>null</code> if the directory could not be
   * created.
   */
  private FileObject getOutputDir(String outputDirPath) {
    FileObject outputDir;
    try {
      outputDir = getFile(outputDirPath);
      outputDir.createFolder();
    } catch(FileSystemException e) {
      outputDir = null;
    }
    return outputDir;
  }

  private String getOutputFileName(FileObject inputFile) {
    String inputFilename = inputFile.getName().getBaseName();
    String inputFilenameExt = "";
    String inputFilenamePrefix = "";
    int inputFilenameExtIndex = inputFilename.lastIndexOf(".");
    if(inputFilenameExtIndex > 0) {
      inputFilenamePrefix = inputFilename.substring(0, inputFilenameExtIndex);
      inputFilenameExt = inputFilename.substring(inputFilenameExtIndex, inputFilename.length());
    }

    return inputFilenamePrefix + "-plaintext" + inputFilenameExt;
  }
}
