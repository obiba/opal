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
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.service.ContingencyService;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

/**
 * Elastic Search API resource that provides a secure mechanism of performing queries on the indexes without exposing
 * individual data,
 */
@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/facets")
public class ValueTableFacetsResource {

  private static final Logger log = LoggerFactory.getLogger(ValueTableFacetsResource.class);

  @Autowired
  protected OpalSearchService opalSearchService;

  private final ContingencyService contingencyService;

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @Autowired
  public ValueTableFacetsResource(ContingencyService contingencyService) {
    this.contingencyService = contingencyService;
  }

  @POST
  @Path("/_search")
  @Transactional(readOnly = true)
  public Response search(Search.QueryTermsDto dtoQueries) {
    List<Search.QueryTermDto> queries = dtoQueries.getQueriesList();
    Optional<Search.QueryTermDto> queryOpt = queries.stream().filter((q) -> q.hasFacet() && "_total".equals(q.getFacet()))
        .findFirst();
    if (queryOpt.isEmpty())
      return Response.status(Response.Status.BAD_REQUEST).build();

    try {
      Search.QueryTermDto queryDto = queryOpt.get();

      Search.VariableTermDto term = queryDto.getExtension(Search.VariableTermDto.field);
      String crossVar1 = term.getVariable();

      Search.LogicalTermDto logical = queryDto.getExtension(Search.LogicalTermDto.facetFilter);
      List<Search.FilterDto> filters = logical.getExtension(Search.FilterDto.filters);
      String crossVar0 = filters.getFirst().getVariable();
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
    } catch (Exception e) {
      log.debug("Unexpected query terms: {}", dtoQueries, e);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }

}