/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.sshd;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.KeyPair;

import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

/**
 * TODO Add javadoc
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class PEMGeneratorHostKeyProvider extends AbstractGeneratorHostKeyProvider {

  public PEMGeneratorHostKeyProvider() {
  }

  public PEMGeneratorHostKeyProvider(String path) {
    super(path);
  }

  public PEMGeneratorHostKeyProvider(String path, String algorithm) {
    super(path, algorithm);
  }

  public PEMGeneratorHostKeyProvider(String path, String algorithm, int keySize) {
    super(path, algorithm, keySize);
  }

  @Override
  protected KeyPair doReadKeyPair(InputStream is) throws Exception {
    PEMReader r = new PEMReader(new InputStreamReader(is));
    return (KeyPair) r.readObject();
  }

  @Override
  protected void doWriteKeyPair(KeyPair kp, OutputStream os) throws Exception {
    try(PEMWriter writer = new PEMWriter(new OutputStreamWriter(os))) {
      writer.writeObject(kp);
    }
  }

}
