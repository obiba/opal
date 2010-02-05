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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

import org.obiba.opal.cli.client.command.options.PublicCommandOptions;
import org.obiba.opal.core.crypt.StudyKeyStore;
import org.obiba.opal.core.service.StudyKeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import sun.misc.BASE64Encoder;

/**
 * Exports public key certificates.
 */
@CommandUsage(description = "Export the public key certificate for the specified key pair alias.\n\nSyntax: certificate --alias NAME [--out FILE]")
public class PublicCommand extends AbstractCommand<PublicCommandOptions> {

  @Autowired
  private StudyKeyStoreService studyKeyStoreService;

  public void execute() {
    StudyKeyStore studyKeyStore = studyKeyStoreService.getStudyKeyStore(StudyKeyStoreService.DEFAULT_STUDY_ID);
    if(studyKeyStore == null) {
      System.out.println("Keystore doesn't exist");
    } else if(options.isOut()) {
      FileWriter fstream;
      BufferedWriter out = null;
      try {
        fstream = new FileWriter(options.getOut());
        out = new BufferedWriter(fstream);
        out.write(getCertificateAsString(studyKeyStore));
        System.console().printf("%s\n", "Certificate written to the file [" + options.getOut() + "]");
      } catch(IOException e) {
        throw new RuntimeException(e);
      } finally {
        try {
          out.close();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      System.console().printf("%s", getCertificateAsString(studyKeyStore));
    }

  }

  private String getCertificateAsString(StudyKeyStore studyKeyStore) {
    Assert.notNull(studyKeyStore, "studyKeyStore can not be null");
    StringBuilder sb = new StringBuilder();
    try {
      Certificate certificate = studyKeyStore.getKeyStore().getCertificate(options.getAlias());
      BASE64Encoder encoder = new BASE64Encoder();
      String encoded = encoder.encode(certificate.getEncoded());
      sb.append("-----BEGIN CERTIFICATE-----\n");
      sb.append(encoded).append("\n");
      sb.append("-----END CERTIFICATE-----\n");
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    } catch(CertificateEncodingException e) {
      throw new RuntimeException(e);
    }
    return sb.toString();
  }

}