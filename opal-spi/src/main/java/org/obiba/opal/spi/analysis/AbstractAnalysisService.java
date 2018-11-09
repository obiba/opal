package org.obiba.opal.spi.analysis;

import java.io.File;

public abstract class AbstractAnalysisService<T extends Analysis, U extends AnalysisResult> implements AnalysisService<T, U> {

  private AnalysisService.OpalFileSystemPathResolver pathResolver;

  protected File resolvePath(String virtualPath) {
    return pathResolver == null ? new File(virtualPath) : pathResolver.resolve(virtualPath);
  }

  /**
   * Sets an instance of an Opal file system path resolver.
   *
   * @param resolver
   */
  public void setOpalFileSystemPathResolver(AnalysisService.OpalFileSystemPathResolver resolver) {
    this.pathResolver = resolver;
  }
}
