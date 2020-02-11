/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.plugins;

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.resource.ResourceFactoryService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.plugins.PluginPackage;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Component
@Path("/resource-plugin")
public class ResourcePluginResource {

  private final OpalRuntime opalRuntime;

  private final PluginsService pluginsService;

  @Autowired
  public ResourcePluginResource(OpalRuntime opalRuntime, PluginsService pluginsService) {
    this.opalRuntime = opalRuntime;
    this.pluginsService = pluginsService;
  }

  @GET
  @Path("/{plg}")
  public Response get(@PathParam("plg") String pluginName) {
    if (opalRuntime.hasServicePlugin(pluginName)) {
      ServicePlugin servicePlugin = opalRuntime.getServicePlugin(pluginName);

      if (servicePlugin instanceof ResourceFactoryService) {
        PluginPackage pluginPackage = new PluginPackage(pluginsService.getInstalledPlugin(pluginName));
        Plugins.PluginPackageDto.Builder builder = Dtos.asDto(pluginPackage, null).toBuilder();
        builder.setExtension(Plugins.ResourcePluginPackageDto.resource,
            Dtos.asDto((ResourceFactoryService) servicePlugin)
                .build());
        return Response.ok(builder.build()).build();
      }

    }

    return Response.status(Status.NOT_FOUND).build();
  }
}
