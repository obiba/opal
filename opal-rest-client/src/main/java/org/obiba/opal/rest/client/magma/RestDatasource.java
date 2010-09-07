package org.obiba.opal.rest.client.magma;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.web.model.Magma.TableDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.protobuf.Message;

public class RestDatasource extends AbstractDatasource {

  private final OpalJavaClient opalClient;

  private final URI datasourceURI;

  private Set<String> cachedTableNames;

  public RestDatasource(String name, String opalUri, String remoteDatasource, String username, String password) throws URISyntaxException {
    this(name, new OpalJavaClient(opalUri, username, password), remoteDatasource);
  }

  public RestDatasource(String name, OpalJavaClient opalClient, String remoteDatasource) {
    super(name, "rest");
    this.opalClient = opalClient;
    this.datasourceURI = opalClient.buildURI("datasource",remoteDatasource);
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
      //      client.getConnectionManager().shutdown();
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
  protected ValueTable initialiseValueTable(final String tableName) {
    return new RestValueTable(this, opalClient.getResource(TableDto.class, newReference("table", tableName), TableDto.newBuilder()));
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
  
  OpalJavaClient getOpalClient() {
    return opalClient;
  }

  URI newReference(String... segments) {
    return opalClient.buildURI(this.datasourceURI, segments);
  }

  URI buildURI(final URI root, String... segments) {
    return opalClient.buildURI(root, segments);
  }

}
