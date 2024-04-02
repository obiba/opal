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

import jakarta.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BufferedHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EncodingUtils;

public class OpalAuthScheme extends BasicScheme {

  public static String SCHEME_NAME = "X-Opal-Auth";

  @Nullable
  @Override
  public String getRealm() {
    return null;
  }

  @Override
  public String getSchemeName() {
    return SCHEME_NAME;
  }

  /**
   * Produces basic authorization header for the given set of {@link Credentials}.
   *
   * @param credentials The set of credentials to be used for authentication
   * @param request The request being authenticated
   * @return a basic authorization string
   * @throws InvalidCredentialsException if authentication credentials are not valid or not applicable for this
   * authentication scheme
   * @throws AuthenticationException if authorization string cannot be generated due to an authentication failure
   */
  @Override
  public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
    if(credentials == null) throw new IllegalArgumentException("credentials may not be null");
    if(request == null) throw new IllegalArgumentException("request may not be null");

    String charset = AuthParams.getCredentialCharset(request.getParams());
    return authenticate(credentials, charset, isProxy());
  }

  /**
   * Returns a basic <tt>Authorization</tt> header value for the given {@link Credentials} and charset.
   *
   * @param credentials The credentials to encode.
   * @param charset The charset to use for encoding the credentials
   * @return a basic authorization header
   */
  public static Header authenticate(Credentials credentials, String charset, boolean proxy) {
    if(credentials == null) throw new IllegalArgumentException("credentials may not be null");
    if(charset == null) throw new IllegalArgumentException("charset may not be null");

    String tmp = credentials.getUserPrincipal().getName() + ":" +
        (credentials.getPassword() == null ? "null" : credentials.getPassword());

    byte[] base64password = Base64.encodeBase64(EncodingUtils.getBytes(tmp, charset));

    CharArrayBuffer buffer = new CharArrayBuffer(32);
    buffer.append(proxy ? AUTH.PROXY_AUTH_RESP : AUTH.WWW_AUTH_RESP);
    buffer.append(": ");
    buffer.append(SCHEME_NAME);
    buffer.append(" ");
    buffer.append(base64password, 0, base64password.length);

    return new BufferedHeader(buffer);
  }

  public static final class Factory implements AuthSchemeFactory {

    @Override
    public AuthScheme newInstance(HttpParams params) {
      return new OpalAuthScheme();
    }

  }
}
