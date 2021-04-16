/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import org.obiba.magma.Disposable;
import org.obiba.magma.ValueTable;

/**
 * An interface to prepare table push to R, referred by a symbol and to call for symbol persistence.
 */
public interface RSymbolWriter extends Disposable {

  /**
   * Get the symbol to be used to refer to the table's tibble in the R session.
   *
   * @param table
   * @return
   */
  String getSymbol(ValueTable table);

  /**
   * Call for the writing of the table's tibble symbol to any persistence media (file, database, etc.).
   *
   * @param table
   */
  void write(ValueTable table);

}
