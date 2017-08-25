/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.plugins;


import com.google.common.base.Strings;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.domain.plugins.PluginPackage;
import org.obiba.opal.web.model.Plugins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/system/plugins")
public class SystemPluginsResource {

  @Autowired
  PluginsService pluginsService;

  @GET
  public List<Plugins.PluginPackageDto> getInstalledPlugins() {
    return pluginsService.getInstalledPlugins().stream()
        .map(this::asDto).collect(Collectors.toList());
  }

  @GET
  @Path("/_updates")
  public List<Plugins.PluginPackageDto> getUpdatablePlugins() {
    return pluginsService.getUpdatablePlugins().stream()
        .map(this::asDto).collect(Collectors.toList());
  }

  @GET
  @Path("/_available")
  public List<Plugins.PluginPackageDto> getAvailablePlugins() {
    return pluginsService.getAvailablePlugins().stream()
        .map(this::asDto).collect(Collectors.toList());
  }

  private Plugins.PluginPackageDto asDto(PluginPackage pluginPackage) {
    Plugins.PluginPackageDto.Builder buider = Plugins.PluginPackageDto.newBuilder()
        .setName(pluginPackage.getName())
        .setType(pluginPackage.getType())
        .setTitle(pluginPackage.getTitle())
        .setDescription(pluginPackage.getDescription())
        .setVersion(pluginPackage.getVersion().toString())
        .setOpalVersion(pluginPackage.getOpalVersion().toString());
    if (!Strings.isNullOrEmpty(pluginPackage.getFileName()))
        buider.setFile(pluginPackage.getFileName());
    return buider.build();
  }
  
}
