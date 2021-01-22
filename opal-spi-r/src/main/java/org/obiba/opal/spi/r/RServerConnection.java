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

import java.io.InputStream;
import java.io.OutputStream;

public interface RServerConnection {

  /**
   * Check if connection is alive.
   *
   * @return
   */
  boolean isConnected();

  /**
   * Assign raw data to a symbol.
   *
   * @param symbol
   * @param content
   * @throws RServerException
   */
  void assign(String symbol, byte[] content) throws RServerException;

  /**
   * Assign string to a symbol.
   *
   * @param symbol
   * @param content
   * @throws RServerException
   */
  void assign(String symbol, String content) throws RServerException;

  /**
   * Evaluate an expression and return the result object.
   *
   * @param expr
   * @return
   * @throws RServerException
   */
  RServerResult eval(String expr) throws RServerException;

  /**
   * Write a file from the input stream.
   *
   * @param fileName
   * @param in
   */
  void writeFile(String fileName, InputStream in);

  /**
   * Read a file in the output stream.
   *
   * @param fileName
   * @param out
   */
  void readFile(String fileName, OutputStream out);

  /**
   * Get last operation error.
   *
   * @return
   */
  String getLastError();

  /**
   * Close the connection.
   */
  void close();

}
