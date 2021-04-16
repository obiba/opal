/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.sql;

import org.obiba.opal.core.domain.sql.SQLExecution;
import org.obiba.opal.web.model.SQL;

public class Dtos {
  private Dtos() {
  }

  public static SQL.SQLExecutionDto asDto(SQLExecution exec) {
    SQL.SQLExecutionDto.Builder builder = SQL.SQLExecutionDto.newBuilder()
        .setUser(exec.getUser())
        .setQuery(exec.getQuery())
        .setStart(exec.getStarted())
        .setEnd(exec.getEnded());
    if (exec.hasDatasource())
      builder.setDatasource(exec.getDatasource());
    if (exec.hasError())
      builder.setError(exec.getError());
    return builder.build();
  }
}
