/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.BooleanType;
import org.obiba.opal.search.service.ContingencyService;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.spi.search.SearchException;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contingency query in the scope of a single table.
 */
@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/_contingency")
public class ValueTableContingencyResource {

  private static final Logger log = LoggerFactory.getLogger(ValueTableContingencyResource.class);

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  private final ContingencyService contingencyService;

  @Autowired
  public ValueTableContingencyResource(ContingencyService contingencyService) {
    this.contingencyService = contingencyService;
  }

  @GET
  @Transactional(readOnly = true)
  public Response getFacets(@QueryParam("v0") String crossVar0, @QueryParam("v1") String crossVar1) throws SearchException {
    if (Strings.isNullOrEmpty(crossVar0) || Strings.isNullOrEmpty(crossVar1))
      return Response.status(Response.Status.BAD_REQUEST).build();
    log.info("Contingency table: {} x {}", crossVar0, crossVar1);

    // check table exists
    ValueTable table = getValueTable();

    return Response.ok()
        .entity(contingencyService.getContingency(
            String.format("%s:%s", table.getTableReference(),  crossVar0),
            String.format("%s:%s", table.getTableReference(),  crossVar1)))
        .build();
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }

}