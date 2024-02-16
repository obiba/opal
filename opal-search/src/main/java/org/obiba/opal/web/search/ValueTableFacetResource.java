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

import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.spi.search.SearchException;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.QueryTermDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * Elastic Search API resource that provides a secure mechanism of performing queries on the indexes without exposing
 * individual data,
 */
@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/facet")
public class ValueTableFacetResource {

  @Autowired
  protected OpalSearchService opalSearchService;

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  /**
   * Given a variable name, returns its corresponding facet (terms, statistical). Only categorical and continuous
   * variables are treated.
   *
   * @param variable
   * @param type
   * @return
   */
  @GET
  @Path("/variable/{variable}/_search")
  @Transactional(readOnly = true)
  public Response search(@PathParam("variable") String variable, @Nullable @QueryParam("type") String type) throws SearchException {
    if (!opalSearchService.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    Search.QueryResultDto dtoResult;
    try {
      QueryTermDtoBuilder dtoBuilder = new QueryTermDtoBuilder("0").variableTermDto(variable, type);
      dtoResult = opalSearchService.executeQuery(datasource, table, dtoBuilder.build());
    } catch (UnsupportedOperationException e) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    return Response.ok().entity(dtoResult).build();
  }
}