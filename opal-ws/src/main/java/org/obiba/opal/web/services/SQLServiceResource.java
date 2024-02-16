/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.services;

import org.obiba.opal.core.domain.sql.SQLExecution;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.web.model.SQL;
import org.obiba.opal.web.sql.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/service/sql")
public class SQLServiceResource {

  @Autowired
  private SQLService sqlService;

  @GET
  @Path("/history")
  public List<SQL.SQLExecutionDto> getHistory(@QueryParam("user") String user, @QueryParam("datasource") String datasource,
                                              @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("100") int limit) {
    List<SQLExecution> execs = sqlService.getSQLExecutions(user, datasource);
    return execs.subList(offset, Math.min(execs.size(), offset + limit)).stream()
        .map(Dtos::asDto).collect(Collectors.toList());
  }


}
