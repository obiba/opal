/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.service.datashield;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.obiba.magma.r.REngineFactory;
import org.obiba.magma.r.rserve.RserveEngineFactory;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.MapMaker;

@Component
public class DatashieldService {

  private static final Logger log = LoggerFactory.getLogger(DatashieldService.class);

  private final REngineFactory engineFactory = new RserveEngineFactory();

  private REngine engine;

  private Map<UUID, REXP> sessions = new MapMaker().expiration(30, TimeUnit.MINUTES).makeMap();

  public DatashieldService() {
  }

  public String createSession() {
    try {
      UUID session = UUID.randomUUID();
      sessions.put(session, engine.newEnvironment(null, false));
      return session.toString();
    } catch(REngineException e) {
      throw new RuntimeException(e);
    } catch(REXPMismatchException e) {
      throw new RuntimeException(e);
    }
  }

  public RTemplate newTemplate(String sessionId) {
    return new RTemplate(sessions.get(UUID.fromString(sessionId)));
  }

  @PostConstruct
  public void start() {
    try {
      engine = engineFactory.createEngine();
    } catch(REngineException e) {
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  public void stop() {
    engine.close();
  }

  public class RTemplate {

    private final REXP environment;

    public RTemplate(REXP environment) {
      this.environment = environment;
    }

    public REXP eval(REXP eval) {
      try {
        return engine.eval(eval, environment, false);
      } catch(REngineException e) {
        throw new RuntimeException(e);
      } catch(REXPMismatchException e) {
        throw new RuntimeException(e);
      }
    }

    public REXP parseAndEval(String command) {
      try {
        log.info("Evaluating '{}'", command);
        return engine.parseAndEval(command, environment, false);
      } catch(REngineException e) {
        throw new RuntimeException(e);
      } catch(REXPMismatchException e) {
        throw new RuntimeException(e);
      }
    }

    public void assign(String symbol, REXP value) {
      try {
        log.info("Assigning '{}={}'", symbol, value);
        engine.assign(symbol, value, environment);
      } catch(REngineException e) {
        throw new RuntimeException(e);
      } catch(REXPMismatchException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
