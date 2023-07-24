/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rserve;

import org.apache.commons.io.IOUtils;
import org.obiba.opal.spi.r.*;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPRaw;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RSession;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Direct connection with Rserve, through its java API.
 */
class RserveConnection implements RServerConnection {

  private static final Logger log = LoggerFactory.getLogger(RserveConnection.class);

  private static final int DEFAULT_BUFFER_SIZE = 81920;

  private final RConnection connection;

  public RserveConnection(RConnection connection) {
    this.connection = connection;
  }

  @Override
  public String getLastError() {
    return connection.getLastError();
  }

  @Override
  public void assignData(String symbol, String content) throws RServerException {
    try {
      byte[] rawContent = Base64.getDecoder().decode(content.replaceAll("\\n", "").replaceAll("\\r", ""));
      connection.assign(symbol, new REXPRaw(rawContent));
      connection.eval(String.format("is.null(base::assign('%s', value=unserialize(%s)))", symbol, symbol));
    } catch (RserveException e) {
      throw new RserveServerException(e);
    }
  }

  @Override
  public void assignScript(String symbol, String content) throws RServerException {
    try {
      connection.assign(symbol, content);
    } catch (RserveException e) {
      throw new RserveServerException(e);
    }
  }

  @Override
  public RServerResult eval(String expr, RSerialize serialize) throws RServerException {
    String cmd;
    if (RSerialize.RAW == serialize) {
      cmd = String.format("try(serialize({%s}, NULL))", expr);
    } else if (RSerialize.JSON == serialize) {
      cmd = String.format("try(tryCatch(jsonlite::toJSON(%s, auto_unbox = T, digits = NA), error = function(e) { jsonlite::serializeJSON(%s) }))", expr, expr);
    } else {
      cmd = String.format("try(%s)", expr);
    }
    return new RserveResult(evalREXP(cmd));
  }

  @Override
  public void writeFile(String fileName, InputStream in) {
    try (OutputStream out = new BufferedOutputStream(connection.createFile(fileName));) {
      IOUtils.copy(in, out);
      in.close();
    } catch (IOException e) {
      log.warn("Failed creating file '{}'", fileName, e);
      throw new RRuntimeException(e);
    }
  }

  @Override
  public void readFile(String fileName, OutputStream out) {
    try (InputStream in = new BufferedInputStream(connection.openFile(fileName), DEFAULT_BUFFER_SIZE)) {
      IOUtils.copy(in, out);
      out.close();
    } catch (IOException e) {
      log.warn("Failed reading file '{}'", fileName, e);
      throw new RRuntimeException(e);
    }
  }

  //
  // Package methods
  //

  void close() {
    connection.close();
  }

  //
  // Private methods
  //

  RSession detach() throws RserveException {
    return connection.detach();
  }

  private REXP evalREXP(String expr) throws RServerException {
    try {
      REXP rexp = connection.eval(expr);
      if (rexp.inherits("try-error")) {
        // Deal with an error
        throw new RserveClientException("Error while evaluating '" + expr + "'", getRMessages(rexp));
      }
      return rexp;
    } catch (RserveException e) {
      throw new RserveServerException(e);
    }
  }

  private List<String> getRMessages(REXP result) {
    String[] strs = null;
    try {
      if (result != null) strs = result.asStrings();
    } catch (REXPMismatchException e) {
      log.error("Not a REXP with strings", e);
    }
    if (strs == null) strs = new String[]{};

    return Arrays.asList(strs);
  }
}
