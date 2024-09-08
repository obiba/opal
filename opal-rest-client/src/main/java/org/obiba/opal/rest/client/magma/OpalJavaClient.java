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

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import jakarta.annotation.Nullable;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Java client for Opal RESTful services.
 */
public class OpalJavaClient {

  private static final Logger log = LoggerFactory.getLogger(OpalJavaClient.class);

  // Don't wait indefinitely for packets.
  // 10 minutes
  public static final int DEFAULT_SO_TIMEOUT = 10 * 60 * 1000;

  public static final int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;

  public static final int DEFAULT_MAX_ATTEMPT = 5;

  private final URI opalURI;

  private KeyStore keyStore;

  private final Credentials credentials;

  private final String token;

  private int soTimeout = DEFAULT_SO_TIMEOUT;

  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

  private CloseableHttpClient client;

  private final ExtensionRegistryFactory extensionRegistryFactory = new ExtensionRegistryFactory();

  /**
   * Authenticate by username/password.
   * @param uri
   * @param username user principal name
   * @param password user password
   * @throws URISyntaxException
   */
  public OpalJavaClient(String uri, String username, String password) throws URISyntaxException {
    this(uri, null, username, password);
  }

  /**
   * Authenticate by token.
   * @param uri
   * @param token personal access token
   * @throws URISyntaxException
   */
  public OpalJavaClient(String uri, String token) throws URISyntaxException {
    if(uri == null) throw new IllegalArgumentException("uri cannot be null");
    if(token == null) throw new IllegalArgumentException("token cannot be null");

    this.opalURI = new URI(uri.endsWith("/") ? uri : uri + "/");
    this.credentials = null;
    this.token = token;
  }

  /**
   * Authenticate by SSL 2-way encryption if a key store is provided, else authenticate by username/password.
   * @param uri
   * @param keyStore
   * @param username key store alias or user principal name (if keyStore is null)
   * @param password key store password or user password (if keyStore is null)
   * @throws URISyntaxException
   */
  public OpalJavaClient(String uri, @Nullable KeyStore keyStore, String username, String password) throws URISyntaxException {
    if(uri == null) throw new IllegalArgumentException("uri cannot be null");
    if(username == null) throw new IllegalArgumentException("username cannot be null");
    if(password == null) throw new IllegalArgumentException("password cannot be null");

    this.opalURI = new URI(uri.endsWith("/") ? uri : uri + "/");
    this.credentials = new UsernamePasswordCredentials(username, password.toCharArray());
    this.token = null;
    this.keyStore = keyStore;
  }

  public void setSoTimeout(Integer soTimeout) {
    this.soTimeout = soTimeout == null ? DEFAULT_SO_TIMEOUT : soTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  private CloseableHttpClient getClient() {
    if(client == null) {
      createClient();
    }
    return client;
  }

  private void createClient() {
    log.info("Connecting to Opal: {}", opalURI);
    client = createHttpClient();
  }

  public void close() {
    if(client != null) {
      log.info("Disconnecting from Opal: {}", opalURI);
      try {
        getClient().close();
      } catch (IOException e) {
        // ignore
      }
      ;
    }
  }

  public UriBuilder newUri() {
    return new UriBuilder(opalURI);
  }

  public UriBuilder newUri(URI root) {
    String rootPath = root.getPath();
    if(!rootPath.endsWith("/")) {
      try {
        String authority = root.getHost();
        if (root.getPort()>0) authority = authority + ":" + root.getPort();
        return new UriBuilder(new URI(root.getScheme(), authority, rootPath + "/", root.getQuery(), root.getFragment()));
      } catch(URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
    return new UriBuilder(root);
  }

  @SuppressWarnings("unchecked")
  public <T extends Message> List<T> getResources(Class<T> messageType, URI uri, Message.Builder builder) {
    List<T> resources = new ArrayList<>();
    InputStream is = null;
    Message.Builder messageBuilder = builder;

    try (CloseableHttpResponse response = get(uri)) {
      if(response.getCode() >= HttpStatus.SC_BAD_REQUEST) {
        EntityUtils.consume(response.getEntity());
        throw new RuntimeException(response.getReasonPhrase());
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

  public <T extends Message> T getResource(Class<T> messageType, URI uri, Message.Builder builder) {
    try (CloseableHttpResponse response = get(uri)) {
      return readResource(messageType, uri, response, builder);
    } catch(IOException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public <T extends Message> T postResource(Class<T> messageType, URI uri, Message.Builder builder, Message message) {
    try (CloseableHttpResponse response = post(uri, message)) {
      return readResource(messageType, uri, response, builder);
    } catch(IOException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public <T extends Message> T readResource(Class<T> messageType, URI uri, CloseableHttpResponse response, Message.Builder builder) {
    InputStream is = null;
    try {
      if(response.getCode() >= HttpStatus.SC_BAD_REQUEST) {
        EntityUtils.consume(response.getEntity());
        throw new RestRuntimeException(uri, new StatusLine(response));
      }
      is = response.getEntity().getContent();
      ExtensionRegistry extensionRegistry = extensionRegistryFactory.forMessage((Class<Message>) messageType);
      return (T) builder.mergeFrom(is, extensionRegistry).build();
    } catch(IOException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      closeQuietly(is);
    }
  }

  public CloseableHttpResponse put(URI uri, Message msg) throws IOException {
    HttpPut put = new HttpPut(uri);
    put.setEntity(new ByteArrayEntity(asByteArray(msg), ContentType.create("application/x-protobuf")));
    return execute(put);
  }

  public CloseableHttpResponse post(URI uri, Message msg) throws IOException {
    HttpPost post = new HttpPost(uri);
    ByteArrayEntity e = new ByteArrayEntity(asByteArray(msg), ContentType.create("application/x-protobuf"));
    post.setEntity(e);
    return execute(post);
  }

  public CloseableHttpResponse post(URI uri, Iterable<? extends Message> msg) throws IOException {
    HttpPost post = new HttpPost(uri);
    ByteArrayEntity e = new ByteArrayEntity(asByteArray(msg), ContentType.create("application/x-protobuf"));
    post.setEntity(e);
    return execute(post);
  }

  public CloseableHttpResponse post(URI uri, String entity) throws IOException {
    HttpPost post = new HttpPost(uri);
    post.setEntity(new StringEntity(entity));
    return execute(post);
  }

  public CloseableHttpResponse post(URI uri, File file) throws IOException {
    HttpPost post = new HttpPost(uri);
    HttpEntity me = MultipartEntityBuilder.create().addBinaryBody("fileToUpload", file).build();
    post.setEntity(me);
    return execute(post);
  }

  public CloseableHttpResponse get(URI uri) throws IOException {
    return execute(new HttpGet(uri));
  }

  public CloseableHttpResponse delete(URI uri) throws IOException {
    return execute(new HttpDelete(uri));
  }

  private CloseableHttpClient createHttpClient() {
    HttpClientBuilder builder = HttpClients.custom();

    // Set the connection manager
    try {
      SSLConnectionSocketFactory sslsf = getSocketFactory();
      Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
          .register("https", sslsf)
          .register("http", new PlainConnectionSocketFactory())
          .build();
      PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
      connectionManager.setMaxTotal(50);  // Maximum total connections
      connectionManager.setDefaultMaxPerRoute(10);  // Maximum connections per route (endpoint)
      connectionManager.setValidateAfterInactivity(Timeout.ofSeconds(30)); // Time after which idle connections will be validated
      builder.setConnectionManager(connectionManager);
    } catch(NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }

    // Set the cookie store
    CookieStore cookieStore = new BasicCookieStore();
    builder.setDefaultCookieStore(cookieStore);

    // Set some timeouts
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout)) // Set connection timeout
        .setResponseTimeout(Timeout.ofMilliseconds(soTimeout)) // Set response (socket) timeout
        .build();
    builder.setDefaultRequestConfig(requestConfig);

    // Set the retry strategy
    DefaultHttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy(DEFAULT_MAX_ATTEMPT, Timeout.ofSeconds(1));
    builder.setRetryStrategy(retryStrategy);

    return builder.build();
  }

  /**
   * Do not check anything from the remote host (Opal server is trusted).
   * @return
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException
   */
  private SSLConnectionSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
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

    return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
  }

  private CloseableHttpResponse execute(HttpUriRequest msg) throws IOException {
    msg.addHeader("Accept", "application/x-protobuf, text/html");
    authenticate(msg);
    try {
      log.debug("{} {}", msg.getMethod(), msg.getUri());
    } catch (URISyntaxException e) {
      // ignore
    }
    if(log.isTraceEnabled()) {
      for(Header allHeader : msg.getHeaders()) {
        log.trace("  {} {}", allHeader.getName(), allHeader.getValue());
      }
    }
    return getClient().execute(msg);
  }

  private void authenticate(HttpMessage msg) {
    if(keyStore == null) {
      if (credentials != null)
        msg.addHeader(OpalAuthScheme.authenticate(credentials));
      else
        msg.addHeader(OpalAuthScheme.SCHEME_NAME, token);
    }
  }

  void closeQuietly(Closeable closable) {
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

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void delete(File resource) {
    if(resource.isDirectory()) {
      File[] childFiles = resource.listFiles();
      if(childFiles != null) {
        for(File child : childFiles) {
          delete(child);
        }
      }
    }
    resource.delete();
  }

  protected static final class ExtensionRegistryFactory {

    private final Map<Class<?>, ExtensionRegistry> registryCache = new HashMap<>();

    private final Map<Class<?>, Method> methodCache = new HashMap<>();

    ExtensionRegistry forMessage(Class<Message> messageType) {
      if(messageType == null) throw new IllegalArgumentException("messageType cannot be null");

      Class<?> enclosingType = messageType.getEnclosingClass();
      if(!registryCache.containsKey(enclosingType)) {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        invokeStaticMethod(extractStaticMethod("registerAllExtensions", methodCache, messageType.getEnclosingClass(),
            ExtensionRegistry.class), registry);
        registryCache.put(enclosingType, registry);
      }
      return registryCache.get(enclosingType);
    }
  }

  private static Object invokeStaticMethod(Method method, Object... arguments) {
    if(method == null) throw new IllegalArgumentException("method cannot be null");

    try {
      return method.invoke(null, arguments);
    } catch(RuntimeException | InvocationTargetException | IllegalAccessException e) {
      log.error("Error invoking '{}' method for type {}", method.getName(), method.getDeclaringClass().getName(), e);
      throw new RuntimeException(
          "Error invoking '" + method.getName() + "' method for type " + method.getDeclaringClass().getName());
    }
  }

  private static Method extractStaticMethod(String methodName, Map<Class<?>, Method> methodCache, Class<?> type,
      Class<?>... arguments) {
    if(methodName == null) throw new IllegalArgumentException("methodName cannot be null");
    if(methodCache == null) throw new IllegalArgumentException("methodCache cannot be null");
    if(type == null) throw new IllegalArgumentException("type cannot be null");

    if(!methodCache.containsKey(type)) {
      try {
        methodCache.put(type, type.getMethod(methodName, arguments));
      } catch(SecurityException e) {
        log.error("Error getting '{}' method from type {}", methodName, type.getName(), e);
        throw new RuntimeException("Error getting '" + methodName + "' method from type " + type.getName());
      } catch(NoSuchMethodException e) {
        throw new IllegalStateException(
            "The type " + type.getName() + " does not define a '" + methodName + "' static method.");
      }
    }
    return methodCache.get(type);
  }
}
