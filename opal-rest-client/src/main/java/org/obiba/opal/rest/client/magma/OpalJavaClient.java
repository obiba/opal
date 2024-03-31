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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nullable;

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
import org.apache.http.conn.ssl.PrivateKeyDetails;
import org.apache.http.conn.ssl.PrivateKeyStrategy;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;

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

  // 5MB
  private static final int MAX_OBJECT_SIZE_BYTES = 1024 * 1024 * 5;

  private static final int HTTPS_PORT = 443;

  private final URI opalURI;

  private KeyStore keyStore;

  private final Credentials credentials;

  private final String token;

  private int soTimeout = DEFAULT_SO_TIMEOUT;

  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

  private HttpClient client;

  private BasicHttpContext ctx;

  private ManagedHttpCacheStorage cacheStorage;

  private final ExtensionRegistryFactory extensionRegistryFactory = new ExtensionRegistryFactory();

  private File cacheFolder;

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
    this.credentials = new UsernamePasswordCredentials(username, password);
    this.token = null;
    this.keyStore = keyStore;
  }

  /**
   * @see CoreConnectionPNames#SO_TIMEOUT
   */
  public void setSoTimeout(Integer soTimeout) {
    this.soTimeout = soTimeout == null ? DEFAULT_SO_TIMEOUT : soTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
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
    if(keyStore == null && credentials != null)
      httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);

    httpClient.getParams().setParameter(ClientPNames.HANDLE_AUTHENTICATION, Boolean.TRUE);
    httpClient.getParams()
        .setParameter(AuthPNames.TARGET_AUTH_PREF, Collections.singletonList(OpalAuthScheme.SCHEME_NAME));
    httpClient.getParams().setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME,
        OpalClientConnectionManagerFactory.class.getName());
    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
    httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(DEFAULT_MAX_ATTEMPT, false));
    httpClient.getAuthSchemes().register(OpalAuthScheme.SCHEME_NAME, new OpalAuthScheme.Factory());

    try {
      httpClient.getConnectionManager().getSchemeRegistry()
          .register(new Scheme("https", HTTPS_PORT, getSocketFactory()));
    } catch(NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
    client = enableCaching(httpClient);

    ctx = new BasicHttpContext();
    ctx.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
  }

  private SchemeSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContextBuilder builder = SSLContexts.custom().useTLS();
    try {
      builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
    } catch(KeyStoreException e) {
      log.error("Unable to set SSL trust manager: {}", e.getMessage(), e);
    }

    if(keyStore != null) {
      try {
        builder.loadKeyMaterial(keyStore, credentials.getPassword().toCharArray(), new PrivateKeyStrategy() {
          @Override
          public String chooseAlias(Map<String, PrivateKeyDetails> aliases, Socket socket) {
            return credentials.getUserPrincipal().getName();
          }
        });
      } catch(KeyStoreException | UnrecoverableKeyException e) {
        log.error("Unable to set SSL key manager: {}", e.getMessage(), e);
      }
    }

    return new SSLSocketFactory(builder.build(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
  }

  public void close() {
    if(client != null) {
      log.info("Disconnecting from Opal: {}", opalURI);
      getClient().getConnectionManager().shutdown();
    }
    if(cacheStorage != null) {
      cacheStorage.close();
      cacheStorage.shutdown();
    }
    if (cacheFolder != null && cacheFolder.exists()) {
      delete(cacheFolder);
      cacheFolder = null;
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

  public <T extends Message> T getResource(Class<T> messageType, URI uri, Message.Builder builder) {
    try {
      return readResource(messageType, uri, get(uri), builder);
    } catch(IOException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public <T extends Message> T postResource(Class<T> messageType, URI uri, Message.Builder builder, Message message) {
    try {
      return readResource(messageType, uri, post(uri, message), builder);
    } catch(IOException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public <T extends Message> T readResource(Class<T> messageType, URI uri, HttpResponse response, Message.Builder builder) {
    InputStream is = null;
    try {
      if(response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
        EntityUtils.consume(response.getEntity());
        throw new RestRuntimeException(uri, response.getStatusLine());
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
    if(keyStore == null) {
      if (credentials != null)
        msg.addHeader(OpalAuthScheme.authenticate(credentials, AuthParams.getCredentialCharset(msg.getParams()), false));
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

  private HttpClient enableCaching(HttpClient httpClient) {
    CacheConfig config = new CacheConfig();
    config.setSharedCache(false);
    config.setMaxObjectSizeBytes(MAX_OBJECT_SIZE_BYTES);
    cacheFolder = Files.createTempDir();
    return new CachingHttpClient(httpClient, new FileResourceFactory(cacheFolder),
        cacheStorage = new ManagedHttpCacheStorage(config), config);
  }

  private void cleanupCache() {
    if(cacheStorage != null) {
      cacheStorage.cleanResources();
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
