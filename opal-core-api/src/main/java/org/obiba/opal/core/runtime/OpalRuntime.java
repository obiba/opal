/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime;

import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.spi.ServicePlugin;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collection;
import java.util.Set;

public interface OpalRuntime extends SystemService {

  String DATA_DIR = System.getProperty("OPAL_HOME") + File.separator + "data";

  String WORK_DIR = System.getProperty("OPAL_HOME") + File.separator + "work";

  String PLUGINS_DIR = System.getProperty("OPAL_HOME") + File.separator + "plugins";

  String PLUGIN_PROPERTIES = "plugin.properties";

  String EXTENSIONS_DIR = System.getProperty("OPAL_HOME") + File.separator + "extensions";

  String MAGMA_JS_EXTENSION = EXTENSIONS_DIR + File.separator + "magma-js";

  String WEBAPP_EXTENSION = EXTENSIONS_DIR + File.separator + "webapp";

  //
  // File system
  //

  /**
   * For test purpose.
   *
   * @return
   */
  boolean hasFileSystem();

  /**
   * Get the opal file system.
   *
   * @return
   */
  OpalFileSystem getFileSystem();

  //
  // Core services
  //

  /**
   * Get the core services.
   *
   * @return
   */
  Set<Service> getServices();

  /**
   * True if service with given name is available in Opal Runtime.
   */
  boolean hasService(String name);

  /**
   * Get the service with the given name.
   *
   * @param name Service name
   * @throws throw NoSuchService runtime exception if not found (hasService() must be evaluated first)
   */
  @NotNull
  Service getService(String name) throws NoSuchServiceException;

  //
  // Plugins
  //

  /**
   * Check if there is any plugin registered in the system.
   *
   * @return
   */
  boolean hasPlugins();

  /**
   * Get the plugins registered in the system.
   *
   * @return
   */
  Collection<Plugin> getPlugins();

  /**
   * Check if there is a plugin with given name.
   *
   * @param name
   * @return
   */
  boolean hasPlugin(String name);

  /**
   * Get the plugin registered in the system with the given name.
   *
   * @param name
   * @return
   */
  Plugin getPlugin(String name);

  //
  // VCF Store Service plugins
  //

  /**
   * Check if there is any service plugin loaded.
   *
   * @return
   */
  boolean hasServicePlugins();

  /**
   * Get all service plugins.
   *
   * @return
   */
  Collection<ServicePlugin> getServicePlugins();

  /**
   * Check that a service service exists.
   *
   * @param name
   * @return
   */
  boolean hasServicePlugin(String name);

  /**
   * Get the {@link ServicePlugin} from name.
   *
   * @param name
   * @return
   * @throws java.util.NoSuchElementException
   */
  ServicePlugin getServicePlugin(String name);

  boolean isVCFStorePluginService(ServicePlugin servicePlugin);
}
