package org.obiba.opal.r.rock;

import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.service.RServerService;

public interface RServerAppService extends RServerService {

  /**
   * Get the inner App from which the R server was built.
   *
   * @return
   */
  App getApp();

}
