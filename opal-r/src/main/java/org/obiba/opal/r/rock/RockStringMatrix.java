/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import org.obiba.opal.spi.r.RMatrix;

import java.util.List;
import java.util.stream.Collectors;

class RockStringMatrix implements RMatrix<String> {

  private List<String> rowNames;

  private List<String> columnNames;

  private List<List<String>> rows;

  public RockStringMatrix() {
  }

  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  @Override
  public String[] getColumnNames() {
    return columnNames.toArray(new String[0]);
  }

  public void setRowNames(List<String> rowNames) {
    this.rowNames = rowNames;
  }

  @Override
  public String[] getRowNames() {
    return rowNames.toArray(new String[0]);
  }

  public void setRows(List<List<String>> rows) {
    this.rows = rows;
  }

  @Override
  public List<String[]> iterateRows() {
    return rows.stream().map(row -> row.toArray(new String[0])).collect(Collectors.toList());
  }

  @Override
  public List<String[]> iterateColumns() {
    return null;
  }
}
