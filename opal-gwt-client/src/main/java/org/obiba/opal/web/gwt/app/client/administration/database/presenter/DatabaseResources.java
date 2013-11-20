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

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class DatabaseResources {

  private DatabaseResources() {}

  public static String databases() {
    return "/system/databases";
  }

  public static String databasesWithSettings() {
    return databases() + "?settings=true";
  }

  public static String sqlDatabases() {
    return databases() + "/sql";
  }

  public static String mongoDatabases() {
    return databases() + "/mongodb";
  }

  public static String storageDatabases() {
    return databases() + "?usage=storage";
  }

  public static String importDatabases() {
    return databases() + "?usage=import";
  }

  public static String exportDatabases() {
    return databases() + "?usage=export";
  }

  public static String identifiersDatabase() {
    return databases() + "/identifiers";
  }

  public static String database(String name) {
    return UriBuilder.create().segment("system", "database", name).build();
  }

  public static String database(String name, String... more) {
    return UriBuilder.create().segment("system", "database", name).segment(more).build();
  }

  public static String drivers() {
    return databases() + "/jdbc-drivers";
  }
}
