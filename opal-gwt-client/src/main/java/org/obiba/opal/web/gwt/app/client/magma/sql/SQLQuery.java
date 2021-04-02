/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.sql;

public class SQLQuery {

  private final String id;

  private final String datasource;

  private final String sql;

  private final boolean error;

  private final long time;

  public SQLQuery(String id, String datasource, String sql, boolean error, long time) {
    this.id = id;
    this.datasource = datasource;
    this.sql = sql;
    this.error = error;
    this.time = time;
  }

  public String getId() {
    return id;
  }

  public String getDatasource() {
    return datasource;
  }

  public String getSql() {
    return sql;
  }

  public boolean isError() {
    return error;
  }

  public long getTime() {
    return time;
  }
}
