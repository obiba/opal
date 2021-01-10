/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import org.obiba.opal.core.service.SystemService;
import org.obiba.plugins.PluginPackage;
import org.obiba.plugins.PluginResources;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface PluginsService extends SystemService {

    /**
     * Get the location of the plugin packages repository.
     *
     * @return
     */
    String getUpdateSite();

    /**
     * Get the last time at which the update site was successfully.
     *
     * @return
     */
    Date getLastUpdate();

    /**
     * Perform the plugin installation by retrieving the plugin package from the update site.
     *
     * @param name
     * @param version
     */
    void installPlugin(String name, String version);

    /**
     * Perform the plugin installation by retrieving the plugin package from the Opal file system.
     *
     * @param filePath
     */
    void installPlugin(String filePath);

    /**
     * Uninstall a plugin.
     *
     * @param name
     */
    void prepareUninstallPlugin(String name);

    /**
     * Cancel plugin uninstallation before it is effective.
     *
     * @param name
     */
    void cancelUninstallPlugin(String name);

    /**
     * Reports if system restart is required to finalize plugin installation.
     *
     * @return
     */
    boolean restartRequired();

    //
    // Plugins
    //

    /**
     * Get the installed plugin with the given name.
     *
     * @param name
     * @return
     */
    PluginResources getInstalledPlugin(String name);

    /**
     * Set the site properties of the installed plugin with the given name.
     *
     * @param name
     * @param properties
     */
    void setInstalledPluginSiteProperties(String name, String properties);

    /**
     * Get the plugins registered in the system.
     *
     * @return
     */
    List<PluginPackage> getInstalledPlugins();

    /**
     * Get the list of plugins that are marked to uninstallation.
     *
     * @return
     */
    Collection<String> getUninstalledPluginNames();

    /**
     * Get the plugins registered in the system that can be updated according to the update site registry.
     *
     * @return
     */
    List<PluginPackage> getUpdatablePlugins();

    /**
     * Get the plugins that are not installed and that are available from the update site registry.
     *
     * @return
     */
    List<PluginPackage> getAvailablePlugins();


}
