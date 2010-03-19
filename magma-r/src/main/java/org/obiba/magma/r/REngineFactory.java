package org.obiba.magma.r;

import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

public interface REngineFactory {
  public REngine createEngine() throws REngineException;
}
