/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import org.obiba.opal.web.gwt.rest.client.UriBuilder;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
final class Resources {

  private Resources() {}

  static String indices() {
    return "/service/search/indices";
  }

  static String indicesEnabled() {
    return "/service/search/indices/cfg/enabled";
  }

  static String index(String datasource, String table) {
    return UriBuilder.create().segment("datasource", datasource, "table", table, "index").build();
  }

  static String updateSchedule(String datasource, String table) {
    return UriBuilder.create().segment("datasource", datasource, "table", table, "index", "schedule").build();
  }

  static String searchService() {
    return "/service/search";
  }

  static String searchServiceEnabled() {
    return "/service/search/cfg/enabled";
  }
}
