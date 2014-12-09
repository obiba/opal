/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * This class is responsible for executing an elastic search. The input and output of this class are DTO format.
 */
public class EsSearchQueryExecutor implements SearchQueryExecutor {

  private static final Logger log = LoggerFactory.getLogger(EsSearchQueryExecutor.class);

  private final ElasticSearchProvider esProvider;

  private final int termsFacetSizeLimit;

  public EsSearchQueryExecutor(@NotNull ElasticSearchProvider esProvider, int termsFacetSizeLimit) {
    Assert.notNull(esProvider, "Elastic Search provider is null!");
    this.esProvider = esProvider;
    this.termsFacetSizeLimit = termsFacetSizeLimit;
  }

  /**
   * Executes an elastic search query.
   *
   * @param indexManagerHelper
   * @param dtoQueries
   * @return
   * @throws JSONException
   */
  @Override
  public Search.QueryResultDto execute(@NotNull IndexManagerHelper indexManagerHelper,
      @NotNull Search.QueryTermsDto dtoQueries) throws JSONException {

    Assert.notNull(indexManagerHelper, "Index Manager Helper is null!");
    Assert.notNull(dtoQueries, "Query dto request is null!");

    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Response> ref = new AtomicReference<>();

    String body = build(dtoQueries, indexManagerHelper);

    EsRestRequest request = new EsRestRequest(indexManagerHelper.getValueTableIndex(), body, "_search");
    esProvider.getRest().dispatchRequest(request,
        new RestChannel(request) {

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

      // TODO separate the methods for GET and POST ; one with one query, other with many
      EsResultConverter converter = new EsResultConverter();

      return converter.convert(jsonContent);

    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Executes a single elastic search query.
   *
   * @param indexManagerHelper
   * @param dtoQuery
   * @return
   * @throws JSONException
   */
  @Override
  public Search.QueryResultDto execute(@NotNull IndexManagerHelper indexManagerHelper,
      @NotNull Search.QueryTermDto dtoQuery) throws JSONException {

    Assert.notNull(indexManagerHelper, "Index Manager Helper is null!");
    Assert.notNull(dtoQuery, "Query dto request is null!");

    // wrap in a QueryTermsDto for API uniformity
    Search.QueryTermsDto dtoQueries = Search.QueryTermsDto.newBuilder().addQueries(dtoQuery).build();

    return execute(indexManagerHelper, dtoQueries);
  }

  private Response convert(RestResponse response) throws IOException {
    byte[] entity;
    if(response.contentThreadSafe()) {
      entity = response.content().toBytes();
    } else {
      entity = new byte[response.content().length()];
      System.arraycopy(response.content().toBytes(), 0, entity, 0, response.content().length());
    }
    return Response.status(response.status().getStatus()).entity(entity).type(response.contentType()).build();
  }

  private String build(Search.QueryTermsDto dtoQueries, IndexManagerHelper indexManagerHelper) throws JSONException {
    QueryTermConverter converter = new QueryTermConverter(indexManagerHelper, termsFacetSizeLimit);
    JSONObject queryJSON = converter.convert(dtoQueries);

    return queryJSON.toString();
  }

  private static class EsRestRequest extends HttpRequest {

    private final String body;

    private final Map<String, String> params;

    private final String esUri;

    private final Map<String, String> headers = ImmutableMap.of("Content-Type", "application/json");

    EsRestRequest(ValueTableIndex tableIndex, String body, String path) {
      this(tableIndex, body, path, new HashMap<String, String>());
    }

    EsRestRequest(ValueTableIndex tableIndex, String body, String path, Map<String, String> params) {
      this.body = body;
      this.params = params;
      esUri = tableIndex.getRequestPath() + "/" + path;
    }

    @Override
    public Method method() {
      return Method.GET;
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
    public BytesReference content() {
      return new BytesArray(body);
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

  }

}