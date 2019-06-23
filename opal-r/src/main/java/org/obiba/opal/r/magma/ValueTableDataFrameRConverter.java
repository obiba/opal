package org.obiba.opal.r.magma;

import org.obiba.opal.spi.r.RRuntimeException;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Base implementation of Magma R converters using file dumps to be transferred to R and read back by R.
 */
class ValueTableDataFrameRConverter extends ValueTableTibbleRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableDataFrameRConverter.class);

  private static final String LABELLED_AS_FACTOR_SCRIPT = ".haven_labelled.as.factor.R";

  ValueTableDataFrameRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  public void doAssign(String symbol, String path) {
    super.doAssign(symbol, path);
    magmaAssignROperation.doEval(String.format("base::is.null(base::assign('%s', as.data.frame(`%s`)))", getSymbol(), getSymbol()));
    // turn the haven_labelled vectors to factors

    try (InputStream is = new ClassPathResource(LABELLED_AS_FACTOR_SCRIPT).getInputStream();) {
      magmaAssignROperation.doWriteFile(LABELLED_AS_FACTOR_SCRIPT, is);
    } catch (IOException e) {
      throw new RRuntimeException(e);
    }
    magmaAssignROperation.doEval(String.format("base::source('%s')", LABELLED_AS_FACTOR_SCRIPT));
    magmaAssignROperation.doEval(String.format("`%s` <- .haven_labelled.as.factor(`%s`)",
        getSymbol(),getSymbol()));
    magmaAssignROperation.doEval("base::rm(.haven_labelled.as.factor)");
    magmaAssignROperation.doEval(String.format("base::unlink('%s')", LABELLED_AS_FACTOR_SCRIPT));
    if (!withIdColumn()) {
      magmaAssignROperation.doEval(String.format("rownames(`%s`) <- `%s`[['%s']]", getSymbol(), getSymbol(), getIdColumnName()));
      magmaAssignROperation.doEval(String.format("`%s`['%s'] <- NULL", getSymbol(), getIdColumnName()));
    }
  }

}
