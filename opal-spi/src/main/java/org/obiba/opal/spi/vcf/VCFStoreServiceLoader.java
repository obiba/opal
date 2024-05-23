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


import org.obiba.plugins.spi.ServicePluginLoader;

import java.net.URLClassLoader;
import java.util.ServiceLoader;

/**
 * {@link VCFStoreService} loader.
 */
public class VCFStoreServiceLoader extends ServicePluginLoader<VCFStoreService> {

  private static VCFStoreServiceLoader loader;

  private final ServiceLoader<VCFStoreService> serviceLoader;

  private VCFStoreServiceLoader(URLClassLoader classLoader) {
    this.serviceLoader = ServiceLoader.load(VCFStoreService.class, classLoader);
  }

  public static synchronized VCFStoreServiceLoader get(URLClassLoader classLoader) {
    if (loader == null) loader = new VCFStoreServiceLoader(classLoader);
    return loader;
  }

  @Override
  protected ServiceLoader<VCFStoreService> getServiceLoader() {
    return serviceLoader;
  }
}
