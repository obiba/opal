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
import java.security.KeyStoreException;
import java.security.cert.Certificate;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.bouncycastle.openssl.PEMWriter;
import org.obiba.core.util.StreamUtil;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.shell.commands.options.CertificateInfo;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Provides key management allowing for key creation, deletion, importing and exporting of keys.
 */
@CommandUsage(description = "Creates, deletes, imports and exports keypairs/certificates.", syntax = "Syntax: keystore [--unit NAME] --alias NAME (--action create --algo NAME --size INT | --action delete | --action import --private FILE [--certificate FILE] | --action export [--certificate FILE])")
public class KeyCommand extends AbstractOpalRuntimeDependentCommand<KeyCommandOptions> {
  //
  // Constants
  //

  private static final String CREATE_ACTION = "create";

  private static final String DELETE_ACTION = "delete";

  private static final String IMPORT_ACTION = "import";

  private static final String EXPORT_ACTION = "export";

  //
  // Instance Variables
  //

  @Autowired
  private UnitKeyStoreService unitKeyStoreService;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  public void execute() {
    if(options.isUnit()) {
      if(unitDoesNotExist(options.getUnit()) || unitIsOpalInstance(options.getUnit())) {
        getShell().printf("Functional unit '%s' does not exist.\n", options.getUnit());
        return;
      }
    }

    executeAction(options.getAction());
  }

  //
  // Methods
  //

  public void setUnitKeyStoreService(UnitKeyStoreService unitKeyStoreService) {
    this.unitKeyStoreService = unitKeyStoreService;
  }

  private void executeAction(String action) {
    if(action.equals(CREATE_ACTION)) {
      createKey();
    } else if(action.equals(DELETE_ACTION)) {
      deleteKey();
    } else if(action.equals(IMPORT_ACTION)) {
      importKey();
    } else if(action.equals(EXPORT_ACTION)) {
      exportCertificate();
    } else {
      unrecognizedOptionsHelp();
    }
  }

  private void createKey() {
    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    if(options.isAlgorithm() && options.isSize()) {
      try {
        if(keyDoesNotExistOrOverwriteConfirmed(unit, options.getAlias())) {
          String certificateInfo = new CertificateInfo(getShell()).getCertificateInfoAsString();
          unitKeyStoreService.createOrUpdateKey(unit, options.getAlias(), options.getAlgorithm(), options.getSize(), certificateInfo);
          getShell().printf("Key generated with alias '%s'.\n", options.getAlias());
        }
      } catch(NoSuchFunctionalUnitException ex) {
        getShell().printf("Functional unit '%s' does not exist. Key not created.\n", ex.getUnitName());
      }
    } else {
      unrecognizedOptionsHelp();
    }
  }

  private void deleteKey() {
    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    try {
      if(unitKeyStoreService.aliasExists(unit, options.getAlias())) {
        unitKeyStoreService.deleteKey(unit, options.getAlias());
        getShell().printf("Deleted key with alias '%s' from keystore '%s'.\n", options.getAlias(), unit);
      } else {
        getShell().printf("The alias '%s' does not exist in keystore '%s'. No key deleted.\n", options.getAlias(), unit);
      }
    } catch(NoSuchFunctionalUnitException ex) {
      getShell().printf("Functional unit '%s' does not exist. No key deleted.\n", ex.getUnitName());
    }
  }

  private void importKey() {
    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    if(options.isPrivate()) {
      try {
        if(getFile(options.getPrivate()).exists() == false) {
          getShell().printf("Private key file '%s' does not exist. Cannot import key.\n", options.getPrivate());
          return;
        }
        if(options.isCertificate() && getFile(options.getCertificate()).exists() == false) {
          getShell().printf("Certificate file '%s' does not exist. Cannot import key.\n", options.getCertificate());
          return;
        }
        if(keyDoesNotExistOrOverwriteConfirmed(unit, options.getAlias())) {
          importKeyFromFileOrInteractively(unit, options.getAlias());
        }
      } catch(NoSuchFunctionalUnitException ex) {
        getShell().printf("Functional unit '%s' does not exist. Key not imported.\n", ex.getUnitName());
      } catch(FileSystemException e) {
        throw new RuntimeException("An error occured while reading the encryption key files.", e);
      }
    } else {
      unrecognizedOptionsHelp();
    }
  }

  private void importKeyFromFileOrInteractively(String unit, String alias) throws FileSystemException {
    if(options.isCertificate()) {
      unitKeyStoreService.importKey(unit, alias, getFile(options.getPrivate()), getFile(options.getCertificate()));
    } else {
      unitKeyStoreService.importKey(unit, alias, getFile(options.getPrivate()), new CertificateInfo(getShell()).getCertificateInfoAsString());
    }
    getShell().printf("Key imported with alias '%s'.\n", alias);
  }

  private void exportCertificate() {
    UnitKeyStore unitKeyStore = getUnitKeyStore();
    if(unitKeyStore == null) {
      getShell().printf("Keystore doesn't exist\n");
      return;
    }

    Writer certificateWriter = null;
    try {
      certificateWriter = getCertificateWriter();
      writeCertificate(unitKeyStore, options.getAlias(), certificateWriter);
    } catch(FileSystemException e) {
      getShell().printf("%s is an invalid output file.  Please make sure that you have specified a valid path.\n", options.getCertificate());
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      StreamUtil.silentSafeClose(certificateWriter);
    }
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

    Certificate certificate;
    try {
      certificate = unitKeyStore.getKeyStore().getCertificate(alias);
      if(certificate == null) {
        getShell().printf("No certificate was found for alias '%s'.\n", alias);
      } else {
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
      getShell().printf("Certificate written to the file [%s]\n", options.getCertificate());
    } else {
      getShell().printf(((StringWriter) certificateWriter).getBuffer().toString());
    }
  }

  private boolean keyDoesNotExistOrOverwriteConfirmed(String unit, String alias) {
    boolean createKeyConfirmation = true;
    if(unitKeyStoreService.aliasExists(unit, options.getAlias())) {
      createKeyConfirmation = confirmKeyOverWrite();
    }
    return createKeyConfirmation;
  }

  private boolean confirmKeyOverWrite() {
    if(confirm("A key already exists for the alias [" + options.getAlias() + "]. Would you like to overwrite it?")) {
      return confirm("Please confirm a second time. Are you sure you want to overwrite the key with the alias [" + options.getAlias() + "]?");
    }
    return false;
  }

  private boolean confirm(String question) {
    getShell().printf("%s\n", question);
    String ans = "no";
    do {
      String answer = getShell().prompt(String.format("  [%s]:  ", ans));
      if(answer != null && !answer.equals("")) {
        ans = answer;
      }
    } while(!(ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("no") || ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("n")));
    if(ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("y")) {
      return true;
    }
    return false;
  }

  private UnitKeyStore getUnitKeyStore() {
    UnitKeyStore unitKeyStore = null;
    if(options.isUnit()) {
      FunctionalUnit unit = getOpalRuntime().getFunctionalUnit(options.getUnit());
      unitKeyStore = unit.getKeyStore();
    } else {
      unitKeyStore = unitKeyStoreService.getUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);
    }
    return unitKeyStore;
  }

  private boolean unitDoesNotExist(String unitName) {
    return getOpalRuntime().getFunctionalUnit(unitName) == null;
  }

  private boolean unitIsOpalInstance(String unitName) {
    return FunctionalUnit.OPAL_INSTANCE.equals(unitName);
  }

  private void unrecognizedOptionsHelp() {
    getShell().printf("This combination of options was unrecognized." + "\nSyntax:" //
        + "\n  keystore [--unit NAME] --alias NAME (--action create --algo NAME --size INT | --action delete | --action import --private FILE [--certificate FILE] | --action export [--certificate FILE])" //
        + "\nExamples:" //
        + "\n  keystore --unit someUnit --alias someAlias --action create --algo RSA --size 2048" //
        + "\n  keystore --unit someUnit --alias someAlias --action delete" //
        + "\n  keystore --unit someUnit --alias someAlias --action import --private private_key.pem --certificate public_key.pem" //
        + "\n  keystore --unit someUnit --alias someAlias --action export --certificate public_key.pem\n"); //
  }

}