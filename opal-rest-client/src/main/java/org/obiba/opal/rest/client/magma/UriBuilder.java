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

  public UriBuilder link(String link) {
    int idx = link.indexOf('?');
    if(idx > 0) {
      segment(link.substring(1, idx).split("/"));
      for(String q : link.substring(idx + 1).split("&")) {
        query(q.split("="));
      }
    } else {
      segment(link.substring(1).split("/"));
    }
    return this;
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
      return root.resolve(new URI(null, null, path(), hasQuery() ? query() : null, null));
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
      sb.append(e.getKey()).append('=').append(e.getValue() != null ? e.getValue() : "").append('&');
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
