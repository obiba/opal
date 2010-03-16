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

import org.apache.commons.vfs.FileSystemException;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.shell.commands.options.CertificateInfo;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides key management allowing for key creation, deletion, importing and exporting of keys.
 */
@CommandUsage(description = "Encryption key pairs creation, import or deletion.", syntax = "Syntax: keystore [--unit NAME] --alias NAME (--delete | --algo NAME --size INT | --private FILE [--certificate FILE])")
public class KeyCommand extends AbstractOpalRuntimeDependentCommand<KeyCommandOptions> {
  //
  // Instance Variables
  //

  @Autowired
  private UnitKeyStoreService unitKeyStoreService;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  public void execute() {
    if(options.isDelete()) {
      deleteKey();
    } else if(options.isAlgorithm() && options.isSize()) {
      createKey();
    } else if(options.isPrivate()) {
      try {
        importKey();
      } catch(FileSystemException e) {
        throw new RuntimeException("An error occured while reading the encryption key files.", e);
      }
    } else {
      unrecognizedOptionsHelp();
    }
  }

  //
  // Methods
  //

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

  private void createKey() {
    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    try {
      if(keyDoesNotExistOrOverwriteConfirmed(unit, options.getAlias())) {
        String certificateInfo = new CertificateInfo(getShell()).getCertificateInfoAsString();
        unitKeyStoreService.createOrUpdateKey(unit, options.getAlias(), options.getAlgorithm(), options.getSize(), certificateInfo);
        getShell().printf("Key generated with alias '%s'.\n", options.getAlias());
      }
    } catch(NoSuchFunctionalUnitException ex) {
      getShell().printf("Functional unit '%s' does not exist. Key not created.\n", ex.getUnitName());
    }
  }

  private void importKey() throws FileSystemException {
    String unit = options.isUnit() ? options.getUnit() : FunctionalUnit.OPAL_INSTANCE;

    if(getFile(options.getPrivate()).exists() == false) {
      getShell().printf("Private key file '%s' does not exist. Cannot import key.\n", options.getPrivate());
      return;
    }
    if(options.isCertificate() && getFile(options.getCertificate()).exists() == false) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import key.\n", options.getCertificate());
      return;
    }
    try {
      if(keyDoesNotExistOrOverwriteConfirmed(unit, options.getAlias())) {
        if(options.isCertificate()) {
          unitKeyStoreService.importKey(unit, options.getAlias(), getFile(options.getPrivate()), getFile(options.getCertificate()));
        } else {
          unitKeyStoreService.importKey(unit, options.getAlias(), getFile(options.getPrivate()), new CertificateInfo(getShell()).getCertificateInfoAsString());
        }
        getShell().printf("Key imported with alias '%s'.\n", options.getAlias());
      }
    } catch(NoSuchFunctionalUnitException ex) {
      getShell().printf("Functional unit '%s' does not exist. Key not imported.\n", ex.getUnitName());
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

  private void unrecognizedOptionsHelp() {
    getShell().printf("This combination of options was unrecognized." + "\nSyntax:" //
        + "\n  key [--unit NAME] --alias NAME (--delete | --algo NAME --size INT | --private FILE [--certificate FILE])" //
        + "\nExamples:" //
        + "\n  key [--unit NAME] --alias onyx --algo RSA --size 2048" //
        + "\n  key [--unit NAME] --alias onyx --private private_key.pem --certificate public_key.pem" //
        + "\n  key [--unit NAME] -alias onyx --delete\n"); //
  }

}