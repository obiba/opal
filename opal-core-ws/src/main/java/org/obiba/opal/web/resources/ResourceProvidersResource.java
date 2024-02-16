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
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.Resources;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.stream.Collectors;

@Component
@Path("/resource-providers")
public class ResourceProvidersResource {

  @Autowired
  private ResourceProvidersService resourceProvidersService;

  @Autowired
  private RServerManagerService rServerManagerService;

  @GET
  @NoAuthorization
  public Resources.ResourceProvidersDto list() {
    Resources.ResourceProvidersDto.Builder builder = Resources.ResourceProvidersDto.newBuilder();
    builder.addAllProviders(resourceProvidersService.getResourceProviders().stream()
        .map(Dtos::asDto).collect(Collectors.toList()));
    builder.addAllCategories(resourceProvidersService.getAllCategories().stream()
        .map(Dtos::asDto).collect(Collectors.toList()));
    return builder.build();
  }

  @GET
  @Path("/_status")
  @NoAuthorization
  public Resources.ResourceProvidersStatusDto status() {
    Resources.ResourceProvidersStatusDto.Builder builder = Resources.ResourceProvidersStatusDto.newBuilder()
        .setProvidersCount(resourceProvidersService.getResourceProviders().size())
        .setRServerRunning(rServerManagerService.getDefaultRServer().isRunning());
    return builder.build();
  }

}
