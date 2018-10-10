package org.obiba.opal.spi.r.datasource;

import org.obiba.opal.spi.datasource.AbstractDatasourceService;

public abstract class AbstractRDatasourceService extends AbstractDatasourceService implements RDatasourceService {

  private RSessionHandler sessionHandler;

  @Override
  public void setRSessionHandler(RSessionHandler sessionHandler) {
    this.sessionHandler = sessionHandler;
  }

  protected RSessionHandler getRSessionHandler() {
    return sessionHandler;
  }

}
