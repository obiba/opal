/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Handles a R connection and provides some utility methods to handle operations on it.
 */
public abstract class AbstractROperation implements ROperation {

  private static final Logger log = LoggerFactory.getLogger(AbstractROperation.class);

  RServerConnection connection;

  private List<String> repositories = Lists.newArrayList("https://cloud.r-project.org", "https://cran.obiba.org");

  /**
   * Assign a string value to a symbol in R.
   *
   * @param sym symbol
   * @param ct  content
   */
  protected void assignScript(String sym, String ct) {
    try {
      connection.assignScript(sym, ct);
    } catch (RServerException e) {
      if (log.isDebugEnabled())
        log.warn("Failed assigning '{}' with: {}", sym, ct, e);
      else
        log.warn("Failed assigning '{}' with: {}", sym, ct);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Assign raw data to a symbol in R.
   *
   * @param sym symbol
   * @param bct base64 encoded content
   */
  protected void assignData(String sym, String bct) {
    try {
      connection.assignData(sym, bct);
    } catch (RServerException e) {
      if (log.isDebugEnabled())
        log.warn("Failed assigning '{}' with raw data", sym, e);
      else
        log.warn("Failed assigning '{}' with raw data", sym);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Safe evaluation of a R script.
   *
   * @param script
   * @return result serialized
   */
  protected RServerResult eval(String script) {
    return eval(script, RSerialize.RAW);
  }

  /**
   * Safe evaluation of a R script with result serialized by R or not.
   *
   * @param script
   * @param serialize
   * @return
   */
  protected RServerResult eval(String script, boolean serialize) {
    return eval(script, serialize ? RSerialize.RAW : RSerialize.NATIVE);
  }

  /**
   * Safe evaluation of a R script with result optionally serialized.
   *
   * @param script
   * @param serialize
   * @return
   */
  protected RServerResult eval(String script, RSerialize serialize) {
    if (script == null) throw new IllegalArgumentException("R script cannot be null");
    try {
      log.debug("evaluating: {}", script);
      return connection.eval(script, serialize);
    } catch (RServerException e) {
      if (log.isDebugEnabled())
        log.warn("Failed evaluating: {}", script, e);
      else
        log.warn("Failed evaluating: {}", script);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Write a file on the R server from a local file.
   *
   * @param fileName R server file name
   * @param in       local file
   */
  protected void writeFile(String fileName, File in) {
    try {
      writeFile(fileName, new BufferedInputStream(new FileInputStream(in)));
    } catch (FileNotFoundException | RServerException e) {
      if (log.isDebugEnabled())
        log.warn("Failed creating file '{}' from file {}", fileName, in.getName(), e);
      else
        log.warn("Failed creating file '{}' from file {}", fileName, in.getName());
      throw new RRuntimeException(e);
    }
  }

  /**
   * Write a file on the R server from a input stream.
   *
   * @param fileName R server file name
   * @param in       local stream
   */
  protected void writeFile(String fileName, InputStream in)  throws RServerException {
    connection.writeFile(fileName, in);
  }

  /**
   * Read a file on the R server into a local file.
   *
   * @param fileName R server file name
   * @param out      local file
   */
  protected void readFile(String fileName, File out) throws RServerException {
    try {
      if (!out.getParentFile().exists()) out.getParentFile().mkdirs();
      readFile(fileName, new BufferedOutputStream(new FileOutputStream(out)));
    } catch (FileNotFoundException e) {
      if (log.isDebugEnabled())
        log.warn("Failed creating file '{}' from '{}'", out, fileName, e);
      else
        log.warn("Failed creating file '{}' from '{}'", out, fileName);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Read a file on the R server into a output stream.
   *
   * @param fileName R server file name
   * @param out      local stream
   */
  protected void readFile(String fileName, OutputStream out) {
    try {
      connection.readFile(fileName, out);
    } catch (RServerException e) {
      throw new RRuntimeException(e);
    }
  }

  /**
   * Load a R package.
   *
   * @param packageName
   * @return
   */
  protected void loadPackage(String packageName) {
    String cmd = String.format("library('%s')", packageName);
    eval(cmd, RSerialize.NATIVE);
  }

  /**
   * Ensure that a R package is installed: check it is available, if not install it.
   *
   * @param packageName
   * @return
   */
  protected void ensurePackage(String packageName) {
    String repos = Joiner.on("','").join(getRepositories());
    String cmd = String.format("if (!require(%s)) { install.packages('%s', repos=c('%s'), dependencies=TRUE) }",
        packageName, packageName, repos);
    eval(cmd, RSerialize.NATIVE);
  }

  /**
   * Ensure R package is installed: check it is available, if not install it from Github repository.
   *
   * @param user
   * @param packageName
   * @param reference   if null, master is used
   * @return
   */
  protected void ensureGitHubPackage(String user, String packageName, String reference) {
    ensurePackage("remotes");
    String cmd = String.format("if (!require(%s)) { remotes::install_github('%s/%s', ref='%s', dependencies=TRUE, upgrade=TRUE) }",
        packageName, user, packageName, Strings.isNullOrEmpty(reference) ? "master" : reference);
    eval(cmd, RSerialize.NATIVE);
  }

  /**
   * Get the current R connection.
   *
   * @return
   */
  protected RServerConnection getConnection() {
    return connection;
  }

  /**
   * Set the R connection to make it available for operations.
   */
  @Override
  public void doWithConnection(RServerConnection connection) {
    if (connection == null) throw new IllegalArgumentException("R connection cannot be null");
    this.connection = connection;
    doWithConnection();
  }

  /**
   * Does anything with the current R connection.
   */
  protected abstract void doWithConnection();

  /**
   * Get the CRAN repositories for installing packages.
   *
   * @return
   */
  private List<String> getRepositories() {
    return repositories;
  }
}
