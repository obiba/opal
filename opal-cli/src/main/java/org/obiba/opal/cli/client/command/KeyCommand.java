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

import java.security.KeyStoreException;

import org.obiba.opal.cli.client.command.options.CertificateInfo;
import org.obiba.opal.cli.client.command.options.KeyCommandOptions;
import org.obiba.opal.core.crypt.StudyKeyStore;
import org.obiba.opal.core.service.StudyKeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Provides key management allowing for key creation, deletion, importing and exporting of keys.
 */
@CommandUsage(description = "Encryption key pairs creation, import or deletion.\n\nSyntax: keystore --alias NAME (--delete | --algo NAME --size INT | --private FILE [--certificate FILE])")
public class KeyCommand extends AbstractCommand<KeyCommandOptions> {

  @Autowired
  private StudyKeyStoreService studyKeyStoreService;

  public void execute() {
    if(options.isDelete()) {
      StudyKeyStore studyKeyStore = studyKeyStoreService.getStudyKeyStore(StudyKeyStoreService.DEFAULT_STUDY_ID);
      if(studyKeyStore == null) {
        System.out.println("Keystore doesn't exist");
      } else {
        if(studyKeyStoreService.aliasExists(options.getAlias())) {
          studyKeyStoreService.deleteKey(options.getAlias());
        } else {
          System.out.println("The alias [" + options.getAlias() + "] does not exist.");
        }
      }
    } else if(options.isAlgorithm() && options.isSize()) {
      boolean createKeyConfirmation = true;
      if(aliasAlreadyExists(options.getAlias())) {
        createKeyConfirmation = confirmKeyOverWrite();
      }
      if(createKeyConfirmation) {
        String certificateInfo = new CertificateInfo().getCertificateInfoAsString();
        System.console().printf("%s:\n", "Keystore creation");
        studyKeyStoreService.createOrUpdateKey(options.getAlias(), options.getAlgorithm(), options.getSize(), certificateInfo);
      }
    } else if(options.isPrivate()) {
      boolean createKeyConfirmation = true;
      if(aliasAlreadyExists(options.getAlias())) {
        createKeyConfirmation = confirmKeyOverWrite();
      }
      if(createKeyConfirmation) {
        if(options.isCertificate()) {
          studyKeyStoreService.importKey(options.getAlias(), options.getPrivate(), options.getCertificate());
        } else {
          String certificateInfo = new CertificateInfo().getCertificateInfoAsString();
          studyKeyStoreService.importKey(options.getAlias(), options.getPrivate(), certificateInfo);
        }
      }
    } else {
      unrecognizedOptionsHelp();
    }
  }

  private boolean confirmKeyOverWrite() {
    if(confirm("A key already exists for the alias [" + options.getAlias() + "]. Would you like to overwrite it?")) {
      return confirm("Please confirm a second time. Are you sure you want to overwrite the key with the alias [" + options.getAlias() + "]?");
    }
    return false;
  }

  private boolean confirm(String question) {
    System.console().printf("%s\n", question);
    String ans = "no";
    do {
      String answer = System.console().readLine("  [%s]:  ", ans);
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
      System.out.println("not loaded");
      throw new RuntimeException(e);
    }
  }

  private void unrecognizedOptionsHelp() {
    System.out.println("This combination of options was unrecognized." + "\nSyntax:" //
        + "\n  key --alias NAME (--delete | --algo NAME --size INT | --private FILE [--certificate FILE])" //
        + "\nExamples:" //
        + "\n  key --alias onyx --algo RSA --size 2048" //
        + "\n  key --alias onyx --private private_key.pem --certificate public_key.pem" //
        + "\n  key -alias onyx --delete"); //
  }

}