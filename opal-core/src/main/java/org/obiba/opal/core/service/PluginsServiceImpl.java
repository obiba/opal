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
import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.domain.plugins.PluginPackage;
import org.obiba.opal.core.domain.plugins.PluginRepository;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Plugin;
import org.obiba.opal.core.runtime.PluginsManager;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PluginsServiceImpl implements PluginsService {

  private static final String REPO_FILE = "plugins.json";

  @Autowired
  private PluginsManager pluginsManager;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  private PluginRepositoryCache pluginRepositoryCache = new PluginRepositoryCache();

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
  public Date getLastUpdate() {
    return pluginRepositoryCache.getLastUpdate();
  }

  @Override
  public Plugin getInstalledPlugin(String name) {
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
        .map(rp -> new PluginPackage(rp.getName(), rp.getType(), rp.getTitle(), rp.getDescription(), rp.getVersion().toString(), rp.getOpalVersion().toString(), ""))
        .collect(Collectors.toList());
  }

  @Override
  public Collection<String> getUninstalledPluginNames() {
    return pluginsManager.getUninstalledPluginNames();
  }

  @Override
  public List<PluginPackage> getUpdatablePlugins() {
    Collection<Plugin> registeredPlugins = pluginsManager.getPlugins();
    // exclude already installed plugin packages whatever the version is
    return pluginRepositoryCache.getOrUpdatePluginRepository().getPlugins().stream()
        .filter(pp -> registeredPlugins.stream().anyMatch(rp -> pp.isNewerThan(rp.getName(), rp.getVersion())))
        .filter(pp -> opalConfigurationService.getOpalConfiguration().getVersion().compareTo(pp.getOpalVersion())>=0)
        .collect(Collectors.toList());
  }

  @Override
  public List<PluginPackage> getAvailablePlugins() {
    Collection<Plugin> registeredPlugins = pluginsManager.getPlugins();
    // exclude already installed plugin packages whatever the version is
    return pluginRepositoryCache.getOrUpdatePluginRepository().getPlugins().stream()
        .filter(pp -> registeredPlugins.stream().noneMatch(rp -> pp.isSameAs(rp.getName())))
        .filter(pp -> opalConfigurationService.getOpalConfiguration().getVersion().compareTo(pp.getOpalVersion())>=0)
        .collect(Collectors.toList());
  }

  @Override
  public void installPlugin(String name, String version) {
    String pVersion = version;
    if (Strings.isNullOrEmpty(version)) {
      // no version specified: get the latest
      pVersion = pluginRepositoryCache.getPluginLatestVersion(name);
    }
    try {
      File tmpDir = Files.createTempDir();
      installPlugin(pluginRepositoryCache.downloadPlugin(name, pVersion, tmpDir), true);
      FileUtil.delete(tmpDir);
    } catch (IOException e) {
      throw new PluginRepositoryException("Failed to install plugin " + name + ":" + version + " : " + e.getMessage(), e);
    }
  }
  
  @Override
  public void installPlugin(String filePath) {
    installPlugin(opalRuntime.getFileSystem().resolveLocalFile(filePath), false);
  }

  @Override
  public void prepareUninstallPlugin(String name) {
    Plugin plugin = pluginsManager.getPlugin(name);
    plugin.prepareForUninstall();
  }

  @Override
  public void cancelUninstallPlugin(String name) {
    Plugin plugin = pluginsManager.getPlugin(name);
    plugin.cancelUninstall();
  }

  private void installPlugin(File pluginFile, boolean rmAfterInstall) {
    try {
      File pluginsDir = new File(OpalRuntime.PLUGINS_DIR);
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

  /**
   * Cache to not query for update site at each request while managing the plugins.
   */
  private class PluginRepositoryCache {
    private static final int DELAY = 600;
    private PluginRepository pluginRepository;
    private long lastUpdate;

    private PluginRepository getOrUpdatePluginRepository() {
      if (hasExpired()) initializePluginRepository();
      return pluginRepository;
    }

    private boolean hasExpired() {
      return pluginRepository == null || (nowInSeconds() - lastUpdate) > DELAY;
    }

    private Date getLastUpdate() {
      return lastUpdate == 0 ? null : new Date(lastUpdate * 1000);
    }

    private File downloadPlugin(String name, String version, File tmpDir) throws IOException {
      Version versionObj = new Version(version);
      Optional<PluginPackage> pluginPackage = getOrUpdatePluginRepository().getPlugins().stream().filter(pp -> pp.isSameAs(name, versionObj)).findFirst();
      if (!pluginPackage.isPresent())
        throw new NoSuchElementException("Plugin " + name + ":" + version + " cannot be found");
      File pluginFile = new File(tmpDir, pluginPackage.get().getFileName());
      ReadableByteChannel rbc = Channels.newChannel(getRepositoryURL(pluginFile.getName()).openStream());
      FileOutputStream fos = new FileOutputStream(pluginFile);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
      return pluginFile;
    }


    private void initializePluginRepository() {
      ObjectMapper mapper = new ObjectMapper();
      try {
        pluginRepository = mapper.readValue(getRepositoryURL(REPO_FILE), new TypeReference<PluginRepository>() {});
        lastUpdate = nowInSeconds();
      } catch (Exception e) {
        throw new PluginRepositoryException("Cannot update plugin site: " + e.getMessage(), e);
      }
    }

    private long nowInSeconds() {
      return new Date().getTime() / 1000;
    }

    private URL getRepositoryURL(String fileName) throws MalformedURLException {
      String basePath = updateSite.endsWith("/") ? updateSite : updateSite + "/";
      return new URL(basePath + "/" + fileName);
    }

    public String getPluginLatestVersion(String name) {
      Version version = new Version("0.0.0");
      for (PluginPackage pp : getOrUpdatePluginRepository().getPlugins().stream()
          .filter(pp -> pp.getName().equals(name))
          .filter(pp -> opalConfigurationService.getOpalConfiguration().getVersion().compareTo(pp.getOpalVersion())>=0)
          .collect(Collectors.toList())) {
        if (pp.getVersion().compareTo(version)>0) version = pp.getVersion();
      }
      return version.toString();
    }
  }
}
