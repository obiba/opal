/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd;

import java.util.logging.Level;

import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.restlet.Client;
import org.restlet.engine.Engine;
import org.restlet.ext.httpclient.HttpClientHelper;

/**
 * Extends {@code HttpClientHelper} to allow configuring the {@code hostnameVerifier} property of the @
 * SSLSocketFactory}
 * @see <a href="http://hc.apache.org/httpcomponents-client/tutorial/html/connmgmt.html#d4e514">HTTP Client Hostname Verification</a>
 * @see <a href="http://restlet.tigris.org/issues/show_bug.cgi?id=1078">Restlet Feature Request</a>
 */
public class ExtendedHttpClientHelper extends HttpClientHelper {

  /**
   * @param client
   */
  public ExtendedHttpClientHelper(Client client) {
    super(client);
  }

  @Override
  protected void configure(SchemeRegistry schemeRegistry) {
    super.configure(schemeRegistry);
    if(getHostnameVerifier() != null) {
      try {
        X509HostnameVerifier hostnameVerifier = (X509HostnameVerifier) Engine.loadClass(getHostnameVerifier()).newInstance();
        ((SSLSocketFactory) schemeRegistry.getScheme("https").getSocketFactory()).setHostnameVerifier(hostnameVerifier);
      } catch(Exception e) {
        getLogger().log(Level.WARNING, "An error occurred during the instantiation of the hostname verifier.", e);
      }
    }
  }

  protected String getHostnameVerifier() {
    return getHelpedParameters().getFirstValue("hostnameVerifier", null);
  }

}
