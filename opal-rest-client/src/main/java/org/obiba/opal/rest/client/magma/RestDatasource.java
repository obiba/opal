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

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.web.model.Magma.DatasourceDto;
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

  public RestDatasource(String name, String opalUri, String remoteDatasource, String username, String password)
      throws URISyntaxException {
    this(name, new OpalJavaClient(opalUri, username, password), remoteDatasource);
  }

  public RestDatasource(String name, OpalJavaClient opalClient, String remoteDatasource) {
    super(name, "rest");
    this.opalClient = opalClient;
    datasourceURI = opalClient.newUri().segment("datasource", remoteDatasource).build();
  }

  @Override
  public Set<ValueTable> getValueTables() {
    try {
      refresh();
    } catch(RuntimeException e) {
      if(e.getCause() instanceof ConnectException) {
        log.error("Failed connecting to Opal server: {}", e.getMessage(), e);
      } else {
        log.error("Unexpected error while communicating with Opal server: {}", e.getMessage(), e);
      }
      throw new MagmaRuntimeException(e.getMessage(), e);
    }
    return super.getValueTables();
  }

  @Override
  public void initialise() {
    try {
      super.initialise();
    } catch(RuntimeException e) {
      if(e.getCause() instanceof ConnectException) {
        log.error("Failed connecting to Opal server: {}", e.getMessage(), e);
      } else {
        log.error("Unexpected error while communicating with Opal server: {}", e.getMessage(), e);
      }
      throw new MagmaRuntimeException(e.getMessage(), e);
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
    DatasourceDto d = opalClient.getResource(DatasourceDto.class, datasourceURI, DatasourceDto.newBuilder());
    return ImmutableSet.copyOf(d.getTableList());
  }

  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    if(!hasValueTable(tableName)) {
      URI tableUri = newReference("tables");
      try {
        HttpResponse response = getOpalClient()
            .post(tableUri, TableDto.newBuilder().setName(tableName).setEntityType(entityType).build());
        if(response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
          throw new RuntimeException("cannot create table " + response.getStatusLine().getReasonPhrase());
        }
        addValueTable(tableName);
        EntityUtils.consume(response.getEntity());
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new RestValueTableWriter((RestValueTable) getValueTable(tableName));
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new RestValueTable(this, opalClient
        .getResource(TableDto.class, newUri("table", tableName).query("counts", "false").build(),
            TableDto.newBuilder()));
  }

  private void refresh() {
    try {
      Set<String> cachedTableNames = ImmutableSet
          .copyOf(Iterables.transform(super.getValueTables(), new Function<ValueTable, String>() {

            @Override
            public String apply(ValueTable input) {
              return input.getName();
            }
          }));
      Set<String> currentTables = getValueTableNames();

      SetView<String> tablesToRemove = Sets.difference(cachedTableNames, currentTables);
      SetView<String> tablesToAdd = Sets.difference(currentTables, cachedTableNames);

      for(String table : tablesToRemove) {
        removeValueTable(table);
      }
      for(String table : tablesToAdd) {
        addValueTable(table);
      }
    } catch (Exception e) {
      log.error("Failed refreshing value tables from Opal server: {}", e.getMessage(), e);
    }
  }

  private void addValueTable(String table) {
    ValueTable vt = initialiseValueTable(table);
    Initialisables.initialise(vt);
    addValueTable(vt);
  }

  OpalJavaClient getOpalClient() {
    return opalClient;
  }

  URI newReference(String... segments) {
    return uriBuilder().segment(segments).build();
  }

  UriBuilder newUri(String... segments) {
    return uriBuilder().segment(segments);
  }

  URI buildURI(URI root, String... segments) {
    return uriBuilder(root).segment(segments).build();
  }

  UriBuilder uriBuilder() {
    return uriBuilder(datasourceURI);
  }

  UriBuilder uriBuilder(URI root) {
    return opalClient.newUri(root);
  }

}
