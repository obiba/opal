package org.obiba.magma.r.rserve;

import org.obiba.magma.r.REngineFactory;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

public class RserveEngineFactory implements REngineFactory {

  private String host = "localhost";

  private int port = 6311;

  public RserveEngineFactory() {

  }

  @Override
  public REngine createEngine() throws REngineException {
    return new RserveEngine(host, port);
  }

}
