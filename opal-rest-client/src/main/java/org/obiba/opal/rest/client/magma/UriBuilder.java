/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.rest.client.magma;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UriBuilder {

  private final URI root;

  private final List<String> pathSegments = new LinkedList<>();

  private final Map<String, String> query = new LinkedHashMap<>();

  public UriBuilder(URI root) {
    this.root = root;
  }

  public UriBuilder(UriBuilder builder) {
    root = builder.root;
    pathSegments.addAll(builder.pathSegments);
    query.putAll(builder.query);
  }

  public UriBuilder segment(String... segments) {
    pathSegments.addAll(Arrays.asList(segments));
    return this;
  }

  @SuppressWarnings("UnusedDeclaration")
  public UriBuilder query(String... keyValue) {
    for(int i = 0; i < keyValue.length; i += 2) {
      query(keyValue[i], keyValue[i + 1]);
    }
    return this;
  }

  /**
   * Adds the segments and query of a link as served by Opal. Links are percent-encoded, and build() encodes again, so
   * they are decoded first, segment by segment so that an encoded slash stays in its name; a segment an older server
   * left raw is taken as is.
   */
  public UriBuilder link(String link) {
    int idx = link.indexOf('?');
    String path = idx > 0 ? link.substring(0, idx) : link;
    for(String segment : path.substring(1).split("/")) {
      segment(decodeSegment(segment));
    }
    if(idx > 0) {
      for(String q : link.substring(idx + 1).split("&")) {
        int eq = q.indexOf('=');
        query(URLDecoder.decode(eq < 0 ? q : q.substring(0, eq), StandardCharsets.UTF_8),
            eq < 0 ? "" : URLDecoder.decode(q.substring(eq + 1), StandardCharsets.UTF_8));
      }
    }
    return this;
  }

  private static String decodeSegment(String segment) {
    try {
      return new URI(segment).getPath();
    } catch(URISyntaxException e) {
      return segment;
    }
  }

  public UriBuilder query(String parameter, String value) {
    query.put(parameter, value);
    return this;
  }

  public UriBuilder newBuilder() {
    return new UriBuilder(this);
  }

  public URI build() {
    try {
      // the query is encoded here, the multi-argument URI constructor would quote its percent signs again
      URI uri = root.resolve(new URI(null, null, path(), null, null));
      return hasQuery() ? URI.create(uri + "?" + query()) : uri;
    } catch(URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean hasQuery() {
    return !query.isEmpty();
  }

  private String query() {
    StringBuilder sb = new StringBuilder();
    for(Map.Entry<String, String> e : query.entrySet()) {
      sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)).append('=')
          .append(e.getValue() != null ? URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8) : "").append('&');
    }
    if(sb.length() > 0) {
      sb.setLength(sb.length() - 1);
    }
    return sb.toString();

  }

  private String path() {
    StringBuilder sb = new StringBuilder(!pathSegments.isEmpty() ? pathSegments.get(0) : "");
    for(int i = 1; i < pathSegments.size(); i++) {
      sb.append('/').append(pathSegments.get(i));
    }
    return sb.toString();
  }

}
