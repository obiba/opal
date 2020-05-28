/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.search;


import org.obiba.plugins.spi.ServicePluginLoader;

import java.util.ServiceLoader;

/**
 * {@link SearchService} loader.
 */
public class SearchServiceLoader extends ServicePluginLoader<SearchService> {

  private static SearchServiceLoader loader;

  private ServiceLoader<SearchService> serviceLoader = ServiceLoader.load(SearchService.class);

  public static synchronized SearchServiceLoader get() {
    if (loader == null) loader = new SearchServiceLoader();
    return loader;
  }

  @Override
  protected ServiceLoader<SearchService> getServiceLoader() {
    return serviceLoader;
  }
}
