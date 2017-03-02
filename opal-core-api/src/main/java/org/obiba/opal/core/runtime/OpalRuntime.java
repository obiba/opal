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
import org.obiba.opal.spi.vcf.VCFStoreService;

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
  // VCF Store Service plugins
  //

  /**
   * Get all vcf services.
   *
   * @return
   */
  Collection<VCFStoreService> getVCFStoreServices();

  /**
   * Check that a vcf service exists.
   *
   * @param name
   * @return
   */
  boolean hasVCFStoreService(String name);

  /**
   * Get the {@link VCFStoreService} from name.
   *
   * @param name
   * @return
   * @throws java.util.NoSuchElementException
   */
  VCFStoreService getVCFStoreService(String name);

}
