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

import java.security.KeyStoreException;

import org.obiba.opal.core.crypt.StudyKeyStore;
import org.obiba.opal.core.service.StudyKeyStoreService;
import org.obiba.opal.shell.commands.options.CertificateInfo;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Provides key management allowing for key creation, deletion, importing and exporting of keys.
 */
@CommandUsage(description = "Encryption key pairs creation, import or deletion.", syntax = "Syntax: keystore --alias NAME (--delete | --algo NAME --size INT | --private FILE [--certificate FILE])")
public class KeyCommand extends AbstractCommand<KeyCommandOptions> {

  @Autowired
  private StudyKeyStoreService studyKeyStoreService;

  public void execute() {
    if(options.isDelete()) {
      deleteKey();
    } else if(options.isAlgorithm() && options.isSize()) {
      createKey();
    } else if(options.isPrivate()) {
      importKey();
    } else {
      unrecognizedOptionsHelp();
    }
  }

  private void deleteKey() {
    StudyKeyStore studyKeyStore = studyKeyStoreService.getStudyKeyStore(StudyKeyStoreService.DEFAULT_STUDY_ID);
    if(studyKeyStore == null) {
      getShell().printf("Keystore doesn't exist\n");
    } else {
      if(studyKeyStoreService.aliasExists(options.getAlias())) {
        studyKeyStoreService.deleteKey(options.getAlias());
        getShell().printf("Deleted key with alias '%s'.\n", options.getAlias());
      } else {
        getShell().printf("The alias '%s' does not exist. No key deleted.\n", options.getAlias());
      }
    }

  }

  private void createKey() {
    boolean createKeyConfirmation = true;
    if(aliasAlreadyExists(options.getAlias())) {
      createKeyConfirmation = confirmKeyOverWrite();
    }

    if(createKeyConfirmation) {
      String certificateInfo = new CertificateInfo(getShell()).getCertificateInfoAsString();
      studyKeyStoreService.createOrUpdateKey(options.getAlias(), options.getAlgorithm(), options.getSize(), certificateInfo);
      getShell().printf("Key generated with alias '%s'.\n", options.getAlias());
    }

  }

  private void importKey() {
    // Private key file is required.
    if(options.getPrivate().exists() == false) {
      getShell().printf("Private key file '%s' does not exist. Cannot import key.\n", options.getPrivate().getPath());
      return;
    }
    // If specified, certificate file must exist.
    if(options.isCertificate() && options.getCertificate().exists() == false) {
      getShell().printf("Certificate file '%s' does not exist. Cannot import key.\n", options.getCertificate().getPath());
      return;
    }

    boolean createKeyConfirmation = true;
    if(aliasAlreadyExists(options.getAlias())) {
      createKeyConfirmation = confirmKeyOverWrite();
    }

    if(createKeyConfirmation) {
      if(options.isCertificate()) {
        studyKeyStoreService.importKey(options.getAlias(), options.getPrivate(), options.getCertificate());
      } else {
        String certificateInfo = new CertificateInfo(getShell()).getCertificateInfoAsString();
        studyKeyStoreService.importKey(options.getAlias(), options.getPrivate(), certificateInfo);
      }
      getShell().printf("Key imported with alias '%s'.\n", options.getAlias());
    }

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

  private boolean aliasAlreadyExists(String alias) {
    Assert.hasText(alias, "alias must not be empty or null");
    StudyKeyStore studyKeyStore = studyKeyStoreService.getStudyKeyStore(StudyKeyStoreService.DEFAULT_STUDY_ID);
    if(studyKeyStore == null) return false; // The KeyStore doesn't exist.
    try {
      return studyKeyStore.getKeyStore().containsAlias(alias);
    } catch(KeyStoreException e) {
      getShell().printf("not loaded\n");
      throw new RuntimeException(e);
    }
  }

  private void unrecognizedOptionsHelp() {
    getShell().printf("This combination of options was unrecognized." + "\nSyntax:" //
        + "\n  key --alias NAME (--delete | --algo NAME --size INT | --private FILE [--certificate FILE])" //
        + "\nExamples:" //
        + "\n  key --alias onyx --algo RSA --size 2048" //
        + "\n  key --alias onyx --private private_key.pem --certificate public_key.pem" //
        + "\n  key -alias onyx --delete\n"); //
  }

}