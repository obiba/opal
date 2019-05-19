/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import java.io.File;

/**
 * Read a file from R into a local file.
 */
public class FileReadROperation extends AbstractROperation {

  private final String fileName;

  private final File destination;

  public FileReadROperation(String fileName, File destination) {
    this.fileName = fileName;
    this.destination = destination;
  }

  @Override
  public void doWithConnection() {
    readFile(fileName, destination);
  }

  @Override
  public String toString() {
    return String.format("%s -> %s", fileName, destination);
  }
}
