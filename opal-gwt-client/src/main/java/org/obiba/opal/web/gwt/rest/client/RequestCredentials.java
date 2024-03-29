/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.rest.client;

import java.util.Date;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import org.obiba.opal.web.security.OpalAuth;

import com.google.gwt.core.client.impl.Md5Digest;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Cookies;

/**
 *
 */
public class RequestCredentials {

  private String username;

  public RequestBuilder provideCredentials(RequestBuilder builder) {
    /*if(hasOpalCredentials()) {
      builder.setHeader(OpalAuth.CREDENTIALS_HEADER, extractOpalCredentials());
    }*/
    return builder;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  /**
   * Adds credentials to allow making a request to the specified URL without using AJAX.
   *
   * @param url the URL to request (GET, POST)
   */
  public void provideCredentials(String url) {
    /*if(hasCredentials()) {
      Md5Digest digest = new Md5Digest();
      digest.update(extractOpalCredentials().getBytes());
      String urlToHash = url;
      int queryIdx = url.indexOf('?');
      if(queryIdx != -1) {
        // remove query string
        urlToHash = url.substring(0, queryIdx);
      }
      String urlHash = toHexString(digest.digest(urlToHash.getBytes()));
      long time = new Date().getTime();
      // Cookie will be valid for 1 second
      Cookies.setCookie(OPALRID, urlHash, new Date(time + 1000), null, "/", false);
    }*/
  }

  /**
   * Returns true when the credentials we currently hold have expired or are no longer valid after making a request to
   * the server. Note that if we did not provide credentials in the previous request, this method will return false.
   *
   * @param request the request that was issued to the server and to which we provided credentials.
   * @return
   */
  public boolean hasExpired(RequestBuilder request) {
    return request.getHeader(OpalAuth.CREDENTIALS_HEADER) != null && !hasCredentials();
  }

  /**
   * Returns true when the client has credentials sent by the server. This does not guarantee that said credentials are
   * valid. It simply tests the presence of some credentials.
   *
   * @return true if we currently have credentials to offer to the server.
   */
  public boolean hasCredentials() {
    return !Strings.isNullOrEmpty(username);//hasOpalCredentials() || hasObibaCredentials();
  }

  public void invalidate() {
    username = "";
  }

  private static String toHexString(byte... bytes) {
    StringBuilder buf = new StringBuilder();
    for(byte aByte : bytes) {
      String hex = Integer.toHexString(aByte & 0xFF);
      buf.append("00".substring(hex.length())).append(hex);
    }
    return buf.toString();
  }

}
