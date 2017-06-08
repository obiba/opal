/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.spi.search.IndexSynchronization;
import org.obiba.opal.spi.search.ValueTableValuesIndex;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.OpalMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Component
@Transactional(readOnly = true)
@Scope("request")
@Path("/datasource/{ds}/table/{table}/index")
public class ValueTableIndexResource extends IndexResource {

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @GET
  @OPTIONS
  public Response getTableStatus() {
    if(!opalSearchService.getValuesIndexManager().isReady()) {
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
  public Response updateIndex() {
    if(!opalSearchService.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    ValueTable valueTable = getValueTable(datasource, table);
    if(!isInProgress(datasource, table)) {
      // synchronize variable index and values index
      synchroManager.synchronizeIndex(opalSearchService.getVariablesIndexManager(), valueTable, 0);
      synchroManager.synchronizeIndex(opalSearchService.getValuesIndexManager(), valueTable, 0);
    }
    return Response.ok().build();
  }

  @DELETE
  public Response deleteIndex() {
    if(!opalSearchService.isEnabled()) {
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
  public Opal.ScheduleDto getSchedule() {
    return getScheduleDto(datasource, table);
  }

  @DELETE
  @Path("schedule")
  public Response deleteSchedule() {
    configService.getConfig().removeSchedule(getValueTable(datasource, table));
    return Response.ok().build();
  }

  @PUT
  @Path("schedule")
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
  public Response search() {
    ValueTableValuesIndex index = getValueTableIndex(datasource, table);
    OpalMap.Builder map = OpalMap.newBuilder();
    for(Variable variable : index.getVariables()) {
      map.addKeys(variable.getName());
      map.addValues(index.getFieldName(variable.getName()));
    }
    return Response.ok(map.build()).build();
  }

}
