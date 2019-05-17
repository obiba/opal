/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import com.google.common.base.Strings;
import org.obiba.magma.ValueTable;
import org.obiba.opal.spi.r.datasource.magma.RDatasource;
import org.rosuda.REngine.REXPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a R tibble from a table: list of vectors of variables.
 */
class ValueTableRTibbleConverter extends ValueTableRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableRTibbleConverter.class);

  ValueTableRTibbleConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  protected void doAssignTable(ValueTable table, String symbol) {
    REXPList list = getVectorsList(table);

    doAssignTmpVectorsList(list);
    doAssignTibble(symbol);
    doRemoveTmpVectorsList();
  }

  private void doAssignTibble(String symbol) {
    magmaAssignROperation.doEnsurePackage("tibble");
    magmaAssignROperation.doEval(String.format("is.null(base::assign('%s', tibble::as_tibble(as.list(%s))))", symbol, VALUETABLE_LIST_SYMBOL));
  }

  @Override
  protected boolean withIdColumn() {
    return true;
  }

  @Override
  protected String getIdColumnName() {
    String col = super.getIdColumnName();
    return Strings.isNullOrEmpty(col) ? RDatasource.DEFAULT_ID_COLUMN_NAME : col;
  }

  @Override
  protected boolean withFactors() {
    return false;
  }

  @Override
  protected boolean withLabelled() {
    return true;
  }
}
