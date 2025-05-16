package org.obiba.opal.r.kubernetes;

import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.service.RServerService;

public interface RServerPodService extends RServerService {

  /**
   * Get the inner App from which the R server was built.
   *
   * @return
   */
  PodSpec getPodSpec();
}
