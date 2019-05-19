/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.rest.client;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.http.client.URL;

public class UriUtils {
  public static Map<String, String> getQueryParameters(String uri) {
    Map<String, String> parameters = Maps.newHashMap();

    if(uri.contains("?")) {
      String[] parts = uri.split("\\?");
      for(String param : parts[1].split("&")) {
        String[] kvPair = param.split("=", 2);
        parameters.put(URL.decode(kvPair[0]), URL.decode(kvPair[1]));
      }
    }

    return parameters;
  }

  public static String getPath(String uri) {
    if(uri.contains("?")) {
      String[] parts = uri.split("\\?");
      return parts[0];
    }

    return uri;
  }

  public static String removeQueryParam(String uri, String name) {
    String path = UriUtils.getPath(uri);
    Map<String, String> queryParams = UriUtils.getQueryParameters(uri);
    queryParams.remove(name);

    return UriBuilder.create().fromPath(path).query(queryParams).build();
  }

  public static String updateQueryParams(String uri, Map<String, String> params) {
    String path = UriUtils.getPath(uri);
    Map<String, String> queryParams = UriUtils.getQueryParameters(uri);

    for(Map.Entry<String, String> arg : params.entrySet()) {
      queryParams.put(arg.getKey(), arg.getValue());
    }

    return UriBuilder.create().fromPath(path).query(queryParams).build();
  }
}
