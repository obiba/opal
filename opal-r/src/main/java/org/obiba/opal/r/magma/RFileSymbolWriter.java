/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import org.obiba.magma.ValueTable;
import org.obiba.opal.r.DataSaveROperation;
import org.obiba.opal.spi.r.FolderReadROperation;
import org.obiba.opal.spi.r.RUtils;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.obiba.opal.spi.r.datasource.magma.RSymbolWriter;

import java.io.File;
import java.util.List;

/**
 * Write a tibble represented by a symbol using haven R package.
 */
public class RFileSymbolWriter implements RSymbolWriter {

  private final RSessionHandler rSessionHandler;

  private final List<File> outputFiles;

  public RFileSymbolWriter(RSessionHandler rSessionHandler, List<File> outputFiles) {
    this.rSessionHandler = rSessionHandler;
    this.outputFiles = outputFiles;
  }

  @Override
  public String getSymbol(ValueTable table) {
    return RUtils.getSymbol(getOutputFile(table));
  }

  @Override
  public void write(ValueTable table) {
    File destination = getOutputFile(table);
    // save tibble in file in R
    rSessionHandler.getSession().execute(new DataSaveROperation(getSymbol(table), destination.getName()));
    // read back file from R to opal
    rSessionHandler.getSession().execute(new FolderReadROperation(destination.getParentFile()));
  }

  @Override
  public void dispose() {
    // no-op
  }

  private File getOutputFile(ValueTable table) {
    if (outputFiles.size() == 1)
      return outputFiles.get(0);
    else
      return outputFiles.stream().filter(f -> f.getName().startsWith(table.getName() + ".")).findFirst().get();
  }
}
