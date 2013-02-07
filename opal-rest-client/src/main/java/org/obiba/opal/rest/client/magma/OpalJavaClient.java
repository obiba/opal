/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.rest.client.magma;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.client.cache.FileResourceFactory;
import org.apache.http.impl.client.cache.ManagedHttpCacheStorage;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.protobuf.Message;

/**
 * A Java client for Opal RESTful services.
 */
public class OpalJavaClient {

  private static final Logger log = LoggerFactory.getLogger(OpalJavaClient.class);

  // Don't wait indefinitely for packets.
  // 10 minutes
  public static final int DEFAULT_SO_TIMEOUT = 10 * 60 * 1000;

  public static final int DEFAULT_MAX_ATTEMPT = 5;

  // 5MB
  private static final int MAX_OBJECT_SIZE_BYTES = 1024 * 1024 * 5;

  private static final int HTTPS_PORT = 443;

  private final URI opalURI;

  private final Credentials credentials;

  private int soTimeout = DEFAULT_SO_TIMEOUT;

  private HttpClient client;

  private BasicHttpContext ctx;

  private ManagedHttpCacheStorage cacheStorage;

  public OpalJavaClient(String uri, String username, String password) throws URISyntaxException {
    if(uri == null) throw new IllegalArgumentException("uri cannot be null");
    if(username == null) throw new IllegalArgumentException("username cannot be null");
    if(password == null) throw new IllegalArgumentException("password cannot be null");

    opalURI = new URI(uri.endsWith("/") ? uri : uri + "/");
    credentials = new UsernamePasswordCredentials(username, password);
  }

  /**
   * @see CoreConnectionPNames#SO_TIMEOUT
   */
  public void setSoTimeout(Integer soTimeout) {
    this.soTimeout = soTimeout == null ? DEFAULT_SO_TIMEOUT : soTimeout;
  }

  private HttpClient getClient() {
    if(client == null) {
      createClient();
    }
    return client;
  }

  private void createClient() {
    log.info("Connecting to Opal: {}", opalURI);
    DefaultHttpClient httpClient = new DefaultHttpClient();
    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
    httpClient.getParams().setParameter(ClientPNames.HANDLE_AUTHENTICATION, Boolean.TRUE);
    httpClient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, Collections.singletonList(OpalAuthScheme.NAME));
    httpClient.getParams().setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME,
        OpalClientConnectionManagerFactory.class.getName());
    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
    httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(DEFAULT_MAX_ATTEMPT, false));
    httpClient.getAuthSchemes().register(OpalAuthScheme.NAME, new OpalAuthScheme.Factory());

    try {
      httpClient.getConnectionManager().getSchemeRegistry()
          .register(new Scheme("https", HTTPS_PORT, getSocketFactory()));
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(KeyManagementException e) {
      throw new RuntimeException(e);

    }
    client = enableCaching(httpClient);

    ctx = new BasicHttpContext();
    ctx.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
  }

  private SchemeSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
    // Accepts any SSL certificate
    TrustManager tm = new X509TrustManager() {

      @Override
      public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

      }

      @Override
      public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, new TrustManager[] { tm }, null);

    return new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
  }

  public void close() {
    if(client != null) {
      log.info("Disconnecting from Opal: {}", opalURI);
      log.debug("", new Exception());
      getClient().getConnectionManager().shutdown();
    }
  }

  public UriBuilder newUri() {
    return new UriBuilder(opalURI);
  }

  public UriBuilder newUri(URI root) {
    String rootPath = root.getPath();
    if(!rootPath.endsWith("/")) {
      try {
        return new UriBuilder(
            new URI(root.getScheme(), root.getHost() + ":" + root.getPort(), rootPath + "/", root.getQuery(),
                root.getFragment()));
      } catch(URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
    return new UriBuilder(root);
  }

  @SuppressWarnings("unchecked")
  public <T extends Message> List<T> getResources(Class<T> messageType, URI uri, Message.Builder builder) {
    List<T> resources = new ArrayList<T>();
    InputStream is = null;
    Message.Builder messageBuilder = builder;

    try {
      HttpResponse response = get(uri);
      if(response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
        EntityUtils.consume(response.getEntity());
        throw new RuntimeException(response.getStatusLine().getReasonPhrase());
      }
      is = response.getEntity().getContent();

      while(messageBuilder.mergeDelimitedFrom(is)) {
        T message = (T) messageBuilder.build();
        resources.add(message);
        messageBuilder = message.newBuilderForType();
      }
      return resources;
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      closeQuietly(is);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Message> T getResource(Class<T> messageType, URI uri, Message.Builder builder) {
    InputStream is = null;
    try {
      HttpResponse response = get(uri);
      if(response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
        EntityUtils.consume(response.getEntity());
        throw new RuntimeException(response.getStatusLine().getReasonPhrase());
      }
      is = response.getEntity().getContent();
      return (T) builder.mergeFrom(is).build();
    } catch(IOException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      closeQuietly(is);
    }
  }

  public HttpResponse put(URI uri, Message msg) throws IOException {
    HttpPut put = new HttpPut(uri);
    put.setEntity(new ByteArrayEntity(asByteArray(msg)));
    return execute(put);
  }

  public HttpResponse post(URI uri, Message msg) throws IOException {
    HttpPost post = new HttpPost(uri);
    ByteArrayEntity e = new ByteArrayEntity(asByteArray(msg));
    e.setContentType("application/x-protobuf");
    post.setEntity(e);
    return execute(post);
  }

  public HttpResponse post(URI uri, Iterable<? extends Message> msg) throws IOException {
    HttpPost post = new HttpPost(uri);
    ByteArrayEntity e = new ByteArrayEntity(asByteArray(msg));
    e.setContentType("application/x-protobuf");
    post.setEntity(e);
    return execute(post);
  }

  public HttpResponse post(URI uri, String entity) throws IOException {
    HttpPost post = new HttpPost(uri);
    post.setEntity(new StringEntity(entity));
    return execute(post);
  }

  public HttpResponse post(URI uri, File file) throws IOException {
    HttpPost post = new HttpPost(uri);
    MultipartEntity me = new MultipartEntity();
    me.addPart("fileToUpload", new FileBody(file));
    post.setEntity(me);
    return execute(post);
  }

  public HttpResponse get(URI uri) throws IOException {
    return execute(new HttpGet(uri));
  }

  public HttpResponse delete(URI uri) throws IOException {
    return execute(new HttpDelete(uri));
  }

  private HttpResponse execute(HttpUriRequest msg) throws IOException {
    msg.addHeader("Accept", "application/x-protobuf, text/html");
    authenticate(msg);
    log.debug("{} {}", msg.getMethod(), msg.getURI());
    if(log.isTraceEnabled()) {
      for(Header allHeader : msg.getAllHeaders()) {
        log.trace("  {} {}", allHeader.getName(), allHeader.getValue());
      }
    }
    try {
      return getClient().execute(msg, ctx);
    } finally {
      cleanupCache();
    }
  }

  private void authenticate(HttpMessage msg) {
    if(credentials != null) {
      msg.addHeader(OpalAuthScheme.authenticate(credentials, AuthParams.getCredentialCharset(msg.getParams()), false));
    }
  }

  private void closeQuietly(Closeable closable) {
    if(closable == null) return;
    try {
      closable.close();
    } catch(Throwable ignored) {
    }
  }

  private byte[] asByteArray(@SuppressWarnings("TypeMayBeWeakened") Message msg) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      msg.writeTo(baos);
      return baos.toByteArray();
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      closeQuietly(baos);
    }
  }

  private byte[] asByteArray(Iterable<? extends Message> msgs) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      for(Message msg : msgs) {
        msg.writeDelimitedTo(baos);
      }
      return baos.toByteArray();
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      closeQuietly(baos);
    }
  }

  private HttpClient enableCaching(HttpClient httpClient) {
    CacheConfig config = new CacheConfig();
    config.setSharedCache(false);
    config.setMaxObjectSizeBytes(MAX_OBJECT_SIZE_BYTES);
    return new CachingHttpClient(httpClient, new FileResourceFactory(Files.createTempDir()),
        cacheStorage = new ManagedHttpCacheStorage(config), config);
  }

  private void cleanupCache() {
    if(cacheStorage != null) {
      cacheStorage.cleanResources();
    }
  }

}
