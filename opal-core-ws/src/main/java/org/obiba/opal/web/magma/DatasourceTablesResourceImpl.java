/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.security.OpalPermissions;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DatasourceTablesResourceImpl implements AbstractTablesResource, DatasourceTablesResource {

  private Datasource datasource;

  private ViewManager viewManager;

  private Set<ValueTableUpdateListener> tableListeners;

  @Override
  public void setDatasource(Datasource datasource) {
    this.datasource = datasource;
  }

  @Autowired
  public void setTableListeners(Set<ValueTableUpdateListener> tableListeners) {
    this.tableListeners = tableListeners;
  }

  @Autowired
  public void setViewManager(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  /**
   * Get the tables of the datasource.
   *
   * @param counts Set the count of entities and of variables (default is true).
   * @param entityType Filter the tables with provided entity type (default is no filter).
   * @return
   */
  @Override
  @GET
  public Response getTables(@Context Request request, @QueryParam("counts") @DefaultValue("false") Boolean counts,
      @Nullable @QueryParam("entityType") String entityType) {
    TimestampedResponses.evaluate(request, datasource);

    // The use of "GenericEntity" is required because otherwise JAX-RS can't determine the type using reflection.
    //noinspection EmptyClass
    return TimestampedResponses.ok(datasource, new GenericEntity<List<TableDto>>(getTables(counts, entityType)) {
      // Nothing to implement. Subclassed to keep generic information at runtime.
    }).build();
  }

  @Override
  public List<TableDto> getTables(boolean counts, String entityType) {
    List<Magma.TableDto> tables = Lists.newArrayList();
    UriBuilder tableLink = UriBuilder.fromPath("/").path(DatasourceResource.class)
        .path(DatasourceResource.class, "getTable");
    UriBuilder viewLink = UriBuilder.fromPath("/").path(DatasourceResource.class)
        .path(DatasourceResource.class, "getView");
    for(ValueTable valueTable : datasource.getValueTables()) {
      if(entityType == null || valueTable.getEntityType().equals(entityType)) {
        TableDto.Builder builder = Dtos.asDto(valueTable, counts)
            .setLink(tableLink.build(datasource.getName(), valueTable.getName()).toString());
        if(valueTable.isView()) {
          builder.setViewLink(viewLink.build(datasource.getName(), valueTable.getName()).toString());
        }
        tables.add(builder.build());
      }
    }
    sortByName(tables);

    return tables;
  }

  @Override
  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthorizeResource
  @AuthenticatedByCookie
  public Response getExcelDictionary(List<String> tables) throws MagmaRuntimeException, IOException {
    String destinationName = datasource.getName() + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    Datasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);
    destinationDatasource.initialise();
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();

      if(tables == null || tables.isEmpty()) {
        copier.copy(datasource, destinationDatasource);
      } else {
        for(ValueTable table : datasource.getValueTables()) {
          if(tables.contains(table.getName())) {
            copier.copy(table, destinationDatasource);
          }
        }
      }

    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }
    return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel")
        .header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @Override
  @POST
  public Response createTable(final TableDto table) {
    if(MagmaEngine.get().hasExtension(MagmaSecurityExtension.class)) {
      return MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer()
          .silentSudo(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
              return createTableInternal(table);
            }
          });
    }

    return createTableInternal(table);
  }

  private Response createTableInternal(TableDto table) {
    if(datasource.hasValueTable(table.getName())) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "TableAlreadyExists").build()).build();
    }
    writeVariablesToTable(table);
    URI tableUri = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable")
        .build(datasource.getName(), table.getName());
    return Response.created(tableUri)//
        .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(tableUri, AclAction.TABLE_ALL)).build();
  }

  @Override
  @DELETE
  public Response deleteTables(@QueryParam("table") List<String> tables) {

    for(String table : tables) {
      if(datasource.hasValueTable(table) && datasource.canDropTable(table)) {
        if(tableListeners != null && !tableListeners.isEmpty()) {
          for(ValueTableUpdateListener listener : tableListeners) {
            listener.onDelete(datasource.getValueTable(table));
          }
        }

        if(datasource.getValueTable(table).isView()) {
          viewManager.removeView(datasource.getName(), table);
        } else {
          datasource.dropTable(table);
        }
      }
    }

    return Response.ok().build();
  }

  private void writeVariablesToTable(TableDto table) {
    try(VariableWriter variableWriter = datasource.createWriter(table.getName(), table.getEntityType())
        .writeVariables()) {
      for(VariableDto dto : table.getVariablesList()) {
        variableWriter.writeVariable(Dtos.fromDto(dto));
      }
    }
  }

  private void sortByName(List<Magma.TableDto> tables) {
    // sort alphabetically
    Collections.sort(tables, new Comparator<Magma.TableDto>() {

      @Override
      public int compare(TableDto t1, TableDto t2) {
        return t1.getName().compareTo(t2.getName());
      }

    });
  }
}
