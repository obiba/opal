/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Transactional
@Path("/datasources")
@Tag(name = "Datasources", description = "Operations on datasources")
public class DatasourcesResource {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private SQLService sqlService;

  @GET
  @Operation(
    summary = "Get all datasources",
    description = "Retrieves a list of all available datasources with their metadata and access links."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved datasources list"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<Magma.DatasourceDto> getDatasources() {
    List<Magma.DatasourceDto> datasources = Lists.newArrayList();
    for (Datasource from : MagmaEngine.get().getDatasources()) {
      URI dsLink = UriBuilder.fromPath("/").path(DatasourceResource.class).build(from.getName());
      Magma.DatasourceDto.Builder ds = Dtos.asDto(from).setLink(dsLink.toString());
      datasources.add(ds.build());
    }
    sortByName(datasources);
    return datasources;
  }

  @GET
  @Path("/tables")
  @Operation(
    summary = "Get all tables from all datasources",
    description = "Retrieves a consolidated list of all tables from all datasources, optionally filtered by entity type and indexed status."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tables list"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<Magma.TableDto> getTables(@Nullable @QueryParam("entityType") String entityType, @QueryParam("indexed") @DefaultValue("false") boolean indexed) {
    List<Magma.TableDto> tables = Lists.newArrayList();
    for (Datasource datasource : MagmaEngine.get().getDatasources()) {
      tables.addAll(getDatasourceTablesResource(datasource).getTables(false, entityType, indexed));
    }

    Collections.sort(tables, Comparator.comparing(Magma.TableDto::getLink));
    return tables;
  }

  @GET
  @Path("/entity-types")
  @Operation(
    summary = "Get all entity types",
    description = "Retrieves a summary of all entity types available across all datasources with their table counts."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved entity types summary"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<Magma.VariableEntitySummaryDto> getEntityTypes() {
    Map<String, Integer> entityTypes = Maps.newHashMap();
    MagmaEngine.get().getDatasources()
        .forEach(ds -> ds.getValueTables().stream().map(ValueTable::getEntityType)
            .forEach(et -> {
              if (entityTypes.containsKey(et)) entityTypes.put(et, entityTypes.get(et) + 1);
              else entityTypes.put(et, 1);
            }));
    return entityTypes.entrySet().stream()
        .map(et -> Magma.VariableEntitySummaryDto.newBuilder().setEntityType(et.getKey()).setTableCount(et.getValue()).build())
        .collect(Collectors.toList());
  }

  @GET
  @Path("/count")
  @Operation(
    summary = "Get datasources count",
    description = "Retrieves the total number of available datasources."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved datasources count"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getDatasourcesCount() {
    return Response.ok().entity(String.valueOf(MagmaEngine.get().getDatasources().size())).build();

  }

  @POST
  @Path("/_sql")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Execute cross-datasource SQL and return JSON",
    description = "Executes a SQL query across all datasources and returns results in JSON format."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Query executed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid SQL query"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response executeSQLToJSON(String query, @QueryParam("id") @DefaultValue(SQLService.DEFAULT_ID_COLUMN) String idName) {
    final File output = sqlService.execute(null, query, idName, SQLService.Output.JSON);
    StreamingOutput stream = os -> {
      Files.copy(output.toPath(), os);
      output.delete();
    };

    return Response.ok(stream, MediaType.APPLICATION_JSON_TYPE)
        .header("Content-Disposition", "attachment; filename=\"" + output.getName() + "\"").build();
  }

  @POST
  @Path("/_sql")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("text/csv")
  @Operation(
    summary = "Execute cross-datasource SQL and return CSV",
    description = "Executes a SQL query across all datasources and returns results in CSV format."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Query executed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid SQL query"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response executeSQLToCSV(@FormParam("query") String query, @FormParam("id") @DefaultValue(SQLService.DEFAULT_ID_COLUMN) String idName) {
    final File output = sqlService.execute(null, query, idName, SQLService.Output.CSV);
    StreamingOutput stream = os -> {
      Files.copy(output.toPath(), os);
      output.delete();
    };

    return Response.ok(stream, "text/csv")
        .header("Content-Disposition", "attachment; filename=\"" + output.getName() + "\"").build();
  }

  @POST
  @Path("/_rsql")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("application/x-rdata")
  @Operation(
    summary = "Execute cross-datasource SQL and return RDS",
    description = "Executes a SQL query across all datasources and returns results in R data format for use in R."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Query executed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid SQL query"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response executeSQLToRDS(@FormParam("query") String query, @FormParam("id") @DefaultValue(SQLService.DEFAULT_ID_COLUMN) String idName) {
    final File output = sqlService.execute(null, query, idName, SQLService.Output.RDS);
    StreamingOutput stream = os -> {
      Files.copy(output.toPath(), os);
      output.delete();
    };

    return Response.ok(stream, "application/x-rdata")
        .header("Content-Disposition", "attachment; filename=\"" + output.getName() + "\"").build();
  }

  private DatasourceTablesResource getDatasourceTablesResource(Datasource datasource) {
    DatasourceTablesResource datasourceTablesResource = applicationContext.getBean(DatasourceTablesResource.class);
    datasourceTablesResource.setDatasource(datasource);
    return datasourceTablesResource;
  }

  private void sortByName(List<Magma.DatasourceDto> datasources) {
    // sort alphabetically
    Collections.sort(datasources, Comparator.comparing(DatasourceDto::getName));
  }

}
