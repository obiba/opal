/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.datasource;

import org.obiba.plugins.spi.ServicePluginLoader;

import java.net.URLClassLoader;
import java.util.ServiceLoader;

/**
 * {@link DatasourceService} loader.
 */
public class DatasourceServiceLoader extends ServicePluginLoader<DatasourceService> {

  private static DatasourceServiceLoader loader;

  private final ServiceLoader<DatasourceService> serviceLoader;

  private DatasourceServiceLoader(URLClassLoader classLoader) {
    this.serviceLoader = ServiceLoader.load(DatasourceService.class, classLoader);
  }

  public static synchronized DatasourceServiceLoader get(URLClassLoader classLoader) {
    if (loader == null) loader = new DatasourceServiceLoader(classLoader);
    return loader;
  }

  @Override
  protected ServiceLoader<DatasourceService> getServiceLoader() {
    return serviceLoader;
  }
}
