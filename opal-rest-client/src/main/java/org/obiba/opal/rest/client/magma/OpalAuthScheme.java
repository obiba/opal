/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.rest.client.magma;

import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.utils.Base64;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;


public class OpalAuthScheme {

  public static String SCHEME_NAME = "X-Opal-Auth";

  /**
   * Returns a basic <tt>Authorization</tt> header value for the given {@link Credentials} and charset.
   *
   * @param credentials The credentials to encode.
   * @return a basic authorization header
   */
  public static Header authenticate(Credentials credentials) {
    if(credentials == null) throw new IllegalArgumentException("credentials may not be null");

    String tmp = credentials.getUserPrincipal().getName() + ":" +
        (credentials.getPassword() == null ? "null" : String.valueOf(credentials.getPassword()));

    byte[] encodedCreds = Base64.encodeBase64(tmp.getBytes());

    return new BasicHeader("Authorization", SCHEME_NAME + " " + new String(encodedCreds, 0, encodedCreds.length, StandardCharsets.US_ASCII));
  }

}
