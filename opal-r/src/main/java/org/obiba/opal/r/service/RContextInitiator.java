package org.obiba.opal.r.service;

import org.obiba.opal.spi.r.RServerException;

/**
 * Apply first R operations once the remote R session is up and running.
 */
public interface RContextInitiator {
  void initiate(RServerSession session) throws RServerException;
}
