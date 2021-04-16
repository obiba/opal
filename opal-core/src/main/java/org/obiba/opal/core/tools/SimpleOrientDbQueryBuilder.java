/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.tools;

import com.google.common.base.Strings;

public final class SimpleOrientDbQueryBuilder {
  private String fields = "*";
  private String table;
  private String whereClauses;
  private String limit;
  private String order;

  public static SimpleOrientDbQueryBuilder newInstance() {
    return new SimpleOrientDbQueryBuilder();
  }

  private SimpleOrientDbQueryBuilder() {}

  public SimpleOrientDbQueryBuilder fields(String... values) {
    fields = values == null || values.length == 0 ? "*" : String.join(",", values);
    return this;
  }

  public SimpleOrientDbQueryBuilder table(String value) {
    table = value;
    return this;
  }

  public SimpleOrientDbQueryBuilder whereClauses(String... values) {
    whereClauses = String.join(" AND ", values);
    return this;
  }

  public SimpleOrientDbQueryBuilder order(String value) {
    order = Strings.isNullOrEmpty(value) || !"asc".equalsIgnoreCase(value) && !"desc".equalsIgnoreCase(value)
      ? "desc"
      : value.toLowerCase();

    return this;
  }

  public SimpleOrientDbQueryBuilder limit(int value) {
    limit = value < 1 ? "" : value + "";

    return this;
  }

  public String build() {
    StringBuilder builder = new StringBuilder()
      .append("SELECT ").append(fields)
      .append(" FROM ").append(table)
      .append(" WHERE ").append(whereClauses);

    if (!Strings.isNullOrEmpty(order)) builder.append(" ORDER BY created ").append(order);
    if (!Strings.isNullOrEmpty(limit)) builder.append(" LIMIT ").append(limit);

    return builder.toString();
  }

}
