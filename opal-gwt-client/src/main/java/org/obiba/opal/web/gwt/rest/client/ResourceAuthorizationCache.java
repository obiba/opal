/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ResourceAuthorizationCache {

  private final Map<String, Set<HttpMethod>> cache = new HashMap<String, Set<HttpMethod>>();

  public Set<HttpMethod> get(String resource) {
    return cache.get(normalize(resource));
  }

  public void put(String resource, Set<HttpMethod> authorizations) {
    cache.put(normalize(resource), authorizations);
  }

  public boolean contains(String resource) {
    return cache.containsKey(normalize(resource));
  }

  public void remove(String resource) {
    cache.remove(normalize(resource));
  }

  public void clear() {
    cache.clear();
  }

  private String normalize(String resource) {
    String str = resource;
    if(resource.startsWith("/ws")) {
      str = resource.substring(3);
    }
    return str;
  }

  @Override
  public String toString() {
    return cache.toString();
  }
}
