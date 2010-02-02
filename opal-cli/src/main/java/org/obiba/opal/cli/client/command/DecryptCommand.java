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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.obiba.core.util.FileUtil;
import org.obiba.opal.cli.client.command.options.DecryptCommandOptions;
import org.obiba.opal.core.crypt.KeyProviderException;

/**
 * Command to decrypt an Onyx data file.
 */
public class DecryptCommand extends AbstractCommand<DecryptCommandOptions> {
  //
  // Constants
  //

  public static final String DIGEST_ALGORITHM = "SHA-512";

  public static final String DIGEST_ENTRY_SUFFIX = ".sha512";

  //
  // Instance Variables
  //

  //
  // AbstractCommand Methods
  //

  public void execute() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }

    if(options.isFiles()) {
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
    } else {
      System.err.println("No input file");
    }
  }

  //
  // Methods
  //

  private void processFile(File inputFile, File outputDir, String keystorePassword) {
    System.out.println("Processing input file " + inputFile.getPath());

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

  private void persistDecryptedEntry(InputStream entryStream, File outputFile) throws IOException {
    // Recursively create the parent directory if necessary.
    if(outputFile.getParentFile() != null) {
      if(!outputFile.getParentFile().exists()) {
        boolean dirCreated = outputFile.getParentFile().mkdirs();
        if(!dirCreated) {
          // Recursively delete the directory path, in case it was partially created.
          try {
            FileUtil.delete(outputFile.getParentFile());
          } catch(IOException ex) {
            ; // nothing to do
          }

          throw new IOException("Could not create parent directory " + outputFile.getParentFile().getPath());
        }
      } else if(!outputFile.getParentFile().isDirectory()) {
        throw new IOException("Invalid parent directory " + outputFile.getParentFile().getPath());
      }
    }

    // Now read the entry's stream and persist it to a file.
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(outputFile);

      while(true) {
        int entryByte = entryStream.read();
        if(entryByte == -1) {
          break;
        }

        fos.write(entryByte);
      }
    } finally {
      if(fos != null) {
        try {
          fos.close();
        } catch(IOException ex) {
        }
      }
    }
  }
}
