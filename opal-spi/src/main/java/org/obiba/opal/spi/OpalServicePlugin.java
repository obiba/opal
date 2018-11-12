package org.obiba.opal.spi;

import java.io.File;
import org.obiba.plugins.spi.ServicePlugin;

public interface OpalServicePlugin extends ServicePlugin {

  /**
   * Resolve a Opal file system path to a local file.
   */
  interface OpalFileSystemPathResolver {
    File resolve(String path);
  }

  /**
   * Sets an instance of an Opal file system path resolver.
   *
   * @param resolver
   */
  void setOpalFileSystemPathResolver(OpalFileSystemPathResolver resolver);

}
