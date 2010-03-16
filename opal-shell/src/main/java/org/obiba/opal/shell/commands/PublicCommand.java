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
import org.obiba.opal.core.domain.unit.UnitKeyStore;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.shell.commands.options.PublicCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Exports public key certificates.
 */
@CommandUsage(description = "Exports the public key certificate for the specified key pair alias.", syntax = "Syntax: certificate [--unit NAME] --alias NAME [--out FILE]")
public class PublicCommand extends AbstractOpalRuntimeDependentCommand<PublicCommandOptions> {
  //
  // Instance Variables
  //

  @Autowired
  private UnitKeyStoreService unitKeyStoreService;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  public void execute() {
    UnitKeyStore unitKeyStore = getUnitKeyStore();
    if(unitKeyStore == null) {
      getShell().printf("Keystore doesn't exist\n");
      return;
    }

    Writer certificateWriter = null;
    try {
      certificateWriter = getCertificateWriter();
      writeCertificate(unitKeyStore, certificateWriter);
      printResult(certificateWriter);
    } catch(FileSystemException e) {
      getShell().printf("%s in an invalid output file.  Please make sure that you have specified a valid path.", options.getOut());
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      StreamUtil.silentSafeClose(certificateWriter);
    }
  }

  //
  // Methods
  //

  private UnitKeyStore getUnitKeyStore() {
    UnitKeyStore unitKeyStore = null;
    if(options.isUnit()) {
      FunctionalUnit unit = getOpalConfiguration().getFunctionalUnit(options.getUnit());
      unitKeyStore = unit.getKeyStore();
    } else {
      unitKeyStore = unitKeyStoreService.getUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);
    }
    return unitKeyStore;
  }

  private Writer getCertificateWriter() throws FileSystemException {
    Writer certificateWriter;
    if(options.isOut()) {
      FileObject outputFile = getFileSystemRoot().resolveFile(options.getOut());
      certificateWriter = new OutputStreamWriter(outputFile.getContent().getOutputStream());

    } else {
      certificateWriter = new StringWriter();
    }
    return certificateWriter;
  }

  private void writeCertificate(UnitKeyStore studyKeyStore, Writer writer) throws IOException {
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

  private void printResult(Writer certificateWriter) {
    if(options.isOut()) {
      getShell().printf("Certificate written to the file [%s]\n", options.getOut());
    } else {
      getShell().printf(((StringWriter) certificateWriter).getBuffer().toString());
    }
  }
}