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
