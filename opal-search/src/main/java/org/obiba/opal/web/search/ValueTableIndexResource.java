/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.search.service.IndexSynchronization;
import org.obiba.opal.search.service.ValueTableValuesIndex;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.OpalMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@Scope("request")
@Path("/datasource/{ds}/table/{table}/index")
@Tag(name = "Datasources", description = "Operations on datasources")
@Tag(name = "Search", description = "Operations on the search service")
public class ValueTableIndexResource extends IndexResource {

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @OPTIONS
  @Operation(summary = "Get table index options", description = "Retrieve supported HTTP methods for table index operations.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Options successfully retrieved")
  })
  public Response getOptions() {
    return Response.ok().build();
  }

  @GET
  @Operation(summary = "Get table index status", description = "Retrieve detailed indexation status for a specific datasource table, including progress, timestamps, and configuration.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Table index status successfully retrieved"),
      @ApiResponse(responseCode = "503", description = "Search service unavailable")
  })
  public Response getTableStatus() {
    if(!isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    ValueTable valueTable = getValueTable(datasource, table);

    Opal.TableIndexStatusDto.Builder dtoBuilder = Opal.TableIndexStatusDto.newBuilder() //
        .setDatasource(datasource) //
        .setTable(table) //
        .setSchedule(getScheduleDto(datasource, table)) //
        .setStatus(getTableIndexationStatus(datasource, table)) //
        .setLink(UriBuilder.fromPath("/").path(ValueTableIndexResource.class).build(datasource, table).getPath());

    Float progress = getValueTableIndexationProgress(datasource, table);
    if(progress != null) {
      dtoBuilder.setProgress(progress);
    }

    if(!valueTable.getTimestamps().getCreated().isNull()) {
      dtoBuilder.setTableLastUpdate(valueTable.getTimestamps().getLastUpdate().toString());
    }

    Timestamps indexTimestamps = opalSearchService.getValuesIndexManager().getIndex(valueTable).getTimestamps();
    if(!indexTimestamps.getCreated().isNull()) {
      dtoBuilder.setIndexCreated(indexTimestamps.getCreated().toString());
    }
    if(!indexTimestamps.getLastUpdate().isNull()) {
      dtoBuilder.setIndexLastUpdate(indexTimestamps.getLastUpdate().toString());
    }

    return Response.ok().entity(dtoBuilder.build()).build();
  }

  @PUT
  @Operation(summary = "Update table index", description = "Trigger re-indexing of variables and values for a specific table. Synchronizes both variable and value indices.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Index update successfully triggered"),
      @ApiResponse(responseCode = "503", description = "Search service unavailable")
  })
  public Response updateIndex() {
    if(!isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    ValueTable valueTable = getValueTable(datasource, table);
    if(!isInProgress(datasource, table)) {
      // synchronize variable index and values index
      synchroManager.synchronizeIndex(opalSearchService.getVariablesIndexManager(), valueTable);
      synchroManager.synchronizeIndex(opalSearchService.getValuesIndexManager(), valueTable);
    }
    return Response.ok().build();
  }

  @DELETE
  @Operation(summary = "Delete table index", description = "Delete the search index for a specific table. Cancels any ongoing indexation and removes both variable and value indices.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Index successfully deleted"),
      @ApiResponse(responseCode = "503", description = "Search service unavailable")
  })
  public Response deleteIndex() {
    if(!isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    // cancel indexation if in progress
    IndexSynchronization currentTask = synchroManager.getCurrentTask();
    if(currentTask != null &&
        currentTask.getValueTable().getName().equals(table) &&
        currentTask.getValueTable().getDatasource().getName().equals(datasource)) {
      // Stop task
      synchroManager.stopTask();
    }
    getValueTableIndex(datasource, table).delete();
    return Response.ok().build();
  }

  @GET
  @Path("schedule")
  @Operation(summary = "Get table index schedule", description = "Retrieve the automatic indexing schedule configuration for a specific table.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Schedule configuration successfully retrieved")
  })
  public Opal.ScheduleDto getSchedule() {
    return getScheduleDto(datasource, table);
  }

  @DELETE
  @Path("schedule")
  @Operation(summary = "Delete table index schedule", description = "Remove the automatic indexing schedule for a specific table.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Schedule successfully deleted")
  })
  public Response deleteSchedule() {
    configService.getConfig().removeSchedule(getValueTable(datasource, table));
    return Response.ok().build();
  }

  @PUT
  @Path("schedule")
  @Operation(summary = "Set table index schedule", description = "Configure automatic indexing schedule for a specific table with specified timing and recurrence.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Schedule successfully configured")
  })
  public Response setSchedule(Opal.ScheduleDto scheduleDto) {

    Schedule schedule = new Schedule();
    schedule.setType(scheduleDto.getType());
    if(scheduleDto.hasDay()) {
      schedule.setDay(scheduleDto.getDay());
    }
    if(scheduleDto.hasHours()) {
      schedule.setHours(scheduleDto.getHours());
    }
    if(scheduleDto.hasMinutes()) {
      schedule.setMinutes(scheduleDto.getMinutes());
    }

    configService.update(getValueTable(datasource, table), schedule);

    return Response.ok().build();
  }

  @GET
  @Path("_schema")
  @Operation(summary = "Get table index schema", description = "Retrieve the search index schema mapping for a specific table, showing variable names and their corresponding field names in the index.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Index schema successfully retrieved"),
      @ApiResponse(responseCode = "503", description = "Search service unavailable")
  })
  public Response search() {
    if(!isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    ValueTableValuesIndex index = getValueTableIndex(datasource, table);
    OpalMap.Builder map = OpalMap.newBuilder();
    for(Variable variable : index.getVariables()) {
      map.addKeys(variable.getName());
      map.addValues(index.getFieldName(variable.getName()));
    }
    return Response.ok(map.build()).build();
  }

  private boolean isEnabled() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled() && opalSearchService.getValuesIndexManager() != null && opalSearchService.getValuesIndexManager().isReady();
  }
}
