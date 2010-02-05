/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import java.io.File;
import java.io.IOException;

import org.obiba.core.util.FileUtil;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.opal.cli.client.command.options.DecryptCommandOptions;
import org.obiba.opal.core.crypt.KeyProviderException;
import org.obiba.opal.core.service.DecryptService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Command to decrypt an Onyx data file.
 */
@CommandUsage(description = "Decrypts a list of Onyx data files into a directory.\n\nSyntax: decrypt --alias NAME --files FILE [FILE...] [--out FILE]")
public class DecryptCommand extends AbstractOpalRuntimeDependentCommand<DecryptCommandOptions> {
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
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }

    // if(options.isFiles()) {
    // Validate/initialize output directory.
    File outputDir = new File(".");
    if(options.isOutput()) {
      outputDir = getOutputDir(options.getOutput());
    }

    if(outputDir != null) {
      // Now process each input file (Onyx data zip file) specified on the command line.
      for(File inputFile : options.getFiles()) {
        try {
          processFile(inputFile, outputDir, null);
        } catch(KeyProviderException ex) {
          System.err.println(ex.getMessage());
          break; // break out of here, this is a fatal exception
        }
      }
    } else {
      System.err.println("Invalid output directory");
    }
    // } else {
    // System.err.println("No input file");
    // }
  }

  //
  // Methods
  //

  private void processFile(File inputFile, File outputDir, String keystorePassword) {
    String outputFilename = inputFile.getName().substring(0, inputFile.getName().length() - 5);
    File outputFile = new File(outputDir, outputFilename + "-plaintext.zip");
    FsDatasource outputDatasource = new FsDatasource(DECRYPT_DATASOURCE_NAME, outputFile);

    MagmaEngine.get().addDatasource(outputDatasource);
    try {
      decryptService.decryptData(DECRYPT_DATASOURCE_NAME, inputFile, true);
    } catch(IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Given the name/path of a directory, returns that directory (creating it if necessary).
   * 
   * @param output the name/path of the directory
   * @return the directory, as a <code>File</code> object (or <code>null</code> if the directory does not exist and
   * could not be created
   */
  private File getOutputDir(File outputDir) {
    if(!outputDir.isDirectory()) {
      if(!outputDir.isFile()) {
        boolean dirCreated = outputDir.mkdirs();
        if(!dirCreated) {
          outputDir = null;

          // Recursively delete the directory path, in case it was partially created.
          try {
            FileUtil.delete(outputDir);
          } catch(IOException ex) {
            ; // nothing to do
          }
        }
      } else {
        outputDir = null;
      }
    }

    return outputDir;
  }

}
