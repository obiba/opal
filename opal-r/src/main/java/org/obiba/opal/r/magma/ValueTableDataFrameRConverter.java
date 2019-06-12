package org.obiba.opal.r.magma;

import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of Magma R converters using file dumps to be transferred to R and read back by R.
 */
class ValueTableDataFrameRConverter extends ValueTableTibbleRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableDataFrameRConverter.class);

  ValueTableDataFrameRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  public void doAssign(String symbol, String path) {
    super.doAssign(symbol, path);
    RConnection connection = magmaAssignROperation.getRConnection();
    try {
      connection.eval(String.format("base::is.null(base::assign('%s', as.data.frame(`%s`)))", getSymbol(), getSymbol()));
      if (!withIdColumn()) {
        connection.eval(String.format("rownames(`%s`) <- `%s`[['%s']]", getSymbol(), getSymbol(), getIdColumnName()));
        connection.eval(String.format("`%s`['%s'] <- NULL", getSymbol(), getIdColumnName()));
      }
    } catch (RserveException e) {
      throw new MagmaRRuntimeException("Failed at assigning table to a data.frame", e);
    }
  }

}
