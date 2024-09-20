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
import java.io.InputStream;

/**
 * Write a local file into R.
 */
public class FileWriteROperation extends AbstractROperation {

  private final String fileName;

  private final File source;

  private final InputStream input;

  public FileWriteROperation(String fileName, File source) {
    this.fileName = fileName;
    this.source = source;
    this.input = null;
  }

  public FileWriteROperation(String fileName, InputStream input) {
    this.fileName = fileName;
    this.source = null;
    this.input = input;
  }

  @Override
  public void doWithConnection() {
    if (input != null) {
      try {
        writeFile(fileName, input);
      } catch (RServerException e) {
        throw new RRuntimeException(e);
      }
    } else if (source != null) {
      writeFile(fileName, source);
    }
  }

  @Override
  public String toString() {
    return String.format("%s <- %s", fileName, source == null ? "?" : source);
  }
}
