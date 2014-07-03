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
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.bouncycastle.openssl.PEMWriter;
import org.obiba.crypt.x509.X509PrettyPrinter;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.NoSuchProjectException;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.core.service.security.SystemKeyStoreService;
import org.obiba.opal.shell.commands.options.CertificateInfo;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Provides key management allowing for key creation, deletion, importing and exporting of keys.
 */
@CommandUsage(description = "Creates, deletes, imports and exports keypairs/certificates.",
    syntax = "Syntax: keystore [--unit NAME] (--action list | --alias NAME (--action create --algo NAME --size INT | --action delete | --action import --private FILE [--certificate FILE] | --action export --certificate FILE))")
public class KeyCommand extends AbstractOpalRuntimeDependentCommand<KeyCommandOptions> {

  private static final String CREATE_ACTION = "create";

  private static final String DELETE_ACTION = "delete";

  private static final String IMPORT_ACTION = "import";

  private static final String EXPORT_ACTION = "export";

  private static final String LIST_ACTION = "list";

  private SystemKeyStoreService systemKeyStoreService;

  private ProjectsKeyStoreService projectsKeyStoreService;

  private ProjectService projectService;

  @Autowired
  public void setSystemKeyStoreService(SystemKeyStoreService systemKeyStoreService) {
    this.systemKeyStoreService = systemKeyStoreService;
  }

  @Autowired
  public void setProjectsKeyStoreService(ProjectsKeyStoreService projectsKeyStoreService) {
    this.projectsKeyStoreService = projectsKeyStoreService;
  }

  @Autowired
  public void setProjectService(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public int execute() {
    if(options.isUnit()) {
      try {
        projectService.getProject(options.getUnit());
      } catch(NoSuchProjectException e) {
        getShell().printf("Project '%s' does not exist.\n", options.getUnit());
        return 1; // error!
      }
    }
    return executeAction(options.getAction());
  }

  private int executeAction(String action) {
    if(action.equals(CREATE_ACTION) && hasAlias()) {
      return createKey();
    }
    if(action.equals(DELETE_ACTION) && hasAlias()) {
      return deleteKey();
    }
    if(action.equals(IMPORT_ACTION) && hasAlias()) {
      return importKey();
    }
    if(action.equals(EXPORT_ACTION) && hasAlias()) {
      return exportCertificate();
    }
    if(action.equals(LIST_ACTION) && !hasAlias()) {
      return listKeystore();
    }
    return unrecognizedOptionsHelp();
  }

  private boolean hasAlias() {
    return options.isAlias();
  }

  private int createKey() {
    int errorCode = 0;
    if(options.isAlgorithm() && options.isSize()) {
      String alias = options.getAlias();
      if(options.isUnit()) {
        Project project = projectService.getProject(options.getUnit());
        if(projectKeyDoesNotExistOrOverwriteConfirmed(project)) {
          String certificateInfo = new CertificateInfo(getShell()).getCertificateInfoAsString();
          projectsKeyStoreService
              .createOrUpdateKey(project, alias, options.getAlgorithm(), options.getSize(), certificateInfo);
          getShell().printf("Key generated with alias '%s'.\n", alias);
        }
      } else {
        if(systemKeyDoesNotExistOrOverwriteConfirmed()) {
          String certificateInfo = new CertificateInfo(getShell()).getCertificateInfoAsString();
          systemKeyStoreService.createOrUpdateKey(alias, options.getAlgorithm(), options.getSize(), certificateInfo);
          getShell().printf("Key generated with alias '%s'.\n", alias);
        }
      }
    } else {
      unrecognizedOptionsHelp();
      errorCode = 2;
    }
    return errorCode;
  }

  private int deleteKey() {
    int errorCode = 0;
    String alias = options.getAlias();
    if(options.isUnit()) {
      Project project = projectService.getProject(options.getUnit());
      if(projectsKeyStoreService.aliasExists(project, alias)) {
        projectsKeyStoreService.deleteKeyStore(project, alias);
        getShell().printf("Deleted key with alias '%s' from keystore '%s'.\n", alias, project.getName());
      } else {
        getShell()
            .printf("The alias '%s' does not exist in keystore '%s'. No key deleted.\n", alias, project.getName());
        errorCode = 1;
      }
    } else {
      if(systemKeyStoreService.aliasExists(alias)) {
        systemKeyStoreService.deleteKeyStore(alias);
        getShell().printf("Deleted key with alias '%s' from system keystore.\n", alias);
      } else {
        getShell().printf("The alias '%s' does not exist in system keystore. No key deleted.\n", alias);
        errorCode = 1;
      }
    }

    return errorCode;
  }

  private int importKey() {
    try {
      return options.isUnit() ? importProjectKey() : importSystemKey();
    } catch(FileSystemException e) {
      throw new RuntimeException("An error occurred while reading the encryption key files.", e);
    }
  }

  private int importSystemKey() throws FileSystemException {
    if(options.isPrivate()) {
      importSystemPrivateKey();
    } else if(options.isCertificate()) {
      importSystemCertificate();
    } else {
      unrecognizedOptionsHelp();
      return 1;
    }
    return 0;
  }

  private int importProjectKey() throws FileSystemException {
    Project project = projectService.getProject(options.getUnit());
    if(options.isPrivate()) {
      importProjectPrivateKey(project);
    } else if(options.isCertificate()) {
      importProjectCertificate(project);
    } else {
      unrecognizedOptionsHelp();
      return 1;
    }
    return 0;
  }

  private void importProjectPrivateKey(Project project) throws FileSystemException {
    if(!getFile(options.getPrivate()).exists()) {
      getShell().printf("Private key file '%s' does not exist. Cannot import key.\n", options.getPrivate());
      return;
    }
    if(options.isCertificate() && !getFile(options.getCertificate()).exists()) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import key.\n", options.getCertificate());
      return;
    }
    if(projectKeyDoesNotExistOrOverwriteConfirmed(project)) {
      importProjectKeyFromFileOrInteractively(project, options.getAlias());
    }
  }

  private void importSystemPrivateKey() throws FileSystemException {
    if(!getFile(options.getPrivate()).exists()) {
      getShell().printf("Private key file '%s' does not exist. Cannot import key.\n", options.getPrivate());
      return;
    }
    if(options.isCertificate() && !getFile(options.getCertificate()).exists()) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import key.\n", options.getCertificate());
      return;
    }
    if(systemKeyDoesNotExistOrOverwriteConfirmed()) {
      importSystemKeyFromFileOrInteractively(options.getAlias());
    }
  }

  private void importProjectCertificate(Project project) throws FileSystemException {
    if(options.isCertificate() && !getFile(options.getCertificate()).exists()) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import certificate.\n", options.getCertificate());
      return;
    }
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(project);
    keyStore.importCertificate(options.getAlias(), getFile(options.getCertificate()));
    projectsKeyStoreService.saveKeyStore(keyStore);
  }

  private void importSystemCertificate() throws FileSystemException {
    if(options.isCertificate() && !getFile(options.getCertificate()).exists()) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import certificate.\n", options.getCertificate());
      return;
    }
    OpalKeyStore keyStore = systemKeyStoreService.getKeyStore();
    keyStore.importCertificate(options.getAlias(), getFile(options.getCertificate()));
    projectsKeyStoreService.saveKeyStore(keyStore);
  }

  private void importProjectKeyFromFileOrInteractively(Project project, String alias) throws FileSystemException {
    if(options.isCertificate()) {
      projectsKeyStoreService
          .importKey(project, alias, getFile(options.getPrivate()), getFile(options.getCertificate()));
    } else {
      projectsKeyStoreService.importKey(project, alias, getFile(options.getPrivate()),
          new CertificateInfo(getShell()).getCertificateInfoAsString());
    }
    getShell().printf("Key imported with alias '%s'.\n", alias);
  }

  private void importSystemKeyFromFileOrInteractively(String alias) throws FileSystemException {
    if(options.isCertificate()) {
      systemKeyStoreService.importKey(alias, getFile(options.getPrivate()), getFile(options.getCertificate()));
    } else {
      systemKeyStoreService.importKey(alias, getFile(options.getPrivate()),
          new CertificateInfo(getShell()).getCertificateInfoAsString());
    }
    getShell().printf("Key imported with alias '%s'.\n", alias);
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private int exportCertificate() {
    int errorCode = 0;

    OpalKeyStore opalKeyStore = getKeyStore();
    if(opalKeyStore == null) {
      getShell().printf("Keystore doesn't exist\n");
      return 1;
    }

    if(options.isPrivate()) {
      getShell().printf("WARNING: the export action only exports public certificates. Ignoring --private option.\n");
    }

    Writer certificateWriter = null;
    try {
      certificateWriter = getCertificateWriter();
      writeCertificate(opalKeyStore, options.getAlias(), certificateWriter);
    } catch(FileSystemException e) {
      getShell().printf("%s is an invalid output file.  Please make sure that you have specified a valid path.\n",
          options.getCertificate());
      errorCode = 2;
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if(certificateWriter != null) certificateWriter.close();
      } catch(IOException ignored) {
      }
    }
    return errorCode;
  }

  private Writer getCertificateWriter() throws FileSystemException {
    Writer certificateWriter;
    if(options.isCertificate()) {
      FileObject outputFile = getFileSystemRoot().resolveFile(options.getCertificate());
      certificateWriter = new OutputStreamWriter(outputFile.getContent().getOutputStream());
    } else {
      certificateWriter = new StringWriter();
    }
    return certificateWriter;
  }

  private void writeCertificate(OpalKeyStore opalKeyStore, String alias, Writer writer) throws IOException {
    Assert.notNull(opalKeyStore, "opalKeyStore can not be null");
    Assert.notNull(writer, "writer can not be null");

    Certificate certificate;
    try {
      certificate = opalKeyStore.getKeyStore().getCertificate(alias);
      if(certificate == null) {
        getShell().printf("No certificate was found for alias '%s'.\n", alias);
      } else {
        if(certificate instanceof X509Certificate) {
          getShell().printf("%s\n", X509PrettyPrinter.prettyPrint((X509Certificate) certificate));
        }
        PEMWriter pemWriter = new PEMWriter(writer);
        pemWriter.writeObject(certificate);
        pemWriter.flush();
        printResult(writer);
      }
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }

  private void printResult(Writer certificateWriter) {
    if(options.isCertificate()) {
      getShell().printf("Certificate written to file [%s]\n", options.getCertificate());
    } else {
      getShell().printf("Certificate will be printed to console. You may then copy-paste it elsewhere:\n",
          options.getCertificate());
      getShell().printf(((StringWriter) certificateWriter).getBuffer().toString());
    }
  }

  private int listKeystore() {
    OpalKeyStore keyStore = getKeyStore();
    getShell().printf("Listing available keys (alias: <key type>) for '%s'\n",
        options.isUnit() ? options.getUnit() : "system");
    for(String alias : keyStore.listAliases()) {
      Entry e = keyStore.getEntry(alias);
      String type = "<unknown>";
      if(e instanceof TrustedCertificateEntry) {
        type = "<certificate>";
      } else if(e instanceof PrivateKeyEntry) {
        type = "<key pair>";
      } else if(e instanceof SecretKeyEntry) {
        type = "<secret key>";
      }
      getShell().printf("%s: %s\n", alias, type);
    }

    return 0; // success!
  }

  private boolean projectKeyDoesNotExistOrOverwriteConfirmed(Project project) {
    return !projectsKeyStoreService.aliasExists(project, options.getAlias()) || confirmKeyOverWrite();
  }

  private boolean systemKeyDoesNotExistOrOverwriteConfirmed() {
    return !systemKeyStoreService.aliasExists(options.getAlias()) || confirmKeyOverWrite();
  }

  private boolean confirmKeyOverWrite() {
    return
        confirm("A key already exists for the alias [" + options.getAlias() + "]. Would you like to overwrite it?") &&
            confirm("Please confirm a second time. Are you sure you want to overwrite the key with the alias [" +
                options.getAlias() + "]?");
  }

  private boolean confirm(String question) {
    getShell().printf("%s\n", question);
    String ans = "no";
    do {
      String answer = getShell().prompt(String.format("  [%s]:  ", ans));
      if(answer != null && !"".equals(answer)) {
        ans = answer;
      }
    } while(!("yes".equalsIgnoreCase(ans) || "no".equalsIgnoreCase(ans) || "y".equalsIgnoreCase(ans) ||
        "n".equalsIgnoreCase(ans)));
    return "yes".equalsIgnoreCase(ans) || "y".equalsIgnoreCase(ans);
  }

  private OpalKeyStore getKeyStore() {
    return options.isUnit() //
        ? projectsKeyStoreService.getKeyStore(projectService.getProject(options.getUnit())) //
        : systemKeyStoreService.getKeyStore();
  }

  private int unrecognizedOptionsHelp() {
    getShell().printf("This combination of options was unrecognized." + "\nSyntax:" +
        "\n  keystore --unit NAME (--action list | --alias NAME (--action create --algo NAME --size INT | --action delete | --action import [--private FILE] [--certificate FILE] | --action export [--certificate FILE]))" +
        "\nExamples:" + "\n  keystore --unit someUnit --action list" +
        "\n  keystore --unit someUnit --alias someAlias --action create --algo RSA --size 2048" +
        "\n  keystore --unit someUnit --alias someAlias --action delete" +
        "\n  keystore --unit someUnit --alias someAlias --action import --private private_key.pem --certificate public_key.pem" +
        "\n  keystore --unit someUnit --alias someAlias --action export --certificate public_key.pem\n");
    return 1; // error!
  }

}