/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.search.ValuesIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
@Transactional
@Scope("request")
@Path("/entity/{id}/type/{type}")
public class VariableEntityResource {

  @PathParam("id")
  private String entityId;

  @PathParam("type")
  private String entityType;

  @Autowired
  private OpalSearchService opalSearchService;

  @Autowired
  private ValuesIndexManager indexManager;

  @Autowired
  private ElasticSearchProvider esProvider;

  @Path("/tables")
  public VariableEntityTablesResource getTables() {
    return new VariableEntityTablesResource(getVariableEntity(), opalSearchService, indexManager, esProvider);
  }

  @GET
  @NoAuthorization
  public Response exists() {

    VariableEntityTablesResource var = new VariableEntityTablesResource(getVariableEntity(), opalSearchService,
        indexManager, esProvider);

    if(var.getTables(1).size() > 0) {
      return Response.ok().entity(Dtos.asDto(getVariableEntity()).build()).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }

  private VariableEntityBean getVariableEntity() {
    return new VariableEntityBean(entityType, entityId);
  }

}
