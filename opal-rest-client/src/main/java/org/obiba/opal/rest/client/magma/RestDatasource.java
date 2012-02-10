package org.obiba.opal.rest.client.magma;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.web.model.Magma.TableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class RestDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(RestDatasource.class);

  private final OpalJavaClient opalClient;

  private final URI datasourceURI;

  public RestDatasource(String name, String opalUri, String remoteDatasource, String username, String password) throws URISyntaxException {
    this(name, new OpalJavaClient(opalUri, username, password), remoteDatasource);
  }

  public RestDatasource(String name, OpalJavaClient opalClient, String remoteDatasource) {
    super(name, "rest");
    this.opalClient = opalClient;
    this.datasourceURI = opalClient.newUri().segment("datasource", remoteDatasource).build();
  }

  @Override
  public Set<ValueTable> getValueTables() {
    try {
      refresh();
    } catch(RuntimeException e) {
      if(e.getCause() != null && e.getCause() instanceof ConnectException) {
        log.warn("Failed connecting to Opal: {}", e.getCause().getMessage());
      } else {
        log.warn("Unexpected error while communicating with Opal", e);
      }
    }
    return super.getValueTables();
  }

  @Override
  public void initialise() {
    try {
      super.initialise();
    } catch(RuntimeException e) {
      if(e.getCause() != null && e.getCause() instanceof ConnectException) {
        log.warn("Failed connecting to Opal: {}", e.getCause().getMessage());
      } else {
        log.warn("Unexpected error while communicating with Opal", e);
      }
    }
  }

  @Override
  protected void onDispose() {
    super.onDispose();
    try {
      opalClient.close();
    } catch(Exception ignore) {
      // ignore
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    Iterable<TableDto> s = opalClient.getResources(TableDto.class, newReference("tables"), TableDto.newBuilder());
    return ImmutableSet.copyOf(Iterables.transform(s, new Function<TableDto, String>() {

      @Override
      public String apply(TableDto from) {
        return from.getName();
      }

    }));
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    if(super.hasValueTable(tableName) == false) {
      URI tableUri = newReference("tables");
      try {
        HttpResponse response = getOpalClient().post(tableUri, TableDto.newBuilder().setName(tableName).setEntityType(entityType).build());
        if(response.getStatusLine().getStatusCode() != 201) {
          throw new RuntimeException("cannot create table " + response.getStatusLine().getReasonPhrase());
        }
        EntityUtils.consume(response.getEntity());
      } catch(ClientProtocolException e) {
        throw new RuntimeException(e);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
      refresh();
    }
    return new RestValueTableWriter((RestValueTable) super.getValueTable(tableName));
  }

  @Override
  protected ValueTable initialiseValueTable(final String tableName) {
    return new RestValueTable(this, opalClient.getResource(TableDto.class, newReference("table", tableName), TableDto.newBuilder()));
  }

  private void refresh() {
    Set<String> cachedTableNames = ImmutableSet.copyOf(Iterables.transform(super.getValueTables(), new Function<ValueTable, String>() {

      @Override
      public String apply(ValueTable input) {
        return input.getName();
      }
    }));
    Set<String> currentTables = getValueTableNames();

    SetView<String> tablesToRemove = Sets.difference(cachedTableNames, currentTables);
    SetView<String> tablesToAdd = Sets.difference(currentTables, cachedTableNames);

    for(String table : tablesToRemove) {
      super.removeValueTable(table);
    }
    for(String table : tablesToAdd) {
      ValueTable vt = initialiseValueTable(table);
      Initialisables.initialise(vt);
      super.addValueTable(vt);
    }
  }

  OpalJavaClient getOpalClient() {
    return opalClient;
  }

  URI newReference(String... segments) {
    return buildURI(this.datasourceURI, segments);
  }

  URI buildURI(final URI root, String... segments) {
    return opalClient.newUri(root).segment(segments).build();
  }

}
