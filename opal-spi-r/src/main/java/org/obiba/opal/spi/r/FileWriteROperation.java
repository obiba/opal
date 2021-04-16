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

/**
 * Write a local file into R.
 */
public class FileWriteROperation extends AbstractROperation {

  private final String fileName;

  private final File source;

  public FileWriteROperation(String fileName, File source) {
    this.fileName = fileName;
    this.source = source;
  }

  @Override
  public void doWithConnection() {
    writeFile(fileName, source);
  }

  @Override
  public String toString() {
    return String.format("%s <- %s", fileName, source.getAbsolutePath());
  }
}
