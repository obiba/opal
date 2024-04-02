/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
  private ApplicationContext applicationContext;

  @Path("/tables")
  public VariableEntityTablesResource getTables() {
    return getVariableEntityTablesResource();
  }

  @GET
  @NoAuthorization
  public Response exists() {
    VariableEntityTablesResource resource = getVariableEntityTablesResource();
    if(!resource.getTables(1).isEmpty()) {
      return Response.ok().entity(Dtos.asDto(getVariableEntity()).build()).build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  private VariableEntityTablesResource getVariableEntityTablesResource() {
    VariableEntityTablesResource resource = applicationContext.getBean(VariableEntityTablesResource.class);
    resource.setVariableEntity(getVariableEntity());
    return resource;
  }

  private VariableEntityBean getVariableEntity() {
    return new VariableEntityBean(entityType, entityId);
  }

}
