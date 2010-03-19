package org.obiba.magma.r.jri;

import org.obiba.magma.r.REngineFactory;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.REngineStdOutput;
import org.rosuda.REngine.JRI.JRIEngine;

/**
 * An implementation of {@code REngineFactory} that uses JRI to create the R environment. Note that this requires the
 * JRI native library present in java.library.path.
 */
public class JRIEngineFactory implements REngineFactory {

  @Override
  public REngine createEngine() throws REngineException {
    return new JRIEngine(new String[] {}, new REngineStdOutput(), false);
  }

}
