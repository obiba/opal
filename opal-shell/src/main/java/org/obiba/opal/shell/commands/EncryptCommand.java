/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.PublicKey;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.KeyProviderSecurityException;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.core.service.NoSuchProjectException;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.shell.commands.options.EncryptCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Command to decrypt an Onyx data file.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
@CommandUsage(description = "Decrypts one or more Onyx data files.",
    syntax = "Syntax: decrypt [--unit NAME] [--out FILE] _FILE_...")
public class EncryptCommand extends AbstractOpalRuntimeDependentCommand<EncryptCommandOptions> {

  public static final String DECRYPT_DATASOURCE_NAME = "decrypt-datasource";

  @Autowired
  private ProjectService projectService;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Override
  public int execute() {
    FileObject outputDir = getFileSystemRoot();
    if(options.isOutput()) {
      outputDir = getOutputDir(options.getOutput());
    }

    if(!validOutputDir(outputDir) || !validProject()) {
      return 1; // error!
    }

    encryptFiles(options.getFiles(), outputDir);

    return 0; // success!
  }

  private boolean validOutputDir(FileObject outputDir) {
    if(outputDir == null) {
      getShell().printf("Invalid output directory");
      return false;
    }
    return true;
  }

  private boolean validProject() {
    try {
      projectService.getProject(options.getUnit());
    } catch(NoSuchProjectException e) {
      getShell().printf("Project '%s' does not exist. Cannot decrypt.\n", options.getUnit());
      return false;
    }
    return true;
  }

  private void encryptFiles(Iterable<String> decryptedFilePaths, FileObject outputDir) {
    for(String path : decryptedFilePaths) {
      try {
        FileObject decryptedFile = getFile(path);
        if(decryptedFile.exists()) {
          getShell().printf("Decrypting input file %s\n", path);
          encryptFile(decryptedFile, outputDir);
        } else {
          getShell().printf("Skipping non-existent input file %s\n", path);
        }
      } catch(IOException ex) {
        getShell().printf("Unexpected decrypt exception: %s, skipping\n", ex.getMessage());
        ex.printStackTrace(System.err);
      }
    }
  }

  private void encryptFile(FileObject inputFile, FileObject outputDir) throws IOException {
    FileObject outputFile = getFile(outputDir, inputFile.getName().getBaseName());

    GeneratedSecretKeyDatasourceEncryptionStrategy s = new GeneratedSecretKeyDatasourceEncryptionStrategy();
    s.setKeyProvider(new KeyProvider() {

      @Override
      public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException {
        try {
          return projectsKeyStoreService.getKeyStore(projectService.getProject(options.getUnit())).getKeyStore()
              .getCertificate(options.getAlias()).getPublicKey();
        } catch(KeyStoreException e) {
          throw new MagmaCryptRuntimeException(e);
        }
      }

      @Override
      public KeyPair getKeyPair(PublicKey publicKey) throws NoSuchKeyException, KeyProviderSecurityException {
        return null;
      }

      @Override
      public KeyPair getKeyPair(String alias) throws NoSuchKeyException, KeyProviderSecurityException {
        return null;
      }
    });

    FsDatasource input = new FsDatasource("input", getLocalFile(inputFile));
    FsDatasource output = new FsDatasource(DECRYPT_DATASOURCE_NAME, getLocalFile(outputFile), s);

    try {
      Initialisables.initialise(input, output);
      DatasourceCopier.Builder.newCopier().build().copy(input, output);
    } finally {
      Disposables.silentlyDispose(input);
      Disposables.silentlyDispose(output);
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

}
