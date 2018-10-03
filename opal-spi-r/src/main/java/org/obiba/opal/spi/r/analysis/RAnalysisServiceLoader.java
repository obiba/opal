package org.obiba.opal.spi.r.analysis;

import org.obiba.plugins.spi.ServicePluginLoader;

import java.util.ServiceLoader;

public class RAnalysisServiceLoader extends ServicePluginLoader<RAnalysisService> {

  private static RAnalysisServiceLoader loader;

  private ServiceLoader<RAnalysisService> serviceLoader = ServiceLoader.load(RAnalysisService.class);

  public static synchronized RAnalysisServiceLoader get() {
    if (loader == null) loader = new RAnalysisServiceLoader();
    return loader;
  }

  @Override
  protected ServiceLoader<RAnalysisService> getServiceLoader() {
    return serviceLoader;
  }
}
