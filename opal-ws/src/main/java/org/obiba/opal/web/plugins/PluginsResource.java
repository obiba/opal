/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.plugins;


import com.google.common.base.Strings;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.web.model.Plugins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.stream.Collectors;

@Component
@Path("/plugins")
public class PluginsResource {

  @Autowired
  PluginsService pluginsService;

  @GET
  public Plugins.PluginPackagesDto getInstalledPlugins(@QueryParam("type") String type) {
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired(),
        pluginsService.getInstalledPlugins().stream()
            .filter(p -> Strings.isNullOrEmpty(type) || (type.equals(p.getType())))
            .collect(Collectors.toList()), pluginsService.getUninstalledPluginNames());
  }

  @GET
  @Path("/_updates")
  public Plugins.PluginPackagesDto getUpdatablePlugins(@QueryParam("type") String type) {
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired(),
        pluginsService.getUpdatablePlugins().stream()
            .filter(p -> Strings.isNullOrEmpty(type) || (type.equals(p.getType())))
            .collect(Collectors.toList()));
  }

  @GET
  @Path("/_available")
  public Plugins.PluginPackagesDto getAvailablePlugins(@QueryParam("type") String type) {
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired(),
        pluginsService.getAvailablePlugins().stream()
            .filter(p -> Strings.isNullOrEmpty(type) || (type.equals(p.getType())))
            .collect(Collectors.toList()));
  }

  @POST
  public Response installPlugin(@QueryParam("name") String name, @QueryParam("version") String version, @QueryParam("file") String file) {
    if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(version)) {
      pluginsService.installPlugin(name, version);
      return Response.ok().build();
    }
    if (!Strings.isNullOrEmpty(file)) {
      pluginsService.installPlugin(file);
      return Response.ok().build();
    }
    return Response.status(Response.Status.BAD_REQUEST).build();
  }

}
