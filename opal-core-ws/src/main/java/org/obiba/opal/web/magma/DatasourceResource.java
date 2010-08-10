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
import java.util.List;

import javax.annotation.PreDestroy;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{name}")
public class DatasourceResource {

  private static final Logger log = LoggerFactory.getLogger(DatasourceResource.class);

  @PathParam("name")
  private String name;

  private Datasource transientDatasourceInstance;

  // Used by Spring. JAX-RS, will inject the name attribute
  public DatasourceResource() {
  }

  // Used for testing
  public DatasourceResource(String name) {
    this.name = name;
  }

  @PreDestroy
  public void destroy() {
    if(transientDatasourceInstance != null) {
      Disposables.silentlyDispose(transientDatasourceInstance);
      transientDatasourceInstance = null;
    }
  }

  @GET
  public Magma.DatasourceDto get() {
    Datasource ds = getDatasource();

    return Dtos.asDto(ds).build();
  }

  @DELETE
  public Response removeDatasource() {
    ResponseBuilder response = null;
    if(MagmaEngine.get().hasTransientDatasource(name)) {
      MagmaEngine.get().removeTransientDatasource(name);
      response = Response.ok();
    } else if(MagmaEngine.get().hasDatasource(name)) {
      response = Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST, "NotTransientDatasourceRemovalUnsupported"));
    } else {
      // returns silently
      response = Response.ok();
    }

    return response.build();
  }

  @GET
  @Path("/variables/excel")
  @Produces("application/vnd.ms-excel")
  @NotAuthenticated
  public Response getExcelDictionary() throws MagmaRuntimeException, IOException {
    String destinationName = name + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    ExcelDatasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    destinationDatasource.initialise();
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
      copier.copy(getDatasource(), destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }
    return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @Path("/table/{table}")
  public TableResource getTable(@PathParam("table") String table) {
    return getTableResource(getDatasource().getValueTable(table));
  }

  @PUT
  @Path("/table/{table}")
  public Response createTable(@Context UriInfo uri, @PathParam("table") String table, List<Variable> variables) throws IOException {
    Datasource ds = getDatasource();
    if(ds.hasValueTable(table)) {
      throw new IllegalStateException("");
    }

    ValueTableWriter writer = ds.createWriter(table, variables.iterator().next().getEntityType());
    try {
      VariableWriter vw = writer.writeVariables();
      for(Variable v : variables) {
        vw.writeVariable(v);
      }
      vw.close();
    } finally {
      writer.close();
    }
    return Response.created(uri.getAbsolutePath()).build();
  }

  @Path("/tables")
  public TablesResource getTables() {
    return new TablesResource(getDatasource());
  }

  @Bean
  @Scope("request")
  public TableResource getTableResource(ValueTable table) {
    return new TableResource(table);
  }

  @PUT
  public Response createTable(TableDto table) {

    try {

      Datasource datasource = MagmaEngine.get().getDatasource(name);

      ClientErrorDto errorDto;

      // @TODO Verify that the datasource allows table creation (Magma does not offer this yet)
      // if(datasource.isReadOnly()) {
      // errorMessage = return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST,
      // "CannotCreateTable")).build();
      // } else

      if(datasource.hasValueTable(table.getName())) {
        return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST, "TableAlreadyExists")).build();
      } else {
        writeVariablesToTable(table, datasource);
        return Response.created(UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable").build(name, table.getName())).build();
      }
    } catch(Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(getErrorMessage(Status.INTERNAL_SERVER_ERROR, e.getMessage())).build();
    }
  }

  private Datasource getDatasource() {
    Datasource ds = null;
    if(MagmaEngine.get().hasDatasource(name)) {
      ds = MagmaEngine.get().getDatasource(name);
    } else {
      ds = MagmaEngine.get().getTransientDatasourceInstance(name);
      transientDatasourceInstance = ds;
    }
    return ds;
  }

  private ClientErrorDto getErrorMessage(Status responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus).build();
  }

  private void writeVariablesToTable(TableDto table, Datasource datasource) {
    VariableWriter vw = null;
    try {
      vw = datasource.createWriter(table.getName(), table.getEntityType()).writeVariables();

      for(VariableDto dto : table.getVariablesList()) {
        vw.writeVariable(Dtos.fromDto(dto));
      }
    } finally {
      StreamUtil.silentSafeClose(vw);
    }
  }
}
