package org.obiba.opal.core.upgrade.support;

import java.util.Set;

import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.fs.OpalFileSystem;

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
  public void start() {

  }

  @Override
  public void stop() {

  }
}
