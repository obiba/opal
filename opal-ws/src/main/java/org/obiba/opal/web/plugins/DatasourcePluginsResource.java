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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/datasource-plugins")
public class DatasourcePluginsResource {
  
  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private PluginsService pluginsService;

  @GET
  public Plugins.PluginPackagesDto list(@QueryParam("usage") @DefaultValue("import") String usage) {
    DatasourceUsage dsUsage = DatasourceUsage.valueOf(usage.toUpperCase());
    List<Plugins.PluginPackageDto> dsPackages = pluginsService.getInstalledPlugins().stream()
        .filter(p -> DatasourceService.SERVICE_TYPE.equals(p.getType()))
        .filter(p -> {
          try {
            return ((DatasourceService) opalRuntime.getServicePlugin(p.getName())).getUsages().contains(dsUsage);
          } catch (Exception e) {
            return false;
          }
        })
        .map(p -> {
          Plugins.PluginPackageDto.Builder builder = Dtos.asDto(p, null).toBuilder();
          DatasourceService dsService = ((DatasourceService) opalRuntime.getServicePlugin(p.getName()));
          builder.setExtension(Plugins.DatasourcePluginPackageDto.datasource, Plugins.DatasourcePluginPackageDto.newBuilder()
              .addAllUsages(dsService.getUsages().stream().map(Enum::name).collect(Collectors.toList()))
              .setGroup(dsService.getGroup().name()).build());
          return builder.build();
        })
        .collect(Collectors.toList());
    return Dtos.asDto(pluginsService.getUpdateSite(), pluginsService.getLastUpdate(), pluginsService.restartRequired())
        .addAllPackages(dsPackages).build();
  }

}
