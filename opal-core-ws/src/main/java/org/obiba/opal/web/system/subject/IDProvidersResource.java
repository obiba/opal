/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.subject;

import org.obiba.opal.core.service.security.IDProvidersService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/system/idproviders")
public class IDProvidersResource {

  private static final Logger log = LoggerFactory.getLogger(IDProvidersResource.class);

  private final IDProvidersService idProvidersService;

  @Autowired
  public IDProvidersResource(IDProvidersService idProvidersService) {
    this.idProvidersService = idProvidersService;
  }

  @GET
  public List<Opal.IDProviderDto> list() {
    return idProvidersService.getConfigurations().stream()
        .map(Dtos::asDto)
        .collect(Collectors.toList());
  }

  @POST
  public Response createOrUpdate(@Context UriInfo uriInfo, Opal.IDProviderDto dto) {
    try {
      idProvidersService.saveConfiguration(Dtos.fromDto(dto));
      return Response.created(uriInfo.getBaseUriBuilder().path("/system/idprovider/" + dto.getName()).build()).build();
    } catch (IOException e) {
      log.error("Save OIDC configuration failed", e);
      return Response.serverError().build();
    }
  }

}