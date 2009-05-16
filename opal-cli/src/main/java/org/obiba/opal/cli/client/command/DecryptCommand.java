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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

import org.obiba.core.util.FileUtil;
import org.obiba.opal.cli.client.command.options.DecryptCommandOptions;
import org.obiba.opal.core.datasource.onyx.IOnyxDataInputStrategy;
import org.obiba.opal.core.datasource.onyx.OnyxDataInputContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.security.auth.callback.TextCallbackHandler;

/**
 * Command to decrypt an Onyx data file.
 */
public class DecryptCommand extends AbstractCommand<DecryptCommandOptions> {
  //
  // Instance Variables
  //

  private IOnyxDataInputStrategy dataInputStrategy;

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
        // Prompt user for keystore password.
        String keystorePassword = promptForKeystorePassword();

        // First, lazily initialize the dataInputStrategy variable (fetch it from the Spring ApplicationContext).
        ApplicationContext context = loadContext();
        setDataInputStrategy((IOnyxDataInputStrategy) context.getBean("onyxDataInputStrategy"));

        // Now process each input file (Onyx data zip file) specified on the command line.
        for(File inputFile : options.getFiles()) {
          processFile(inputFile, outputDir, keystorePassword);
        }

        System.out.println("Done!");
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

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  private ApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext("spring/opal-cli/context-lite.xml");
  }

  private void processFile(File inputFile, File outputDir, String keystorePassword) {
    System.out.println("Processing input file " + inputFile.getPath());

    // Create and initialize the dataInputContext, based on the command-line options that
    // were specified.
    OnyxDataInputContext dataInputContext = new OnyxDataInputContext();
    dataInputContext.setSource(inputFile.getPath());
    dataInputContext.setKeyProviderArgs(keystorePassword);

    // Prepare the dataInputStrategy.
    dataInputStrategy.prepare(dataInputContext);

    // Decrypt all encrypted entries in the specified file.
    for(String entryName : dataInputStrategy.listEntries()) {
      System.out.println("  Decrypting " + entryName + "...");
      InputStream entryStream = dataInputStrategy.getEntry(entryName);

      try {
        // Persist the decrypted entry.
        persistDecryptedEntry(entryStream, new File(outputDir, entryName));
      } catch(IOException ex) {
        System.err.println("  ERROR: " + ex.getMessage());
      }
    }
  }

  /**
   * Given the name/path of a directory, returns that directory (creating it if necessary).
   * 
   * @param output the name/path of the directory
   * @return the directory, as a <code>File</code> object (or <code>null</code> if the directory does not exist and
   * could not be created
   */
  private File getOutputDir(String output) {
    File outputDir = new File(output);

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
    if(outputFile.getParentFile() != null && !outputFile.getParentFile().isDirectory()) {
      outputFile.getParentFile().mkdirs();
    }

    // Now read the entry's stream and persist it to a file.
    // TODO: Replace this code with code from org.apache.commons.io.IOUtils.
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

  private String promptForKeystorePassword() {
    String keystorePassword = null;

    PasswordCallback passwordCallback = new PasswordCallback("keystore password: ", false);
    TextCallbackHandler handler = new TextCallbackHandler();

    try {
      handler.handle(new Callback[] { passwordCallback });
      keystorePassword = new String(passwordCallback.getPassword());
    } catch(Exception ex) {
      // nothing to do
    }

    return keystorePassword;
  }
}
