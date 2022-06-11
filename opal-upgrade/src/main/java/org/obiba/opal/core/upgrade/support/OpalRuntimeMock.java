/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.support;

import com.google.common.collect.Lists;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.plugins.spi.ServicePlugin;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Upgrade purpose only to fix injection dependencies.
 */
public class OpalRuntimeMock implements OpalRuntime {

  @Override
  public Set<Service> getServices() {
    return null;
  }

  @Override
  public boolean hasService(String name) {
    return false;
  }

  @Override
  public Service getService(String name) throws NoSuchServiceException {
    throw new NoSuchServiceException(name);
  }

  @Override
  public boolean hasServicePlugins() {
    return false;
  }

  @Override
  public boolean hasServicePlugins(Class clazz) {
    return false;
  }

  @Override
  public Collection<ServicePlugin> getServicePlugins(Class clazz) {
    return Lists.newArrayList();
  }

  @Override
  public ServicePlugin getServicePlugin(Class clazz) {
    throw new NoSuchElementException(clazz.getName());
  }

  @Override
  public boolean hasServicePlugin(String name) {
    return false;
  }

  @Override
  public ServicePlugin getServicePlugin(String name) {
    throw new NoSuchElementException(name);
  }

  @Override
  public Collection<ServicePlugin> getServicePlugins() {
    return Lists.newArrayList();
  }

  @Override
  public Collection<App> getApps() {
    return Lists.newArrayList();
  }

  @Override
  public App getApp(String name) {
    throw new NoSuchElementException(name);
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }
}
