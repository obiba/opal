package org.obiba.opal.spi.r.datasource.magma;

import org.obiba.magma.ValueTable;

/**
 *
 */
public interface RSymbolWriter {

  String getSymbol(ValueTable table);

  void write(ValueTable table);

}
