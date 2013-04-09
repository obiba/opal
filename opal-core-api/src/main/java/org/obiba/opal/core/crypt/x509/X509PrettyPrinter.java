/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.crypt.x509;

import java.security.cert.X509Certificate;

public final class X509PrettyPrinter {

  private static final String FORMAT = //
      "Certificate:\n" + //
          "  Data:\n" + //
          "    Version: %d\n" + //
          "    Serial Number: %s\n" + //
          "    Signature Algorithm: %s\n" + //
          "    Issuer: %s\n" + //
          "    Validity:\n" + //
          "      Not Before: %s\n" + //
          "      Not After: %s\n" + //
          "    Subject: %s\n" + //
          "    Subject Public Key Info:\n" + //
          "      Public Key Algorithm: %s\n" + //
          "    X509v3 extensions: %d\n" + //
          "  Signature Algorithm: %s\n";

  private X509PrettyPrinter() {}

  public static String prettyPrint(X509Certificate x509) {
    if(x509 == null) throw new IllegalArgumentException("x509 cannot be null");
    return String.format(FORMAT, x509.getVersion(), x509.getSerialNumber(), x509.getSigAlgName(),
        x509.getIssuerX500Principal().getName(), x509.getNotBefore(), x509.getNotAfter(),
        x509.getSubjectX500Principal().getName(), x509.getPublicKey().getAlgorithm(), x509.getBasicConstraints(),
        x509.getSigAlgName());
  }

}
