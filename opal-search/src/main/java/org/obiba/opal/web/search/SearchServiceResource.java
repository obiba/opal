/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Opal;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/service/search/indices")
public class SearchServiceResource extends IndexResource {

  @GET
  @Transactional(readOnly = true)
  public List<Opal.TableIndexStatusDto> getIndices() {
    List<Opal.TableIndexStatusDto> tableStatusDtos = Lists.newArrayList();

    // ES is available
    if(!valuesIndexManager.isReady() || esProvider.getClient() == null) return tableStatusDtos;

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable valueTable : datasource.getValueTables()) {
        tableStatusDtos.add(getTableStatusDto(datasource, valueTable));
      }
    }
    sortByName(tableStatusDtos);

    return tableStatusDtos;
  }

  @PUT
  @Path("/cfg/enabled")
  public Response enableIndexing() {
    valuesIndexManager.setEnabled(true);
    return Response.ok().build();
  }

  @GET
  @Path("/cfg/enabled")
  public Response isEnableIndexing() {
    return valuesIndexManager.isEnabled()
        ? Response.ok().build()
        : Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
  }

  @DELETE
  @Path("/cfg/enabled")
  public Response disableIndexing() {
    valuesIndexManager.setEnabled(false);
    return Response.ok().build();
  }

  private void sortByName(List<Opal.TableIndexStatusDto> tableStatusDtos) {
    // sort alphabetically
    Collections.sort(tableStatusDtos, new Comparator<Opal.TableIndexStatusDto>() {

      @Override
      public int compare(Opal.TableIndexStatusDto s1, Opal.TableIndexStatusDto s2) {
        String d1 = s1.getDatasource() + "." + s1.getTable();
        String d2 = s2.getDatasource() + "." + s2.getTable();
        return d1.compareTo(d2);
      }

    });
  }

  private Opal.TableIndexStatusDto getTableStatusDto(Datasource datasource, ValueTable valueTable) {
    float progress = 0f;
    if(synchroManager.getCurrentTask() != null &&
        synchroManager.getCurrentTask().getValueTable().getName().equals(valueTable.getName())) {
      progress = synchroManager.getCurrentTask().getProgress();
    }

    URI link = UriBuilder.fromPath("/").path(ValueTableIndexResource.class)
        .build(datasource.getName(), valueTable.getName());
    Timestamps timestamps = valueTable.getTimestamps();
    Opal.TableIndexStatusDto tableStatusDto = Opal.TableIndexStatusDto.newBuilder() //
        .setDatasource(datasource.getName()) //
        .setTable(valueTable.getName()) //
        .setSchedule(getScheduleDto(datasource.getName(), valueTable.getName())) //
        .setStatus(getTableIndexationStatus(datasource.getName(), valueTable.getName())) //
        .setProgress(progress) //
        .setLink(link.getPath()).build();

    if(!timestamps.getLastUpdate().isNull()) {
      tableStatusDto = tableStatusDto.toBuilder().setTableLastUpdate(timestamps.getLastUpdate().toString()).build();
    }

    if(!valuesIndexManager.getIndex(valueTable).getTimestamps().getCreated().isNull()) {
      tableStatusDto = tableStatusDto.toBuilder()
          .setIndexCreated(valuesIndexManager.getIndex(valueTable).getTimestamps().getCreated().toString()).build();
    }
    if(!valuesIndexManager.getIndex(valueTable).getTimestamps().getLastUpdate().isNull()) {
      tableStatusDto = tableStatusDto.toBuilder()
          .setIndexLastUpdate(valuesIndexManager.getIndex(valueTable).getTimestamps().getLastUpdate().toString())
          .build();
    }

    return tableStatusDto;
  }

}
