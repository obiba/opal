/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd.ssl;

import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

import org.jsslutils.sslcontext.X509SSLContextFactory;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.restlet.data.Parameter;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.util.Series;

/**
 * An {@code SslContextFactory} that allows two functional units to communicate through SSL.
 */
public class FunctionalUnitSslContextFactory extends SslContextFactory {

  private final X509SSLContextFactory x509Factory;

  public FunctionalUnitSslContextFactory(final UnitKeyStore keyManager, final UnitKeyStore trustedClient) {
    KeyStore trustManagerStore = trustedClient.getKeyStore();
    x509Factory = new X509SSLContextFactory(null, (String) null, trustManagerStore) {
      @Override
      protected KeyManager[] getRawKeyManagers() throws SSLContextFactoryException {
        return new KeyManager[] { new UnitKeyManager(keyManager) };
      }
    };
  }

  @Override
  public void init(Series<Parameter> parameters) {
  }

  @Override
  public SSLContext createSslContext() throws Exception {
    return x509Factory.buildSSLContext();
  }

}
