package org.obiba.opal.rest.client.magma;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.TableDto.Builder;

import com.google.common.collect.ImmutableList;
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
    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, new String(password)));
    httpClient.getParams().setParameter("http.protocol.handle-authentication", Boolean.TRUE);
    httpClient.getParams().setParameter("http.auth.target-scheme-pref", ImmutableList.of(OpalAuthScheme.NAME));
    httpClient.getAuthSchemes().register(OpalAuthScheme.NAME, new OpalAuthScheme.Factory());
    ctx = new BasicHttpContext();
    ctx.setAttribute(ClientContext.AUTH_SCHEME_PREF, ImmutableList.of(OpalAuthScheme.NAME));
    ctx.setAttribute(ClientContext.COOKIE_STORE, cs = new BasicCookieStore());
    this.client = httpClient;
  }

  public URI buildURI(String... segments) {
    return buildURI(this.opalURI, segments);
  }

  public URI buildURI(final URI root, String... segments) {
    URI uri = root;
    for(String segment : segments) {
      segment = segment.endsWith("/") ? segment : segment + "/";
      uri = uri.resolve(segment);
    }
    return uri;
  }

  @SuppressWarnings("unchecked")
  public <T extends Message> List<T> getResources(Class<T> messageType, URI uri, Message.Builder builder) {
    ArrayList<T> resources = new ArrayList<T>();
    InputStream is = null;
    try {
      HttpResponse response = get(uri);
      is = response.getEntity().getContent();
      while(builder.mergeDelimitedFrom(is)) {
        T message = (T) builder.build();
        resources.add(message);
        builder = message.newBuilderForType();
      }
      return resources;
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    } finally {
      StreamUtil.silentSafeClose(is);
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
      throw new MagmaRuntimeException(e);
    } finally {
      StreamUtil.silentSafeClose(is);
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

}
