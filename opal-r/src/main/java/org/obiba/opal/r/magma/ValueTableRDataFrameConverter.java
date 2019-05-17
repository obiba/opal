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

import org.obiba.magma.ValueTable;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a R data.frame from a table: list of vectors of variables.
 */
class ValueTableRDataFrameConverter extends ValueTableRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableRDataFrameConverter.class);

  ValueTableRDataFrameConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  protected void doAssignTable(ValueTable table, String symbol) {
    REXPList list = getVectorsList(table);
    if (!withIdColumn() && hasMultilines()) {
      throw new IllegalArgumentException("Id column name is missing (there are multiple rows per entity).");
    }

    doAssignTmpVectorsList(list);
    doAssignDataFrame(table, symbol);
    doRemoveTmpVectorsList();
  }

  private void doAssignDataFrame(ValueTable table, String symbol) {
    // create the data.frame from the list of vectors
    magmaAssignROperation.doEval(String.format("is.null(base::assign('%s', as.data.frame(as.list(%s), stringsAsFactors=FALSE)))", symbol, VALUETABLE_LIST_SYMBOL));

    if (!withIdColumn()) {
      REXP ids = getIdsVector(table, withMissings());
      magmaAssignROperation.doAssign(".ids", ids);
      magmaAssignROperation.doEval(String.format("row.names(`%s`) <- .ids", symbol));
      magmaAssignROperation.doEval("base::rm(.ids)");
    }
  }

}
