package org.obiba.opal.rest.client.magma;

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
  
  public static final String NAME = "X-Opal-Auth";
  
  @Override
  public String getRealm() {
    return null;
  }

  @Override
  public String getSchemeName() {
    return NAME;
  }
  

  /**
   * Produces basic authorization header for the given set of {@link Credentials}.
   * 
   * @param credentials The set of credentials to be used for authentication
   * @param request The request being authenticated
   * @throws InvalidCredentialsException if authentication credentials are not 
   *   valid or not applicable for this authentication scheme
   * @throws AuthenticationException if authorization string cannot 
   *   be generated due to an authentication failure
   * 
   * @return a basic authorization string
   */
  public Header authenticate(
          final Credentials credentials, 
          final HttpRequest request) throws AuthenticationException {

      if (credentials == null) {
          throw new IllegalArgumentException("Credentials may not be null");
      }
      if (request == null) {
          throw new IllegalArgumentException("HTTP request may not be null");
      }
      
      String charset = AuthParams.getCredentialCharset(request.getParams());
      return authenticate(credentials, charset, isProxy());
  }
  
  /**
   * Returns a basic <tt>Authorization</tt> header value for the given 
   * {@link Credentials} and charset.
   * 
   * @param credentials The credentials to encode.
   * @param charset The charset to use for encoding the credentials
   * 
   * @return a basic authorization header
   */
  public static Header authenticate(
          final Credentials credentials, 
          final String charset, 
          boolean proxy) {
      if (credentials == null) {
          throw new IllegalArgumentException("Credentials may not be null"); 
      }
      if (charset == null) {
          throw new IllegalArgumentException("charset may not be null");
      }

      StringBuilder tmp = new StringBuilder();
      tmp.append(credentials.getUserPrincipal().getName());
      tmp.append(":");
      tmp.append((credentials.getPassword() == null) ? "null" : credentials.getPassword());

      byte[] base64password = Base64.encodeBase64(
              EncodingUtils.getBytes(tmp.toString(), charset));
      
      CharArrayBuffer buffer = new CharArrayBuffer(32);
      if (proxy) {
          buffer.append(AUTH.PROXY_AUTH_RESP);
      } else {
          buffer.append(AUTH.WWW_AUTH_RESP);
      }
      buffer.append(": ");
      buffer.append(NAME);
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
