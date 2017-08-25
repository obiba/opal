/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.domain.plugins.PluginPackage;
import org.obiba.opal.core.domain.plugins.PluginRepository;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PluginsServiceImpl implements PluginsService {

  private static final String REPO_FILE = "plugins.json";

  @Autowired
  private OpalRuntime opalRuntime;

  @NotNull
  @Value("${org.obiba.opal.plugins.site}")
  private String updateSite;

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public String getUpdateSite() {
    return updateSite;
  }

  @Override
  public List<PluginPackage> getInstalledPlugins() {
    PluginRepository pluginRepository = getPluginRepository();
    // find corresponding plugin package or create a fake one
    return opalRuntime.getPlugins().stream()
        .map(rp -> pluginRepository.getPlugins().stream()
            .filter(pp -> pp.isSameAs(rp.getName(), rp.getType(), rp.getVersion())).findFirst()
            .orElse(new PluginPackage(rp.getName(), rp.getType(), rp.getTitle(), rp.getDescription(), rp.getVersion().toString(), rp.getOpalVersion().toString(), "")))
        .collect(Collectors.toList());
  }

  @Override
  public List<PluginPackage> getUpdatablePlugins() {
    Collection<Plugin> registeredPlugins = opalRuntime.getPlugins();
    // exclude already installed plugin packages whatever the version is
    return getPluginRepository().getPlugins().stream()
        .filter(pp -> registeredPlugins.stream().anyMatch(rp -> pp.isNewerThan(rp.getName(), rp.getType(), rp.getVersion())))
        .collect(Collectors.toList());
  }

  @Override
  public List<PluginPackage> getAvailablePlugins() {
    Collection<Plugin> registeredPlugins = opalRuntime.getPlugins();
    // exclude already installed plugin packages whatever the version is
    return getPluginRepository().getPlugins().stream()
        .filter(pp -> registeredPlugins.stream().noneMatch(rp -> pp.isSameAs(rp.getName(), rp.getType())))
        .collect(Collectors.toList());
  }

  @Override
  public void installPlugin(String name, String version) {

  }

  @Override
  public boolean restartRequired() {
    return false;
  }

  private PluginRepository getPluginRepository() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(new URL(updateSite + "/" + REPO_FILE), new TypeReference<PluginRepository>() {
      });
    } catch (Exception e) {
      throw new PluginRepositoryException(e);
    }
  }
}
