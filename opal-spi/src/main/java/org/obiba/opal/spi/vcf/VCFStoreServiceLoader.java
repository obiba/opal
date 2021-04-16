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

import java.util.ServiceLoader;

/**
 * {@link VCFStoreService} loader.
 */
public class VCFStoreServiceLoader extends ServicePluginLoader<VCFStoreService> {

  private static VCFStoreServiceLoader loader;

  private ServiceLoader<VCFStoreService> serviceLoader = ServiceLoader.load(VCFStoreService.class);

  public static synchronized VCFStoreServiceLoader get() {
    if (loader == null) loader = new VCFStoreServiceLoader();
    return loader;
  }

  @Override
  protected ServiceLoader<VCFStoreService> getServiceLoader() {
    return serviceLoader;
  }
}
