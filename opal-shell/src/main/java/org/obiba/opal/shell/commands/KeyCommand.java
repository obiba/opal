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
import org.obiba.opal.core.crypt.x509.X509PrettyPrinter;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.shell.commands.options.CertificateInfo;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.io.Closeables;

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

  //
  // Instance Variables
  //

  @Autowired
  private UnitKeyStoreService unitKeyStoreService;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  @Override
  public int execute() {
    if(options.isUnit()) {
      if(unitDoesNotExist(options.getUnit()) || unitIsOpalInstance(options.getUnit())) {
        getShell().printf("Functional unit '%s' does not exist.\n", options.getUnit());
        return 1; // error!
      }
    }

    return executeAction(options.getAction());
  }

  //
  // Methods
  //

  public void setUnitKeyStoreService(UnitKeyStoreService unitKeyStoreService) {
    this.unitKeyStoreService = unitKeyStoreService;
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

    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    if(options.isAlgorithm() && options.isSize()) {
      try {
        if(keyDoesNotExistOrOverwriteConfirmed(unit)) {
          String certificateInfo = new CertificateInfo(getShell()).getCertificateInfoAsString();
          unitKeyStoreService
              .createOrUpdateKey(unit, options.getAlias(), options.getAlgorithm(), options.getSize(), certificateInfo);
          getShell().printf("Key generated with alias '%s'.\n", options.getAlias());
        }
      } catch(NoSuchFunctionalUnitException ex) {
        getShell().printf("Functional unit '%s' does not exist. Key not created.\n", ex.getUnitName());
        errorCode = 1;
      }
    } else {
      unrecognizedOptionsHelp();
      errorCode = 2;
    }

    return errorCode;
  }

  private int deleteKey() {
    int errorCode = 0;

    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    try {
      if(unitKeyStoreService.aliasExists(unit, options.getAlias())) {
        unitKeyStoreService.deleteKey(unit, options.getAlias());
        getShell().printf("Deleted key with alias '%s' from keystore '%s'.\n", options.getAlias(), unit);
      } else {
        getShell()
            .printf("The alias '%s' does not exist in keystore '%s'. No key deleted.\n", options.getAlias(), unit);
        errorCode = 1;
      }
    } catch(NoSuchFunctionalUnitException ex) {
      getShell().printf("Functional unit '%s' does not exist. No key deleted.\n", ex.getUnitName());
      errorCode = 2;
    }

    return errorCode;
  }

  private int importKey() {
    int errorCode = 0;

    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    try {
      if(options.isPrivate()) {
        importPrivateKey(unit);
      } else if(options.isCertificate()) {
        importCertificate(unit);
      } else {
        unrecognizedOptionsHelp();
        errorCode = 1;
      }
    } catch(NoSuchFunctionalUnitException ex) {
      getShell().printf("Functional unit '%s' does not exist. Key not imported.\n", ex.getUnitName());
      errorCode = 2;
    } catch(FileSystemException e) {
      throw new RuntimeException("An error occured while reading the encryption key files.", e);
    }

    return errorCode;
  }

  private void importPrivateKey(String unit) throws FileSystemException {
    if(!getFile(options.getPrivate()).exists()) {
      getShell().printf("Private key file '%s' does not exist. Cannot import key.\n", options.getPrivate());
      return;
    }
    if(options.isCertificate() && !getFile(options.getCertificate()).exists()) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import key.\n", options.getCertificate());
      return;
    }
    if(keyDoesNotExistOrOverwriteConfirmed(unit)) {
      importKeyFromFileOrInteractively(unit, options.getAlias());
    }
  }

  private void importCertificate(String unit) throws FileSystemException {
    if(options.isCertificate() && !getFile(options.getCertificate()).exists()) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import certificate.\n", options.getCertificate());
      return;
    }
    UnitKeyStore ks = unitKeyStoreService.getOrCreateUnitKeyStore(unit);
    ks.importCertificate(options.getAlias(), getFile(options.getCertificate()));
    unitKeyStoreService.saveUnitKeyStore(ks);
  }

  private void importKeyFromFileOrInteractively(String unit, String alias) throws FileSystemException {
    if(options.isCertificate()) {
      unitKeyStoreService.importKey(unit, alias, getFile(options.getPrivate()), getFile(options.getCertificate()));
    } else {
      unitKeyStoreService.importKey(unit, alias, getFile(options.getPrivate()),
          new CertificateInfo(getShell()).getCertificateInfoAsString());
    }
    getShell().printf("Key imported with alias '%s'.\n", alias);
  }

  private int exportCertificate() {
    int errorCode = 0;

    UnitKeyStore unitKeyStore = getUnitKeyStore();
    if(unitKeyStore == null) {
      getShell().printf("Keystore doesn't exist\n");
      return 1;
    }

    if(options.isPrivate()) {
      getShell().printf("WARNING: the export action only exports public certficates. Ignoring --private option.\n");
    }

    Writer certificateWriter = null;
    try {
      certificateWriter = getCertificateWriter();
      writeCertificate(unitKeyStore, options.getAlias(), certificateWriter);
    } catch(FileSystemException e) {
      getShell().printf("%s is an invalid output file.  Please make sure that you have specified a valid path.\n",
          options.getCertificate());
      errorCode = 2;
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(certificateWriter);
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

  private void writeCertificate(UnitKeyStore unitKeyStore, String alias, Writer writer) throws IOException {
    Assert.notNull(unitKeyStore, "unitKeyStore can not be null");
    Assert.notNull(writer, "writer can not be null");

    Certificate certificate;
    try {
      certificate = unitKeyStore.getKeyStore().getCertificate(alias);
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
    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;
    UnitKeyStore uks = unitKeyStoreService.getUnitKeyStore(unit);
    if(uks != null) {
      getShell().printf("Listing available keys (alias: <key type>) for '%s'\n", unit);
      for(String alias : uks.listAliases()) {
        Entry e = uks.getEntry(alias);
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
    }

    return 0; // success!
  }

  private boolean keyDoesNotExistOrOverwriteConfirmed(String unit) {
    boolean createKeyConfirmation = true;
    if(unitKeyStoreService.aliasExists(unit, options.getAlias())) {
      createKeyConfirmation = confirmKeyOverWrite();
    }
    return createKeyConfirmation;
  }

  private boolean confirmKeyOverWrite() {
    if(confirm("A key already exists for the alias [" + options.getAlias() + "]. Would you like to overwrite it?")) {
      return confirm("Please confirm a second time. Are you sure you want to overwrite the key with the alias [" +
          options.getAlias() + "]?");
    }
    return false;
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

  private UnitKeyStore getUnitKeyStore() {
    UnitKeyStore unitKeyStore = null;
    if(options.isUnit()) {
      FunctionalUnit unit = getFunctionalUnitService().getFunctionalUnit(options.getUnit());
      unitKeyStore = unit.getKeyStore();
    } else {
      unitKeyStore = unitKeyStoreService.getUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);
    }
    return unitKeyStore;
  }

  private boolean unitDoesNotExist(String unitName) {
    return !getFunctionalUnitService().hasFunctionalUnit(unitName);
  }

  private boolean unitIsOpalInstance(String unitName) {
    return FunctionalUnit.OPAL_INSTANCE.equals(unitName);
  }

  private int unrecognizedOptionsHelp() {
    getShell().printf("This combination of options was unrecognized." + "\nSyntax:" //
        +
        "\n  keystore --unit NAME (--action list | --alias NAME (--action create --algo NAME --size INT | --action delete | --action import [--private FILE] [--certificate FILE] | --action export [--certificate FILE]))"
        //
        + "\nExamples:" //
        + "\n  keystore --unit someUnit --action list" //
        + "\n  keystore --unit someUnit --alias someAlias --action create --algo RSA --size 2048" //
        + "\n  keystore --unit someUnit --alias someAlias --action delete" //
        +
        "\n  keystore --unit someUnit --alias someAlias --action import --private private_key.pem --certificate public_key.pem"
        //
        + "\n  keystore --unit someUnit --alias someAlias --action export --certificate public_key.pem\n"); //
    return 1; // error!
  }

}