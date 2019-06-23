/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import org.obiba.opal.spi.r.RRuntimeException;
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

  private static final String TIBBLE_AS_DATAFRAME_SCRIPT = ".tibble.as.data.frame.R";

  ValueTableDataFrameRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  public void doAssign(String symbol, String path) {
    super.doAssign(symbol, path);
    try (InputStream is = new ClassPathResource(TIBBLE_AS_DATAFRAME_SCRIPT).getInputStream();) {
      magmaAssignROperation.doWriteFile(TIBBLE_AS_DATAFRAME_SCRIPT, is);
    } catch (IOException e) {
      throw new RRuntimeException(e);
    }
    magmaAssignROperation.doEval(String.format("base::source('%s')", TIBBLE_AS_DATAFRAME_SCRIPT));
    magmaAssignROperation.doEval(String.format("base::is.null(base::assign('%s', .tibble.as.data.frame(`%s`)))", getSymbol(), getSymbol()));
    // cleaning
    magmaAssignROperation.doEval("base::rm(.tibble.as.data.frame)");
    magmaAssignROperation.doEval(String.format("base::unlink('%s')", TIBBLE_AS_DATAFRAME_SCRIPT));
    if (!withIdColumn()) {
      magmaAssignROperation.doEval(String.format("rownames(`%s`) <- `%s`[['%s']]", getSymbol(), getSymbol(), getIdColumnName()));
      magmaAssignROperation.doEval(String.format("`%s`['%s'] <- NULL", getSymbol(), getIdColumnName()));
    }
  }

}
