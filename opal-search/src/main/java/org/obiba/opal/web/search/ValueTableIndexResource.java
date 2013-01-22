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
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.support.AbstractRestRequest;
import org.elasticsearch.rest.support.RestUtils;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronizationManager;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.search.SearchServiceException;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.OpalMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/index")
public class ValueTableIndexResource extends IndexResource {

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @Autowired
  public ValueTableIndexResource(IndexManager indexManager, ElasticSearchProvider esProvider,
      IndexManagerConfigurationService configService, IndexSynchronizationManager synchroManager) {
    super(indexManager, configService, esProvider, synchroManager);
  }

  @GET
  public Opal.TableIndexStatusDto getTableStatus() throws SearchServiceException {
    if(esProvider.isEnabled()) {
      ValueTable valueTable = getValueTable(datasource, table);

      URI link = UriBuilder.fromPath("/").path(ValueTableIndexResource.class).build(datasource, table);
      Opal.TableIndexStatusDto tableStatusDto = Opal.TableIndexStatusDto.newBuilder().setDatasource(datasource)
          .setTable(table).setSchedule(getScheduleDto(datasource, table))
          .setStatus(getTableIndexationStatus(datasource, table)).setProgress(getValueTableIndexationProgress(table))
          .setLink(link.getPath()).setTableLastUpdate(valueTable.getTimestamps().getLastUpdate().toString()).build();

      if(!indexManager.getIndex(valueTable).getTimestamps().getCreated().isNull()) {
        tableStatusDto = tableStatusDto.toBuilder()
            .setIndexCreated(indexManager.getIndex(valueTable).getTimestamps().getCreated().toString()).build();
      }
      if(!indexManager.getIndex(valueTable).getTimestamps().getLastUpdate().isNull()) {
        tableStatusDto = tableStatusDto.toBuilder()
            .setIndexLastUpdate(indexManager.getIndex(valueTable).getTimestamps().getLastUpdate().toString()).build();
      }

      return tableStatusDto;
    }

    throw new SearchServiceException();
  }

  @PUT
  public Response updateIndex() {
    if(esProvider.isEnabled()) {

      ValueTable valueTable = getValueTable(datasource, table);

      if(!isInProgress(table)) {

        synchroManager.synchronizeIndex(valueTable);
      }

      return Response.ok().build();
    }

    throw new SearchServiceException();
  }

  @DELETE
  public Response deleteIndex() {
    if(esProvider.isEnabled()) {

      // cancel indexation if in progress
      if(synchroManager.hasTask() &&
          synchroManager.getCurrentTask().getValueTable().getName().equals(table) &&
          synchroManager.getCurrentTask().getValueTable().getDatasource().getName().equals(datasource)) {
        // Stop task
        synchroManager.stopTask();
      }

      return Response.ok().build();
    }

    throw new SearchServiceException();
  }

  @GET
  @Path("/schedule")
  public Opal.ScheduleDto getSchedule() {

    return getScheduleDto(datasource, table);
  }

  @DELETE
  @Path("/schedule")
  public Response deleteSchedule() {

    configService.getConfig().removeSchedule(getValueTable(datasource, table));

    return Response.ok().build();
  }

  @PUT
  @Path("/schedule")
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
  public Response search(@Context HttpServletRequest servletRequest) {
    ValueTableIndex index = getValueTableIndex(datasource, table);
    OpalMap.Builder map = OpalMap.newBuilder();

    for(Variable variable : index.getVariables()) {
      map.addKeys(variable.getName());
      map.addValues(index.getName() + ":" + variable.getName());
    }
    return Response.ok(map.build()).build();
  }

  @GET
  @POST
  @Path("_search")
  public Response search(@Context HttpServletRequest servletRequest, String body) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Response> ref = new AtomicReference<Response>();

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
      return (r != null) ? r : Response.serverError().build();
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

  private static class JaxRsRestRequest extends AbstractRestRequest {

    private final String body;

    private final HttpServletRequest servletRequest;

    private final Map<String, String> params;

    private final String rawPath;

    private final String esUri;

    JaxRsRestRequest(ValueTableIndex tableIndex, HttpServletRequest servletRequest, String body, String path) {
      this.body = body;
      this.servletRequest = servletRequest;
      this.params = Maps.newHashMap();
      this.rawPath = tableIndex.getRequestPath() + "/" + path;

      // Reconstruct the uri
      String queryString = servletRequest.getQueryString();
      esUri = rawPath + (queryString != null ? ('?' + queryString) : "");

      RestUtils.decodeQueryString(queryString != null ? queryString : "", 0, params);
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
      if(pathEndPos < 0) {
        return esUri;
      } else {
        return esUri.substring(0, pathEndPos);
      }
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
      return servletRequest.getHeader(name);
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
