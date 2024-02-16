/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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
import org.obiba.opal.spi.datasource.DatasourceService;
import org.obiba.opal.spi.datasource.DatasourceUsage;
import org.obiba.opal.web.model.Plugins;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;

@Component
@Scope("request")
@Path("/datasource-plugin/{name}")
public class DatasourcePluginResource {

  @PathParam("name")
  private String name;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private PluginsService pluginsService;

  @GET
  public Plugins.PluginDto get() {
    return Dtos.asDto(pluginsService.getInstalledPlugin(name));
  }

  @GET
  @Path("form")
  public String getSchemaJsonForUsage(@QueryParam("usage") @DefaultValue("import") String usage) {
    if (opalRuntime.hasServicePlugin(name)) {
      ServicePlugin servicePlugin = opalRuntime.getServicePlugin(name);
      if (servicePlugin instanceof DatasourceService) {
        DatasourceService asDatasourceService = (DatasourceService) servicePlugin;

        return asDatasourceService.getJSONSchemaForm(DatasourceUsage.valueOf(usage.toUpperCase())).toString();
      }
    }

    return "{}";
  }
}
