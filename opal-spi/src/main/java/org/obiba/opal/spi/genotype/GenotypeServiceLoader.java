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
 * {@link GenotypeService} loader.
 */
public class GenotypeServiceLoader extends ServicePluginLoader<GenotypeService> {

  private static GenotypeServiceLoader loader;

  private ServiceLoader<GenotypeService> serviceLoader = ServiceLoader.load(GenotypeService.class);

  public static synchronized GenotypeServiceLoader get() {
    if (loader == null) loader = new GenotypeServiceLoader();
    return loader;
  }

  @Override
  protected ServiceLoader<GenotypeService> getServiceLoader() {
    return serviceLoader;
  }
}
