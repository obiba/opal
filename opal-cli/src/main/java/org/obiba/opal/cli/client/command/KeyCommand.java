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

import org.obiba.opal.cli.client.command.options.CertificateInfo;
import org.obiba.opal.cli.client.command.options.KeyCommandOptions;
import org.obiba.opal.core.service.StudyKeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides key management allowing for key creation, deletion, importing and exporting of keys.
 */
public class KeyCommand extends AbstractCommand<KeyCommandOptions> {

  @Autowired
  private StudyKeyStoreService studyKeyStoreService;

  public void execute() {
    if(options.isDelete()) {
      System.out.println("delete");
    } else if(options.isAlgorithm() && options.isSize()) {
      System.out.println("create key");
      // check if alias already exists. If yes, then confirm the overwrite.
      for(String id : studyKeyStoreService.getStudyIds()) {
        System.out.println(id);
      }
      CertificateInfo info = new CertificateInfo();
      System.out.println(info.getCertificateInfoAsString());
    } else if(options.isPrivate()) {
      System.out.println("import");
    } else {
      unrecognizedOptionsHelp();
    }
  }

  private void unrecognizedOptionsHelp() {
    System.out.println("This combination of options was unrecognized." + "\nSyntax:" //
        + "\n   opalkey key --alias=NAME (--delete | --algo=NAME --size=INT | --private=FILE [--certificate=FILE])" //
        + "\nExamples:" //
        + "\n   opalkey key --alias=onyx --algo=RSA --size=2048" //
        + "\n   opalkey key --alias=onyx --private=private_key.pem --certificate=public_key.pem" //
        + "\n  opalkey key -alias=onyx --delete"); //
  }

}