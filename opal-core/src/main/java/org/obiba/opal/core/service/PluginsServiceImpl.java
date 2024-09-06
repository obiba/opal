/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.OpalPlugin;
import org.obiba.opal.core.runtime.PluginsManager;
import org.obiba.opal.web.model.Opal;
import org.obiba.plugins.PluginPackage;
import org.obiba.plugins.PluginRepositoryCache;
import org.obiba.plugins.PluginRepositoryException;
import org.obiba.plugins.PluginResources;
import org.obiba.runtime.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PluginsServiceImpl implements PluginsService {

  @Autowired
  private PluginsManager pluginsManager;

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  private VersionProvider opalVersionProvider;

  private PluginRepositoryCache pluginRepositoryCache;

  @NotNull
  @Value("${org.obiba.opal.plugins.site}")
  private String updateSite;

  @Autowired
  public void setOpalVersionProvider(VersionProvider opalVersionProvider) {
    this.opalVersionProvider = opalVersionProvider;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
    pluginRepositoryCache = null;
  }

  @Override
  public String getUpdateSite() {
    return updateSite;
  }

  @Override
  public Date getLastUpdate() {
    return getPluginRepositoryCache().getLastUpdate();
  }

  @Override
  public PluginResources getInstalledPlugin(String name) {
    return pluginsManager.getPlugin(name);
  }

  @Override
  public void setInstalledPluginSiteProperties(String name, String properties) {
    try {
      pluginsManager.setPluginSiteProperties(name, properties);
    } catch (IOException e) {
      throw new PluginRepositoryException("Failed to save plugin " + name + " site properties: " + e.getMessage(), e);
    }
  }

  @Override
  public List<PluginPackage> getInstalledPlugins() {
    return pluginsManager.getPlugins().stream()
        .map(PluginPackage::new)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<String> getUninstalledPluginNames() {
    return pluginsManager.getUninstalledPluginNames();
  }

  @Override
  public List<PluginPackage> getUpdatablePlugins() {
    Collection<PluginResources> registeredPlugins = pluginsManager.getPlugins();
    // exclude already installed plugin packages whatever the version is
    return getPluginRepositoryCache().getOrUpdatePluginRepository().getPlugins().stream()
        .filter(PluginPackage::hasOpalVersion)
        .filter(pp -> !"opal-search".equals(pp.getType()))
        .filter(pp -> registeredPlugins.stream().anyMatch(rp -> pp.isNewerThan(rp.getName(), rp.getVersion())))
        .filter(pp -> opalVersionProvider.getVersion().compareTo(pp.getOpalVersion()) >= 0)
        .collect(Collectors.toList());
  }

  @Override
  public List<PluginPackage> getAvailablePlugins() {
    Collection<PluginResources> registeredPlugins = pluginsManager.getPlugins();
    // exclude already installed plugin packages whatever the version is
    return getPluginRepositoryCache().getOrUpdatePluginRepository().getPlugins().stream()
        .filter(PluginPackage::hasOpalVersion)
        .filter(pp -> !"opal-search".equals(pp.getType()))
        .filter(pp -> registeredPlugins.stream().noneMatch(rp -> pp.isSameAs(rp.getName())))
        .filter(pp -> opalVersionProvider.getVersion().compareTo(pp.getOpalVersion()) >= 0)
        .collect(Collectors.toList());
  }

  @Override
  public void installPlugin(String name, String version) {
    String pVersion = version;
    if (Strings.isNullOrEmpty(version)) {
      // no version specified: get the latest
      pVersion = getPluginRepositoryCache().getPluginLatestVersion(name);
    }
    try {
      File tmpDir = Files.createTempDir();
      installPlugin(getPluginRepositoryCache().downloadPlugin(name, pVersion, tmpDir), true);
      FileUtil.delete(tmpDir);
    } catch (IOException e) {
      throw new PluginRepositoryException("Failed to install plugin " + name + ":" + version + " : " + e.getMessage(), e);
    }
  }

  @Override
  public void installPlugin(String filePath) {
    installPlugin(opalFileSystemService.getFileSystem().resolveLocalFile(filePath), false);
  }

  @Override
  public void prepareUninstallPlugin(String name) {
    PluginResources plugin = pluginsManager.getPlugin(name);
    plugin.prepareForUninstall();
  }

  @Override
  public void cancelUninstallPlugin(String name) {
    PluginResources plugin = pluginsManager.getPlugin(name);
    plugin.cancelUninstall();
  }

  private void installPlugin(File pluginFile, boolean rmAfterInstall) {
    try {
      File pluginsDir = new File(OpalRuntime.PLUGINS_DIR);
      pluginsDir.mkdirs();
      FileUtil.copyFile(pluginFile, pluginsDir);
      if (rmAfterInstall) pluginFile.delete();
    } catch (IOException e) {
      throw new PluginRepositoryException("Plugin installation failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean restartRequired() {
    return pluginsManager.restartRequired();
  }

  private PluginRepositoryCache getPluginRepositoryCache() {
    if (pluginRepositoryCache == null)
      pluginRepositoryCache = new PluginRepositoryCache(opalVersionProvider, updateSite);
    return pluginRepositoryCache;
  }
}
