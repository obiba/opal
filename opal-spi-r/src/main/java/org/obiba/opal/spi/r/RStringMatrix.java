/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class RStringMatrix implements RMatrix<String> {

  private static final Logger log = LoggerFactory.getLogger(RStringMatrix.class);

  private String[] rowNames = new String[]{};

  private String[] columnNames = new String[]{};

  private String[] values = new String[]{};

  public RStringMatrix(REXP matrix) throws REXPMismatchException {
    if (matrix != null) {
      REXP dims = matrix.getAttribute("dimnames");
      if (dims != null) {
        try {
          RList dimnames = dims.asList();
          rowNames = dimnames.at(0).isNull() ? new String[]{""} : dimnames.at(0).asStrings();
          columnNames = dimnames.at(1).isNull() ? new String[]{""} : dimnames.at(1).asStrings();
          values = matrix.asStrings();
        } catch (REXPMismatchException e) {
          log.error("Failed at extracting strings from matrix", e);
        }
      }
    }
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String[] getColumnNames() {
    return columnNames;
  }

  @Override
  public String getColumnName(int idx) {
    return columnNames[idx];
  }

  @Override
  public int getColumnIndex(String name) {
    for(int i = 0; i < columnNames.length; i++) {
      if(columnNames[i].equals(name)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int getRowCount() {
    return rowNames.length;
  }

  @Override
  public String[] getRowNames() {
    return rowNames;
  }

  @Override
  public String getRowName(int idx) {
    return rowNames[idx];
  }

  @Override
  public int getRowIndex(String name) {
    for(int i = 0; i < rowNames.length; i++) {
      if(rowNames[i].equals(name)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public Iterable<String[]> iterateRows() {
    return RowsIterator::new;
  }

  @Override
  public Iterable<String[]> iterateColumns() {
    return ColumnsIterator::new;
  }

  private class RowsIterator implements Iterator<String[]> {

    private int current = 0;

    @Override
    public boolean hasNext() {
      return current < getRowCount();
    }

    @Override
    public String[] next() {
      String[] row = new String[getColumnCount()];
      for(int i = 0; i < getColumnCount(); i++) {
        row[i] = values[current + i * getRowCount()];
      }
      current++;
      return row;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private class ColumnsIterator implements Iterator<String[]> {

    private int current = 0;

    @Override
    public boolean hasNext() {
      return current < getColumnCount();
    }

    @Override
    public String[] next() {
      String[] col = new String[getRowCount()];
      for(int i = 0; i < getRowCount(); i++) {
        col[i] = values[current * getColumnCount() + i];
      }
      current++;
      return col;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
