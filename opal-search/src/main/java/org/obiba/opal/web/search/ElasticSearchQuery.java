/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
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
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.support.AbstractRestRequest;
import org.elasticsearch.rest.support.RestUtils;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchQuery {

  private static final Logger log = LoggerFactory.getLogger(ElasticSearchQuery.class);

  private final ElasticSearchProvider esProvider;

  private final HttpServletRequest servletRequest;

  public ElasticSearchQuery(HttpServletRequest servletRequest, ElasticSearchProvider esProvider) {
    this.servletRequest = servletRequest;
    this.esProvider = esProvider;
  }

  public Search.QueryResultDto execute(ValueTableIndex index, Search.QueryTermDto dtoQuery) throws JSONException {
    log.info("Executing query");

    String body = build(dtoQuery, index.getName());

    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Response> ref = new AtomicReference<Response>();

    esProvider.getRest()
        .dispatchRequest(new JaxRsRestRequest(index, servletRequest, body, "_search"), new RestChannel() {

          @Override
          public void sendResponse(RestResponse response) {
            log.info(response.toString());

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

      JSONObject jsonContent = new JSONObject(new String((byte[]) r.getEntity()));
      JsonToDtoConverter converter = new JsonToDtoConverter(dtoQuery);

      return converter.convert(jsonContent);

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

  private String build(Search.QueryTermDto dto, String indexName) throws JSONException {
    DtoToJsonConverter converter = new DtoToJsonConverter(indexName);
    JSONObject queryJSON = converter.convert(dto);

    return queryJSON.toString();
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
      params = Maps.newHashMap();
      rawPath = tableIndex.getRequestPath() + "/" + path;

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
    public BytesReference content() {
      return new BytesArray(body);
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

  }

}
