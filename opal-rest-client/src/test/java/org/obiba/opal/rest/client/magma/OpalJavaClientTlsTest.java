/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.rest.client.magma;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * The client sends credentials or a token to the remote Opal: that server must be the one its URL names. A local
 * HTTPS server presents a self-signed certificate issued to "localhost" only.
 */
public class OpalJavaClientTlsTest {

  private static final char[] PASSWORD = "changeit".toCharArray();

  private static HttpsServer server;

  private static KeyStore serverKeyStore;

  @BeforeClass
  public static void startServer() throws Exception {
    serverKeyStore = KeyStore.getInstance("PKCS12");
    try (InputStream in = OpalJavaClientTlsTest.class.getResourceAsStream("/tls/server.p12")) {
      serverKeyStore.load(in, PASSWORD);
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(serverKeyStore, PASSWORD);
    SSLContext serverContext = SSLContext.getInstance("TLS");
    serverContext.init(kmf.getKeyManagers(), null, null);

    // every address "localhost" resolves to: the client tries them all and reports the last failure
    server = HttpsServer.create(new InetSocketAddress(0), 0);
    server.setHttpsConfigurator(new HttpsConfigurator(serverContext));
    server.createContext("/", exchange -> {
      exchange.sendResponseHeaders(200, 2);
      exchange.getResponseBody().write("ok".getBytes());
      exchange.close();
    });
    server.start();
  }

  @AfterClass
  public static void stopServer() {
    if(server != null) server.stop(0);
  }

  @Test(expected = SSLHandshakeException.class)
  public void anUntrustedCertificateIsRefusedByDefault() throws Exception {
    client("localhost").get(uri("localhost"));
  }

  @Test
  public void anUntrustedCertificateIsAcceptedWhenExplicitlyAllowed() throws Exception {
    OpalJavaClient client = client("localhost");
    client.setAllowInvalidCertificates(true);

    try (CloseableHttpResponse response = client.get(uri("localhost"))) {
      assertThat(response.getCode()).isEqualTo(200);
    }
  }

  @Test
  public void aTrustedCertificateOfTheNamedHostIsAccepted() throws Exception {
    OpalJavaClient client = client("localhost");
    client.setSslSocketFactory(trustingTheServer());

    try (CloseableHttpResponse response = client.get(uri("localhost"))) {
      assertThat(response.getCode()).isEqualTo(200);
    }
  }

  @Test(expected = SSLPeerUnverifiedException.class)
  public void aTrustedCertificateOfAnotherHostIsRefused() throws Exception {
    // the certificate names "localhost" only, the URL names the IP address
    OpalJavaClient client = client("127.0.0.1");
    client.setSslSocketFactory(trustingTheServer());

    client.get(uri("127.0.0.1"));
  }

  private static OpalJavaClient client(String host) throws Exception {
    return new OpalJavaClient("https://" + host + ":" + server.getAddress().getPort() + "/ws", "user", "password");
  }

  private static URI uri(String host) {
    return URI.create("https://" + host + ":" + server.getAddress().getPort() + "/ws/datasources");
  }

  /**
   * A socket factory of a trust store holding the server's own certificate, what Opal's credentials key store does.
   */
  private static SSLSocketFactory trustingTheServer() throws Exception {
    Certificate certificate = serverKeyStore.getCertificate("server");
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null, null);
    trustStore.setCertificateEntry("server", certificate);
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(trustStore);
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, tmf.getTrustManagers(), null);
    return context.getSocketFactory();
  }
}
