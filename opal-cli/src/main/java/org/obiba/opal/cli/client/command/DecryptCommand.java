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
import org.obiba.opal.core.datasource.onyx.DigestMismatchException;
import org.obiba.opal.core.datasource.onyx.DigestUtil;
import org.obiba.opal.core.datasource.onyx.EncryptionDataMissingException;
import org.obiba.opal.core.datasource.onyx.IOnyxDataInputStrategy;
import org.obiba.opal.core.datasource.onyx.KeyProviderException;
import org.obiba.opal.core.datasource.onyx.OnyxDataInputContext;
import org.obiba.opal.core.datasource.onyx.OpalKeyStore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.security.auth.callback.TextCallbackHandler;

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
        // First, lazily initialize the dataInputStrategy variable (fetch it from the Spring ApplicationContext).
        ApplicationContext context = loadContext();
        setDataInputStrategy((IOnyxDataInputStrategy) context.getBean("onyxDataInputStrategy"));

        // Prompt user for keystore password.
        String keystorePassword = promptForPassword("Enter keystore password: ");

        // Now process each input file (Onyx data zip file) specified on the command line.
        for(File inputFile : options.getFiles()) {
          try {
            processFile(inputFile, outputDir, keystorePassword);
          } catch(DigestMismatchException ex) {
            System.err.println(ex.getMessage());
          } catch(EncryptionDataMissingException ex) {
            System.err.println(ex.getMessage());
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

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  private ApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext("spring/opal-cli/context-lite.xml");
  }

  private void processFile(File inputFile, File outputDir, String keystorePassword) {
    System.out.println("Processing input file " + inputFile.getPath());

    // Check the file against its digest. No point doing anything if the file is corrupted.
    File digestFile = new File(inputFile.getPath() + DIGEST_ENTRY_SUFFIX);
    DigestUtil.checkDigest(DIGEST_ALGORITHM, digestFile, inputFile);

    // Prompt user for key password.
    String keyPassword = promptForPassword("Enter key password (RETURN if same as keystore password): ");
    if(keyPassword == null) {
      keyPassword = keystorePassword;
    }

    // Create the dataInputContext, based on the specified command-line options.
    OnyxDataInputContext dataInputContext = new OnyxDataInputContext();
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEYSTORE_PASSWORD_ARGKEY, keystorePassword);
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEY_PASSWORD_ARGKEY, keyPassword);
    dataInputContext.setSource(inputFile.getPath());

    // Prepare the strategy.
    dataInputStrategy.prepare(dataInputContext);

    // Decrypt all encrypted entries in the specified file.
    try {
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
    } catch(KeyProviderException ex) {
      System.err.println("  ERROR: " + ex.getMessage());
    } catch(DigestMismatchException ex) {
      System.err.println("  ERROR: " + ex.getMessage());
    } finally {
      // Terminate the strategy.
      dataInputStrategy.terminate(dataInputContext);
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

  private String promptForPassword(String prompt) {
    String password = null;

    PasswordCallback passwordCallback = new PasswordCallback(prompt, false);
    TextCallbackHandler handler = new TextCallbackHandler();

    try {
      handler.handle(new Callback[] { passwordCallback });
      if(passwordCallback.getPassword() != null) {
        password = new String(passwordCallback.getPassword());

        if(password.length() == 0) {
          password = null;
        }
      }
    } catch(Exception ex) {
      // nothing to do
    }

    return password;
  }
}
