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

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class SQLHistoryEntry {

  private final String id;

  private final String datasource;

  private final String sql;

  private final String error;

  private final long time;

  public SQLHistoryEntry(String id, String datasource, String sql, String error, long time) {
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
    return !Strings.isNullOrEmpty(error);
  }

  public long getTime() {
    return time;
  }

  public String getError() {
    return error;
  }

  public String getSafeHtmlError() {
    return SafeHtmlUtils.htmlEscape(error);
  }
}
