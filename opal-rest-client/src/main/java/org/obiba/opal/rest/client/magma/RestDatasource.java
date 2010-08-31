package org.obiba.opal.rest.client.magma;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.web.model.Magma.TableDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.protobuf.Message;

public class RestDatasource extends AbstractDatasource {

  private final URI datasourceURI;

  private final HttpClient client;

  private final BasicHttpContext ctx;

  private final BasicCookieStore cs;

  private Set<String> cachedTableNames;

  public RestDatasource(String name, String uri, String username, char[] password) throws URISyntaxException {
    super(name, "rest");

    DefaultHttpClient httpClient = new DefaultHttpClient();
    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, new String(password)));
    httpClient.getParams().setParameter("http.protocol.handle-authentication", Boolean.TRUE);
    httpClient.getParams().setParameter("http.auth.target-scheme-pref", ImmutableList.of(OpalAuthScheme.NAME));
    httpClient.getAuthSchemes().register(OpalAuthScheme.NAME, new OpalAuthScheme.Factory());
    ctx = new BasicHttpContext();
    ctx.setAttribute(ClientContext.AUTH_SCHEME_PREF, ImmutableList.of(OpalAuthScheme.NAME));
    ctx.setAttribute(ClientContext.COOKIE_STORE, cs = new BasicCookieStore());

    this.client = httpClient;
    this.datasourceURI = new URI(uri.endsWith("/") ? uri : uri + "/");
  }

  @Override
  public Set<ValueTable> getValueTables() {
    refresh();
    return super.getValueTables();
  }

  @Override
  protected void onInitialise() {
    super.onInitialise();
    cachedTableNames = getValueTableNames();
  }

  @Override
  protected void onDispose() {
    super.onDispose();
    try {
      client.getConnectionManager().shutdown();
    } catch(Exception ignore) {
      // ignore
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    Iterable<TableDto> s = readResources(newReference("tables"), TableDto.newBuilder());
    return ImmutableSet.copyOf(Iterables.transform(s, new Function<TableDto, String>() {

      @Override
      public String apply(TableDto from) {
        return from.getName();
      }

    }));
  }

  @Override
  protected ValueTable initialiseValueTable(final String tableName) {
    return new RestValueTable(this, (TableDto) readResource(newReference("table", tableName), TableDto.newBuilder()));
  }

  private void refresh() {
    Set<String> currentTables = getValueTableNames();

    SetView<String> tablesToRemove = Sets.difference(cachedTableNames, currentTables);
    SetView<String> tablesToAdd = Sets.difference(currentTables, cachedTableNames);

    for(String table : tablesToRemove) {
      ValueTable v = super.getValueTable(table);
      // remove table.
    }
    for(String table : tablesToAdd) {
      ValueTable vt = initialiseValueTable(table);
      Initialisables.initialise(vt);
      super.addValueTable(vt);
    }
  }

  URI newReference(String... segments) {
    return buildURI(this.datasourceURI, segments);
  }

  URI buildURI(final URI root, String... segments) {
    URI uri = root;
    for(String segment : segments) {
      segment = segment.endsWith("/") ? segment : segment + "/";
      uri = uri.resolve(segment);
    }
    return uri;
  }

  @SuppressWarnings("unchecked")
  <T extends Message> List<T> readResources(URI uri, Message.Builder builder) {
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
  <T extends Message> T readResource(URI uri, Message.Builder builder) {
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

  HttpResponse get(URI uri) throws ClientProtocolException, IOException {
    HttpGet get = new HttpGet(uri);
    get.addHeader("Accept", "application/x-protobuf");
    authenticate(get);
    return client.execute(get, ctx);
  }

  void authenticate(HttpMessage msg) {
    for(Cookie c : cs.getCookies()) {
      if(c.getName().equalsIgnoreCase("opalsid")) {
        msg.addHeader(OpalAuthScheme.NAME, c.getValue());
      }
    }
  }
}
