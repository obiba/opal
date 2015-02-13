package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;

import com.wordnik.swagger.annotations.ApiOperation;

public interface DatasourceTablesResource {

  @GET
  @ApiOperation(value = "Returns all tables of all datasources", response = List.class)
  List<Magma.TableDto> getTables(@Context Request request, @QueryParam("counts") @DefaultValue("false") boolean counts,
      @Nullable @QueryParam("entityType") String entityType);

  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthorizeResource
  @AuthenticatedByCookie
  Response getExcelDictionary(@QueryParam("table") List<String> tables) throws MagmaRuntimeException, IOException;

  @POST
  Response createTable(Magma.TableDto table);

  @DELETE
  Response deleteTables(@QueryParam("table") List<String> tables);

  void setDatasource(Datasource datasource);

  List<Magma.TableDto> getTables(boolean counts, String entityType);
}
