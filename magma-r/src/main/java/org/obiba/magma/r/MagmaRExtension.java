package org.obiba.magma.r;

import org.obiba.magma.Disposable;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.r.rserve.RserveEngineFactory;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

/**
 */
public class MagmaRExtension implements MagmaEngineExtension, Disposable {

  private REngineFactory engineFactory = new RserveEngineFactory();

  private REngine engine;

  public MagmaRExtension() {

  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public void initialise() {
    if(engineFactory == null) throw new MagmaRuntimeException("No REngineFactory specified. Cannot initialise the R extension.");
    try {
      REngine engine = engineFactory.createEngine();
      this.engine = engine;
    } catch(REngineException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public void dispose() {
    engine.close();
  }

  public REXP newEnvironment() {
    try {
      return engine.newEnvironment(null, false);
    } catch(REngineException e) {
      throw new MagmaRuntimeException(e);
    } catch(REXPMismatchException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  public REngine getEngine() {
    return engine;
  }

}
