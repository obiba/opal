package org.obiba.opal.spi.r.datasource;

import org.obiba.opal.spi.datasource.DatasourceService;

public interface RDatasourceService extends DatasourceService {

  /**
   * Set the accessor to the R session for executing operations.
   *
   * @param sessionHandler
   */
  void setRSessionHandler(RSessionHandler sessionHandler);

}
