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

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

import org.bouncycastle.openssl.PEMWriter;
import org.obiba.core.util.StreamUtil;
import org.obiba.opal.core.crypt.StudyKeyStore;
import org.obiba.opal.core.service.StudyKeyStoreService;
import org.obiba.opal.shell.commands.options.PublicCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Exports public key certificates.
 */
@CommandUsage(description = "Exports the public key certificate for the specified key pair alias.", syntax = "Syntax: certificate --alias NAME [--out FILE]")
public class PublicCommand extends AbstractCommand<PublicCommandOptions> {

  @Autowired
  private StudyKeyStoreService studyKeyStoreService;

  public void execute() {
    StudyKeyStore studyKeyStore = studyKeyStoreService.getStudyKeyStore(StudyKeyStoreService.DEFAULT_STUDY_ID);
    if(studyKeyStore == null) {
      getShell().printf("Keystore doesn't exist\n");
      return;
    }
    Writer certificateWriter = null;
    try {
      if(options.isOut()) {
        certificateWriter = new FileWriter(options.getOut());
      } else {
        certificateWriter = new StringWriter();
      }
      writeCertificate(studyKeyStore, certificateWriter);
      if(options.isOut()) {
        getShell().printf("Certificate written to the file [%s]\n", options.getOut());
      } else {
        getShell().printf(((StringWriter) certificateWriter).getBuffer().toString());
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      StreamUtil.silentSafeClose(certificateWriter);
    }

  }

  private void writeCertificate(StudyKeyStore studyKeyStore, Writer writer) throws IOException {
    Assert.notNull(studyKeyStore, "studyKeyStore can not be null");
    String alias = options.getAlias();

    Certificate certificate;
    try {
      certificate = studyKeyStore.getKeyStore().getCertificate(alias);
      if(certificate == null) {
        getShell().printf("No certificate was found for alias '%s'.\n", alias);
      } else {
        PEMWriter pemWriter = new PEMWriter(writer);
        pemWriter.writeObject(certificate);
        pemWriter.flush();
      }
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }

}