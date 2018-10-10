package org.obiba.opal.spi.r.datasource;

import org.obiba.opal.spi.r.ROperationTemplate;

/**
 * Wrapper of a R session to facilitate handling.
 */
public interface RSessionHandler {

  /**
   * Get the R session for executing operations.
   *
   */
  ROperationTemplate getSession();

  /**
   * To be called on datasource dispose in order to clean up the R session.
   */
  void onDispose();

}
