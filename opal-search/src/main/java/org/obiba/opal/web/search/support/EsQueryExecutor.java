/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.support.AbstractRestRequest;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class EsQueryExecutor {

  private final ElasticSearchProvider elasticSearchProvider;

  public EsQueryExecutor(ElasticSearchProvider elasticSearchProvider) {
    this.elasticSearchProvider = elasticSearchProvider;
  }

  public JSONObject execute(JSONObject jsonBody) throws JSONException {

    Assert.notNull(jsonBody, "Query json body is null!");

    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<byte[]> ref = new AtomicReference<byte[]>();

    elasticSearchProvider.getRest()
      .dispatchRequest(new EsRestRequest(jsonBody.toString(), "_search"),
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
      byte[] content = ref.get();

      return new JSONObject(new String(content));

    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  private byte[] convert(RestResponse response) throws IOException {
    byte[] entity;
    if(response.contentThreadSafe()) {
      entity = response.content();
    } else {
      entity = new byte[response.contentLength()];
      System.arraycopy(response.content(), 0, entity, 0, response.contentLength());
    }
    return entity;
  }

  private static class EsRestRequest extends AbstractRestRequest {

    private final String body;

    private final Map<String, String> params;

    private final String esUri;

    private final Map<String, String> headers = ImmutableMap.of("Content-Type", "application/json");

    EsRestRequest(String body, String path) {
      this(body, path, new HashMap<String, String>());
    }

    EsRestRequest(String body, String path, Map<String, String> params) {
      this.body = body;
      this.params = params;
      this.esUri = "/" + path;
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
      return headers.get(name);
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
