package org.obiba.opal.spi.r.datasource;

import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.spi.r.datasource.magma.RSymbolWriter;

public interface RDatasourceFactory extends DatasourceFactory {

  /**
   * Set the accessor to the R session for executing operations.
   *
   * @param sessionHandler
   */
  void setRSessionHandler(RSessionHandler sessionHandler);

  /**
   * Output parameters to write a tibble symbol into another format.
   *
   * @return
   */
  RSymbolWriter createSymbolWriter();

}
