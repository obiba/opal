package org.obiba.magma.r.rserve;

import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPEnvironment;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.google.common.collect.Sets;

/**
 * A {@code REngine} implementation on top of {@code RConnection} (Rserve) that adds environments.
 * <p>
 * Callers must obtain an environment to use this {@code REngine}.
 * <p>
 * This implementation uses one {@code RConnection} per created environment. Thus, it is important to release the
 * environment to release this connection.
 */
public class RserveEngine extends REngine {

  private Set<Environment> envs = Sets.newHashSet();

  public RserveEngine() throws REngineException {
    new RConnection().close();
  }

  public RserveEngine(String host) throws REngineException {
    new RConnection(host).close();
  }

  public RserveEngine(String host, int port) throws REngineException {
    new RConnection(host, port).close();
  }

  @Override
  public void assign(String symbol, REXP value, REXP env) throws REngineException, REXPMismatchException {
    RConnection connection = getConnection(env);
    connection.assign(symbol, value);
  }

  @Override
  public REXP createReference(REXP value) throws REngineException, REXPMismatchException {
    throw new UnsupportedOperationException("environment is required");
  }

  @Override
  public REXP parseAndEval(String text, REXP where, boolean resolve) throws REngineException, REXPMismatchException {
    return getConnection(where).parseAndEval(text);
  }

  @Override
  public REXP eval(REXP what, REXP where, boolean resolve) throws REngineException, REXPMismatchException {
    throw new UnsupportedOperationException("RServe does not have a separate parse step");
  }

  @Override
  public void finalizeReference(REXP ref) throws REngineException, REXPMismatchException {
    if(ref.isEnvironment()) {
      closeEnvironment(ref);
    }
    throw new UnsupportedOperationException("finalizeReference");
  }

  @Override
  public REXP get(String symbol, REXP env, boolean resolve) throws REngineException, REXPMismatchException {
    return getConnection(env).get(symbol, null, false);
  }

  @Override
  public REXP getParentEnvironment(REXP env, boolean resolve) throws REngineException, REXPMismatchException {
    throw new UnsupportedOperationException("getParentEnvironment");
  }

  @Override
  public REXP newEnvironment(REXP parent, boolean resolve) throws REngineException, REXPMismatchException {
    return new REXPEnvironment(this, new Environment());
  }

  @Override
  public REXP parse(String text, boolean resolve) throws REngineException {
    throw new UnsupportedOperationException("RServe does not have a separate parse step");
  }

  @Override
  public REXP resolveReference(REXP ref) throws REngineException, REXPMismatchException {
    throw new UnsupportedOperationException("getParentEnvironment");
  }

  @Override
  public boolean close() {
    for(Environment env : this.envs) {
      env.connection.close();
    }
    envs.clear();
    return true;
  }

  private void closeEnvironment(REXP ref) {
    Environment env = ((Environment) ((REXPEnvironment) ref).getHandle());
    this.envs.remove(env);
    env.connection.close();
  }

  private RConnection getConnection(REXP env) {
    return ((Environment) ((REXPEnvironment) env).getHandle()).connection;
  }

  private class Environment {

    final RConnection connection;

    Environment() throws RserveException {
      connection = new RConnection();
      envs.add(this);
    }
  }

}
