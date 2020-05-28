/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.opal.core.service.security.DecryptService;
import org.obiba.opal.shell.commands.options.DecryptCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Command to decrypt an Onyx data file.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
@CommandUsage(description = "Decrypts one or more Onyx data files.",
    syntax = "Syntax: decrypt [--unit NAME] [--out FILE] _FILE_...")
public class DecryptCommand extends AbstractOpalRuntimeDependentCommand<DecryptCommandOptions> {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DecryptCommand.class);

  public static final String DECRYPT_DATASOURCE_NAME = "decrypt-datasource";

  //
  // Instance Variables
  //

  @Autowired
  private DecryptService decryptService;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  @Override
  public int execute() {
    FileObject outputDir = getFileSystemRoot();
    if(options.isOutput()) {
      outputDir = getOutputDir(options.getOutput());
    }

    if(!validOutputDir(outputDir) || !validInputFiles()) {
      return 1; // error!
    }

    decryptFiles(options.getFiles(), outputDir);

    return 0; // success!
  }

  //
  // Methods
  //

  private boolean validOutputDir(FileObject outputDir) {
    if(outputDir == null) {
      getShell().printf("Invalid output directory");
      return false;
    }
    return true;
  }

  private boolean validInputFiles() {
    if(!options.isFiles()) {
      getShell().printf("No input file specified.\n");
      return false;
    }
    return true;
  }

  private void decryptFiles(Iterable<String> encryptedFilePaths, FileObject outputDir) {
    for(String path : encryptedFilePaths) {
      try {
        FileObject encryptedFile = getEncryptedFile(path);
        if(encryptedFile.exists()) {
          getShell().printf("Decrypting input file %s\n", path);
          decryptFile(encryptedFile, outputDir);
        } else {
          getShell().printf("Skipping non-existent input file %s\n", path);
        }
      } catch(IOException ex) {
        getShell().printf("Unexpected decrypt exception: %s, skipping\n", ex.getMessage());
        ex.printStackTrace(System.err);
      }
    }
  }

  private void decryptFile(FileObject inputFile, FileObject outputDir) throws IOException {
    FileObject outputFile = getFile(outputDir, getOutputFileName(inputFile));

    Datasource outputDatasource = new FsDatasource(DECRYPT_DATASOURCE_NAME, getLocalFile(outputFile));
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
    try {
      FileObject outputDir = getFile(outputDirPath);
      outputDir.createFolder();
      return outputDir;
    } catch(FileSystemException e) {
      return null;
    }
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

  private FileObject getEncryptedFile(String path) throws FileSystemException {
    return getFile(path);
  }
}
