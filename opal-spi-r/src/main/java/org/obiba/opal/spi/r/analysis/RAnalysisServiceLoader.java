/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.analysis;

import org.obiba.plugins.spi.ServicePluginLoader;

import java.net.URLClassLoader;
import java.util.ServiceLoader;

public class RAnalysisServiceLoader extends ServicePluginLoader<RAnalysisService> {

  private static RAnalysisServiceLoader loader;

  private final ServiceLoader<RAnalysisService> serviceLoader;

  public RAnalysisServiceLoader(URLClassLoader classLoader) {
    this.serviceLoader = ServiceLoader.load(RAnalysisService.class, classLoader);;
  }

  public static synchronized RAnalysisServiceLoader get(URLClassLoader classLoader) {
    if (loader == null) loader = new RAnalysisServiceLoader(classLoader);
    return loader;
  }

  @Override
  protected ServiceLoader<RAnalysisService> getServiceLoader() {
    return serviceLoader;
  }
}
