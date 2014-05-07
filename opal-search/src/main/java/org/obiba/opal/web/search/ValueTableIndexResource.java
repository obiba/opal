/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.support.RestUtils;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableValuesIndex;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.OpalMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    if(!valuesIndexManager.isReady()) {
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

    Timestamps indexTimestamps = valuesIndexManager.getIndex(valueTable).getTimestamps();
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
    if(!esProvider.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    ValueTable valueTable = getValueTable(datasource, table);
    if(!isInProgress(datasource, table)) {
      // synchronize variable index and values index
      synchroManager.synchronizeIndex(variablesIndexManager, valueTable, 0);
      synchroManager.synchronizeIndex(valuesIndexManager, valueTable, 0);
    }
    return Response.ok().build();
  }

  @DELETE
  public Response deleteIndex() {
    if(!esProvider.isEnabled()) {
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

  @GET
  @POST
  @Path("_search")
  public Response search(@Context HttpServletRequest servletRequest, String body) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Response> ref = new AtomicReference<>();

    esProvider.getRest()
        .dispatchRequest(new JaxRsRestRequest(getValueTableIndex(datasource, table), servletRequest, body, "_search"),
            new RestChannel() {

              @Override
              public void sendResponse(RestResponse response) {
                try {
                  ref.set(convert(response));
                } catch(IOException e) {
                  // Not gonna happen
                } finally {
                  latch.countDown();
                }
              }

            });

    try {
      latch.await();
      Response r = ref.get();
      return r != null ? r : Response.serverError().build();
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private Response convert(RestResponse response) throws IOException {
    byte[] entity;
    if(response.contentThreadSafe()) {
      entity = response.content();
    } else {
      entity = new byte[response.contentLength()];
      System.arraycopy(response.content(), 0, entity, 0, response.contentLength());
    }
    return Response.status(response.status().getStatus()).entity(entity).type(response.contentType()).build();
  }

  private static class JaxRsRestRequest extends HttpRequest {

    private final String body;

    private final HttpServletRequest servletRequest;

    private final Map<String, String> params;

    private final String rawPath;

    private final String esUri;

    private final Map<String, String> headers;

    JaxRsRestRequest(ValueTableIndex tableIndex, HttpServletRequest servletRequest, String body, String path) {
      this.body = body;
      this.servletRequest = servletRequest;
      params = Maps.newHashMap();
      headers = Maps.newHashMap();
      rawPath = tableIndex.getRequestPath() + "/" + path;

      // Reconstruct the uri
      String queryString = servletRequest.getQueryString();
      esUri = rawPath + (queryString == null ? "" : '?' + queryString);

      RestUtils.decodeQueryString(queryString == null ? "" : queryString, 0, params);

      // headers
      String cType = servletRequest.getHeader("Content-Type");
      if(cType == null) {
        cType = "application/json";
      }
      headers.put("Content-Type", cType);
    }

    @Override
    public Method method() {
      return Method.valueOf(servletRequest.getMethod().toUpperCase());
    }

    @Override
    public String uri() {
      return esUri;
    }

    @Override
    public String rawPath() {
      int pathEndPos = esUri.indexOf('?');
      return pathEndPos < 0 ? esUri : esUri.substring(0, pathEndPos);
    }

    @Override
    public boolean hasContent() {
      return body != null && body.length() > 0;
    }

    @Override
    public boolean contentUnsafe() {
      return false;
    }

    @Override
    public String header(String name) {
      return headers.get(name);
    }

    @Override
    public Iterable<Map.Entry<String, String>> headers() {
      return headers.entrySet();
    }

    @Override
    public boolean hasParam(String key) {
      return params.containsKey(key);
    }

    @Override
    public String param(String key) {
      return params.get(key);
    }

    @Override
    public Map<String, String> params() {
      return params;
    }

    @Override
    public String param(String key, String defaultValue) {
      return hasParam(key) ? param(key) : defaultValue;
    }

    @Override
    public BytesReference content() {
      return new BytesArray(body);
    }

  }
}
