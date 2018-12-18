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
import org.obiba.opal.spi.r.datasource.magma.RDatasource;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
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
  public void doAssign(String symbol, String path) {
    if (magmaAssignROperation.hasValueTable()) setValueTable(magmaAssignROperation.getValueTable());
    else resolvePath(path);
    if (getValueTable() == null) throw new IllegalStateException("Table must not be null");
    magmaAssignROperation.setEntities(getValueTable());
    RList list = getVariableVectors();
    REXP ids = getIdsVector(true);

    String[] names = list.keys();
    if (names == null || names.length == 0) return;

    doAssignTmpVectors(ids, names, list);
    doAssignTibble(names);
    doRemoveTmpVectors(names);
  }

  private void doAssignTibble(String... names) {
    // create the tibble from the vectors
    StringBuilder args = new StringBuilder();
    args.append(String.format("'%s'=%s", getIdColumnName(),
          getTmpVectorName(getSymbol(), getIdColumnName())));

    if (magmaAssignROperation.withUpdatedColumn()) {
      if (args.length() > 0) args.append(", ");
      args.append(String.format("'%s'=%s", getUpdatedColumnName(),
          getTmpVectorName(getSymbol(), getUpdatedColumnName())));
    }
    for (String name : names) {
      if (args.length() > 0) args.append(", ");
      args.append(String.format("'%s'=%s", name, getTmpVectorName(getSymbol(), name)));
    }
    log.debug("tibble arguments: {}", args);
    magmaAssignROperation.doEnsurePackage("tibble");
    magmaAssignROperation.doEval("library(tibble)");
    magmaAssignROperation.doEval(String.format("is.null(base::assign('%s', tibble(%s)))", getSymbol(), args));
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
