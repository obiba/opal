/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.resources;

import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.web.model.Resources;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.stream.Collectors;

@Component
@Path("/resource-provider/{name}")
@Scope("request")
public class ResourceProviderResource {

  @Autowired
  private ResourceProvidersService resourceProvidersService;

  @PathParam("name")
  private String name;

  @GET
  @NoAuthorization
  public Resources.ResourceProviderDto get() {
    return Dtos.asDto(resourceProvidersService.getResourceProvider(name));
  }

  @GET
  @NoAuthorization
  @Path("/factory/{type}")
  public Resources.ResourceFactoryDto get(@PathParam("type") String type) {
    return Dtos.asDto(resourceProvidersService.getResourceFactory(name, type));
  }
}
