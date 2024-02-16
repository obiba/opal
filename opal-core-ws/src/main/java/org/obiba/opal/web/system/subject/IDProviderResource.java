/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.subject;

import org.obiba.oidc.OIDCConfiguration;
import org.obiba.opal.core.service.security.IDProvidersService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Component
@Scope("request")
@Path("/system/idprovider/{name}")
public class IDProviderResource {

  private static final Logger log = LoggerFactory.getLogger(IDProvidersResource.class);

  @PathParam("name")
  private String name;

  private final IDProvidersService idProvidersService;

  @Autowired
  public IDProviderResource(IDProvidersService idProvidersService) {
    this.idProvidersService = idProvidersService;
  }

  @GET
  public Response get() {
    OIDCConfiguration configuration = idProvidersService.getConfiguration(name);
    return Response.ok().entity(Dtos.asDto(configuration)).build();
  }

  @PUT
  public Response update(Opal.IDProviderDto dto) {
    if (!name.equals(dto.getName())) return Response.status(Response.Status.BAD_REQUEST).build();
    // ensure it exists
    idProvidersService.getConfiguration(name);
    try {
      idProvidersService.saveConfiguration(Dtos.fromDto(dto));
    } catch (IOException e) {
      log.error("Save OIDC configuration failed", e);
      return Response.serverError().build();
    }
    return Response.ok().build();
  }

  @PUT
  @Path("/_enable")
  public Response enable() {
    return enableConfiguration(true);
  }

  @DELETE
  @Path("/_enable")
  public Response disable() {
    return enableConfiguration(false);
  }

  @DELETE
  public Response delete() {
    idProvidersService.deleteConfiguration(name);
    return Response.ok().build();
  }

  private Response enableConfiguration(boolean enable) {
    try {
      idProvidersService.enableConfiguration(name, enable);
      return Response.ok().build();
    } catch (IOException e) {
      return Response.serverError().build();
    }
  }

}