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

final class Resources {

  static String databases() {
    return "/jdbc/databases";
  }

  static String database(String name) {
    return "/jdbc/database/" + name;
  }

  static String database(String name, String... more) {
    String r = database(name);
    if(more != null) {
      StringBuilder sb = new StringBuilder(r);
      for(String s : more) {
        sb.append("/").append(s);
      }
      r = sb.toString();
    }
    return r;
  }

  public static String drivers() {
    return "/jdbc/drivers";
  }
}
