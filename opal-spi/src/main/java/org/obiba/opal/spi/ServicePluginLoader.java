/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi;

import java.util.*;

/**
 * {@link ServicePlugin} loader.
 */
public abstract class ServicePluginLoader<T extends ServicePlugin> {

  private Map<String, T> services = new HashMap<>();

  protected abstract ServiceLoader<T> getServiceLoader();

  private synchronized void init() {
    services.clear();
    getServiceLoader().reload();
    getServiceLoader().iterator().forEachRemaining(c -> services.put(c.getName(), c));
  }

  public void reload() {
    init();
  }

  public Collection<T> getServices() {
    if (services.isEmpty()) init();
    return services.values();
  }

  public boolean hasService(String name) {
    if (services.isEmpty()) init();
    return services.containsKey(name);
  }

  public T getService(String name) {
    if (!hasService(name)) throw new NoSuchElementException("No such service plugin: " + name);
    return services.get(name);
  }
}
