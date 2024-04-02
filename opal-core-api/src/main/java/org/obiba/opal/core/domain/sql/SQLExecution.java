/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.sql;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import jakarta.annotation.Nullable;
import java.util.Date;

public class SQLExecution {

  private long started = new Date().getTime();

  private long ended;

  private String user;

  @Nullable
  private String datasource;

  private String query;

  @Nullable
  private String error;

  public long getStarted() {
    return started;
  }

  public void setStarted(long started) {
    this.started = started;
  }

  public long getEnded() {
    return ended;
  }

  public void setEnded(long ended) {
    this.ended = ended;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public boolean hasDatasource() {
    return !Strings.isNullOrEmpty(datasource);
  }

  @Nullable
  public String getDatasource() {
    return datasource;
  }

  public void setDatasource(@Nullable String datasource) {
    this.datasource = datasource;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public boolean hasError() {
    return !Strings.isNullOrEmpty(error);
  }

  @Nullable
  public String getError() {
    return error;
  }

  public void setError(@Nullable String error) {
    this.error = error;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SQLExecution that = (SQLExecution) o;
    return Objects.equal(started, that.started) && Objects.equal(ended, that.ended) && Objects.equal(user, that.user) && Objects.equal(datasource, that.datasource) && Objects.equal(query, that.query) && Objects.equal(error, that.error);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(started, ended, user, datasource, query, error);
  }

  @Override
  public String toString() {
    return "SQLExecution{" +
        "started=" + started +
        ", ended=" + ended +
        ", user='" + user + '\'' +
        ", datasource='" + datasource + '\'' +
        ", query='" + query + '\'' +
        ", error='" + error + '\'' +
        '}';
  }
}
