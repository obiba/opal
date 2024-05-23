/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.search;


import org.obiba.plugins.spi.ServicePluginLoader;

import java.net.URLClassLoader;
import java.util.ServiceLoader;

/**
 * {@link SearchService} loader.
 */
public class SearchServiceLoader extends ServicePluginLoader<SearchService> {

  private static SearchServiceLoader loader;

  private final ServiceLoader<SearchService> serviceLoader;

  private SearchServiceLoader(URLClassLoader classLoader) {
    this.serviceLoader = ServiceLoader.load(SearchService.class, classLoader);
  }

  public static synchronized SearchServiceLoader get(URLClassLoader classLoader) {
    if (loader == null) loader = new SearchServiceLoader(classLoader);
    return loader;
  }

  @Override
  protected ServiceLoader<SearchService> getServiceLoader() {
    return serviceLoader;
  }
}
