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

import java.io.File;
import java.io.OutputStream;

/**
 * Read a file from R into a local file.
 */
public class FileReadROperation extends AbstractROperation {

  private final String fileName;

  private final File destination;

  private final OutputStream output;

  public FileReadROperation(String fileName, File destination) {
    this.fileName = fileName;
    this.destination = destination;
    this.output = null;
  }

  public FileReadROperation(String fileName, OutputStream output) {
    this.fileName = fileName;
    this.destination = null;
    this.output = output;
  }

  @Override
  public void doWithConnection() {
    try {
      if (output != null) {
        readFile(fileName, output);
      } else if (destination != null) {
        readFile(fileName, destination);
      }
    } catch (RServerException e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("%s -> %s", fileName, destination == null ? "?" : destination);
  }
}
