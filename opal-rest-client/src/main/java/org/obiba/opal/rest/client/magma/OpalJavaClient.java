package org.obiba.opal.rest.client.magma;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import com.google.protobuf.Message;

/**
 * A Java client for Opal RESTful services.
 */
public class OpalJavaClient {

  private final URI opalURI;

  private final HttpClient client;

  private final BasicHttpContext ctx;

  private final BasicCookieStore cs;

  public OpalJavaClient(String uri, String username, String password) throws URISyntaxException {
    if(uri == null) throw new IllegalArgumentException("uri cannot be null");
    if(username == null) throw new IllegalArgumentException("username cannot be null");
    if(password == null) throw new IllegalArgumentException("password cannot be null");

    this.opalURI = new URI(uri.endsWith("/") ? uri : uri + "/");

    DefaultHttpClient httpClient = new DefaultHttpClient();
    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    httpClient.getParams().setParameter("http.protocol.handle-authentication", Boolean.TRUE);
    httpClient.getParams().setParameter("http.auth.target-scheme-pref", Collections.singletonList(OpalAuthScheme.NAME));
    httpClient.getAuthSchemes().register(OpalAuthScheme.NAME, new OpalAuthScheme.Factory());
    ctx = new BasicHttpContext();
    ctx.setAttribute(ClientContext.AUTH_SCHEME_PREF, Collections.singletonList(OpalAuthScheme.NAME));
    ctx.setAttribute(ClientContext.COOKIE_STORE, cs = new BasicCookieStore());
    this.client = httpClient;
  }
  
  public UriBuilder newUri() {
    return new UriBuilder(this.opalURI);
  }

  public UriBuilder newUri(URI root) {
    String rootPath = root.getPath();
    if(rootPath.endsWith("/") == false) {
      try {
        return new UriBuilder(new URI(root.getScheme(), root.getHost(), rootPath + "/",root.getQuery(), root.getFragment()));
      } catch(URISyntaxException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return new UriBuilder(root);
  }

  @SuppressWarnings("unchecked")
  public <T extends Message> List<T> getResources(Class<T> messageType, URI uri, Message.Builder builder) {
    ArrayList<T> resources = new ArrayList<T>();
    InputStream is = null;
    Message.Builder messageBuilder = builder;

    try {
      HttpResponse response = get(uri);
      is = response.getEntity().getContent();

      while(messageBuilder.mergeDelimitedFrom(is)) {
        T message = (T) messageBuilder.build();
        resources.add(message);
        messageBuilder = message.newBuilderForType();
      }
      return resources;
    } catch(IOException e) {
      e.printStackTrace();
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
      is = response.getEntity().getContent();
      return (T) builder.mergeFrom(is).build();
    } catch(IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      closeQuietly(is);
    }
  }

  public HttpResponse get(URI uri) throws ClientProtocolException, IOException {
    HttpGet get = new HttpGet(uri);
    get.addHeader("Accept", "application/x-protobuf");
    authenticate(get);
    return client.execute(get, ctx);
  }

  private void authenticate(HttpMessage msg) {
    for(Cookie c : cs.getCookies()) {
      if(c.getName().equalsIgnoreCase("opalsid")) {
        msg.addHeader(OpalAuthScheme.NAME, c.getValue());
      }
    }
  }

  private void closeQuietly(Closeable closable) {
    if(closable == null) return;
    try {
      closable.close();
    } catch(Throwable t) {
    }
  }

}
