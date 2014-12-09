/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.search;

import java.io.IOException;
import java.util.Enumeration;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.support.RestUtils;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/es")
public class EsResource {

  @Autowired
  private ElasticSearchProvider esProvider;

  @GET
  @PUT
  @DELETE
  @POST
  @OPTIONS
  @Path("/{uri:.*}")
  public Response proxy(@Context HttpServletRequest servletRequest, String body) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Response> ref = new AtomicReference<>();

    JaxRsRestRequest request = new JaxRsRestRequest(servletRequest, body);
    esProvider.getRest().dispatchRequest(request, new RestChannel(request) {

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
      entity = response.content().toBytes();
    } else {
      entity = new byte[response.content().length()];
      System.arraycopy(response.content().toBytes(), 0, entity, 0, response.content().length());
    }
    return Response.status(response.status().getStatus()).entity(entity).type(response.contentType()).build();
  }

  private static class JaxRsRestRequest extends HttpRequest {

    private final String body;

    private final HttpServletRequest servletRequest;

    private final Map<String, String> params;

    private final String rawPath;

    private final String esUri;

    JaxRsRestRequest(HttpServletRequest servletRequest, String body) {
      this.body = body;
      this.servletRequest = servletRequest;
      params = Maps.newHashMap();

      // Remove the opal-ws part of the requested uri
      rawPath = servletRequest.getRequestURI().replaceFirst(servletRequest.getContextPath(), "")
          .replaceFirst(servletRequest.getServletPath(), "").replaceFirst("/es", "");

      // Reconstruct the uri
      String queryString = servletRequest.getQueryString();
      esUri = rawPath + (queryString != null ? '?' + queryString : "");

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
      return servletRequest.getHeader(name);
    }

    @Override
    public Iterable<Map.Entry<String, String>> headers() {
      Map<String, String> headers = Maps.newHashMap();
      Enumeration<String> names = servletRequest.getHeaderNames();
      while(names.hasMoreElements()) {
        String name = names.nextElement();
        headers.put(name, servletRequest.getHeader(name));
      }
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
