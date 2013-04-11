/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import org.obiba.opal.web.gwt.rest.client.UriBuilder;

final class Resources {

  private Resources() {}

  static String databases() {
    return "/jdbc/databases";
  }

  static String database(String name) {
    return UriBuilder.create().segment("jdbc", "database", name).build();
  }

  static String database(String name, String... more) {
    return UriBuilder.create().segment("jdbc", "database", name).segment(more).build();
  }

  public static String drivers() {
    return "/jdbc/drivers";
  }
}
