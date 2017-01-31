/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r;

import org.apache.commons.io.IOUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Handles a R connection and provides some utility methods to handle operations on it.
 */
public abstract class AbstractROperation implements ROperation {

  private static final Logger log = LoggerFactory.getLogger(AbstractROperation.class);

  RConnection connection;

  /**
   * Check if connection is still operational.
   *
   * @return
   */
  protected boolean isConnected() {
    return connection.isConnected();
  }

  /**
   * Assign a string value to a symbol in R.
   *
   * @param sym symbol
   * @param ct content
   * @see RConnection#assign(String, String)
   */
  protected void assign(String sym, String ct) {
    try {
      connection.assign(sym, ct);
    } catch(RserveException e) {
      log.warn("Failed assigning '{}' with: {}", sym, ct, e);
      throw new RRuntimeException(e);
    }
  }

  protected void assign(String sym, byte[] ct, boolean serialized) {
    try {
      connection.assign(sym, ct);
      if (serialized) assign(sym, eval(String.format("unserialize(%s)", sym), false));
    } catch (REngineException e) {
      log.warn("Failed assigning '{}' with: byte[{}]", sym, ct == null ? 0 : ct.length, e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Assign a REXP object to a symbol in R.
   *
   * @param sym
   * @param ct
   */
  protected void assign(String sym, REXP ct) {
    try {
      connection.assign(sym, ct);
    } catch(RserveException e) {
      log.warn("Failed assigning '{}' with REXP", sym, e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Safe evaluation of a R script.
   *
   * @param script
   * @return result serialized
   */
  protected REXP eval(String script) {
    return eval(script, true);
  }

  /**
   * Safe evaluation of a R script with result optionally serialized.
   *
   * @param script
   * @param serialize
   * @return
   */
  protected REXP eval(String script, boolean serialize) {
    if(script == null) throw new IllegalArgumentException("R script cannot be null");

    REXP evaled;
    try {
      log.debug("evaluating {}", script);
      String cmd = script;
      if(serialize) {
        cmd = "serialize({" + script + "}, NULL)";
      }
      evaled = connection.eval("try(" + cmd + ")");
    } catch(RserveException e) {
      log.warn("Failed evaluating: {}", script, e);
      throw new RRuntimeException(e);
    }
    if(evaled.inherits("try-error")) {
      // Deal with an error
      throw new REvaluationRuntimeException("Error while evaluating '" + script + "'", evaled);
    }

    return evaled;
  }

  /**
   * Write a file on the R server from a local file.
   *
   * @param fileName R server file name
   * @param in local file
   */
  protected void writeFile(String fileName, File in) {
    try {
      writeFile(fileName, new BufferedInputStream(new FileInputStream(in)));
    } catch (FileNotFoundException e) {
      log.warn("Failed creating file '{}' from file {}", fileName, in.getName(), e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Write a file on the R server from a input stream.
   *
   * @param fileName R server file name
   * @param in local stream
   */
  protected void writeFile(String fileName, InputStream in) {
    byte [] b = new byte[8192];
    try (RFileOutputStream out = connection.createFile(fileName);) {
      IOUtils.copy(in, out);
      in.close();
    } catch (IOException e) {
      log.warn("Failed creating file '{}'", fileName, e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Read a file on the R server into a local file.
   *
   * @param fileName R server file name
   * @param out local file
   */
  protected void readFile(String fileName, File out) {
    try {
      if (!out.getParentFile().exists()) out.getParentFile().mkdirs();
      readFile(fileName, new BufferedOutputStream(new FileOutputStream(out)));
    } catch (FileNotFoundException e) {
      log.warn("Failed creating file '{}' from '{}'", out, fileName, e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Read a file on the R server into a output stream.
   *
   * @param fileName R server file name
   * @param out local stream
   */
  protected void readFile(String fileName, OutputStream out) {
    try (RFileInputStream in = connection.openFile(fileName)) {
      IOUtils.copy(in, out);
      out.close();
    } catch( IOException e){
      log.warn("Failed reading file '{}'", fileName, e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Ensure that a R package is installed: check it is available, if not install it.
   *
   * @param packageName
   * @return
   */
  protected REXP ensurePackage(String packageName) {
    String cmd = String.format("if (!require(%s)) { install.packages('%s', repos=c('http://cran.rstudio.com/', 'http://cran.obiba.org'), dependencies=TRUE) }",
        packageName, packageName);
    return eval(cmd, false);
  }

  /**
   * Get the current R connection.
   *
   * @return
   */
  protected RConnection getConnection() {
    return connection;
  }

  /**
   * Set the R connection to make it available for operations.
   */
  @Override
  public void doWithConnection(RConnection connection) {
    if(connection == null) throw new IllegalArgumentException("R connection cannot be null");
    this.connection = connection;
    doWithConnection();
  }

  /**
   * Does anything with the current R connection.
   */
  protected abstract void doWithConnection();

}
