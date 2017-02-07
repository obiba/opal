/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.support;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.spi.genotype.GenotypeStoreService;

/**
 * Upgrade purpose only to fix injection dependencies.
 */
public class OpalRuntimeMock implements OpalRuntime {

  @Override
  public Set<Service> getServices() {
    return null;
  }

  @Override
  public boolean hasFileSystem() {
    return false;
  }

  @Override
  public OpalFileSystem getFileSystem() {
    return null;
  }

  @Override
  public boolean hasService(String name) {
    return false;
  }

  @Override
  public Service getService(String name) throws NoSuchServiceException {
    return null;
  }

  @Override
  public boolean hasGenotypeStoreService(String name) {
    return false;
  }

  @Override
  public GenotypeStoreService getGenotypeStoreService(String name) {
    throw new NoSuchElementException(name);
  }

  @Override
  public Collection<GenotypeStoreService> getGenotypeStoreServices() {
    return Lists.newArrayList();
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }
}
