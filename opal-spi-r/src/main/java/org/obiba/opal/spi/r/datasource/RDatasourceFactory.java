package org.obiba.opal.spi.r.datasource;

import org.obiba.magma.DatasourceFactory;

public interface RDatasourceFactory extends DatasourceFactory {

  /**
   * Set the accessor to the R session for executing operations.
   *
   * @param sessionHandler
   */
  void setRSessionHandler(RSessionHandler sessionHandler);

}
