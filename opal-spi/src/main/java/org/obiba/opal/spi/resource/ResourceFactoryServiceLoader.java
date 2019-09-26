/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource;

import org.obiba.plugins.spi.ServicePluginLoader;

import java.util.ServiceLoader;

public class ResourceFactoryServiceLoader extends ServicePluginLoader<ResourceFactoryService> {

  private static ResourceFactoryServiceLoader loader;

  private ServiceLoader<ResourceFactoryService> serviceLoader = ServiceLoader.load(ResourceFactoryService.class);

  public static synchronized ResourceFactoryServiceLoader get() {
    if (loader == null) loader = new ResourceFactoryServiceLoader();
    return loader;
  }

  @Override
  protected ServiceLoader<ResourceFactoryService> getServiceLoader() {
    return serviceLoader;
  }
}
