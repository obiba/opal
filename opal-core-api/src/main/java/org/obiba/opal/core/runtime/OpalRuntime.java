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

import java.util.Collection;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.spi.genotype.GenotypeStoreService;

public interface OpalRuntime extends SystemService {

  String EXTENSIONS = System.getProperty("OPAL_HOME") + "/extensions";

  String MAGMA_JS_EXTENSION = EXTENSIONS + "/magma-js";

  String WEBAPP_EXTENSION = EXTENSIONS + "/webapp";

  Set<Service> getServices();

  /**
   * For test purpose.
   * @return
   */
  boolean hasFileSystem();

  OpalFileSystem getFileSystem();

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

  /**
   * Check that a genotype service exists.
   *
   * @param name
   * @return
   */
  boolean hasGenotypeStoreService(String name);

  /**
   * Get the {@link GenotypeStoreService} from name.
   *
   * @param name
   * @return
   * @throws java.util.NoSuchElementException
   */
  GenotypeStoreService getGenotypeStoreService(String name);

  /**
   * Get all genotype services.
   *
   * @return
   */
  Collection<GenotypeStoreService> getGenotypeStoreServices();

}
