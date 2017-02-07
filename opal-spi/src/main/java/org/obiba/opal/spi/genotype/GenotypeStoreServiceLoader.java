/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.genotype;

import org.obiba.opal.spi.ServicePluginLoader;

import java.util.ServiceLoader;

/**
 * {@link GenotypeStoreService} loader.
 */
public class GenotypeStoreServiceLoader extends ServicePluginLoader<GenotypeStoreService> {

  private static GenotypeStoreServiceLoader loader;

  private ServiceLoader<GenotypeStoreService> serviceLoader = ServiceLoader.load(GenotypeStoreService.class);

  public static synchronized GenotypeStoreServiceLoader get() {
    if (loader == null) loader = new GenotypeStoreServiceLoader();
    return loader;
  }

  @Override
  protected ServiceLoader<GenotypeStoreService> getServiceLoader() {
    return serviceLoader;
  }
}
