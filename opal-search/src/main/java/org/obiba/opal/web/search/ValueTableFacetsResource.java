/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @POST
  @Path("/_search")
  @Transactional(readOnly = true)
  public Response search(Search.QueryTermsDto dtoQueries) throws SearchException {
    if (!opalSearchService.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    try {
      Search.QueryResultDto dtoResult = opalSearchService.executeQuery(datasource, table, dtoQueries);
      return Response.ok().entity(dtoResult).build();
    } catch (UnsupportedOperationException e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }
}