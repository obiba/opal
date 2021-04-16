/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.vcf;


import org.obiba.plugins.spi.ServicePlugin;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * {@link ServicePlugin} to handle the vcf data.
 */
public interface VCFStoreService extends ServicePlugin {

  String SERVICE_TYPE = "vcf-store";

  /**
   * Get the registered store names.
   *
   * @return
   */
  Collection<String> getStoreNames();

  /**
   * Check {@link VCFStore} existence.
   *
   * @param name
   * @return
   */
  boolean hasStore(String name);

  /**
   * Get the {@link VCFStore} with given name. Throws an exception if not found.
   *
   * @param name
   * @return
   */
  VCFStore getStore(String name) throws NoSuchElementException;

  /**
   * Create a {@link VCFStore} with given name. Throws an exception if already exists.
   *
   * @param name
   * @return the created store.
   */
  VCFStore createStore(String name);

  /**
   * Delete the {@link VCFStore} with given name. Ignore if no such store is found.
   *
   * @param name
   */
  void deleteStore(String name);

}
