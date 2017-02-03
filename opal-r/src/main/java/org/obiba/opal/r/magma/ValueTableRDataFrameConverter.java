/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.r.magma;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
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
  public void doAssign(String symbol, String path) {
    // OPAL-2710 assigning a data.frame directly fails with a lot of rows
    resolvePath(path);
    if (getValueTable() == null) throw new IllegalStateException("Table must not be null");
    magmaAssignROperation.setEntities(getValueTable());
    RList list = getVariableVectors();
    if (!withIdColumn() && hasMultilines()) {
      throw new IllegalArgumentException("Id column name is missing (there are multiple rows per entity).");
    }
    REXP ids = getIdsVector(withMissings());

    String[] names = list.keys();
    if (names == null || names.length == 0) return;

    doAssignTmpVectors(ids, names, list);
    doAssignDataFrame(names);
    doRemoveTmpVectors(names);
  }

  private void doAssignDataFrame(String... names) {
    // create the data.frame from the vectors
    StringBuilder args = new StringBuilder();
    if (withIdColumn()) {
      args.append(String.format("'%s'=%s", getIdColumnName(),
          getTmpVectorName(getSymbol(), getIdColumnName())));
    }

    if (withUpdatedColumn()) {
      if (args.length() > 0) args.append(", ");
      args.append(String.format("'%s'=%s", getUpdatedColumnName(),
          getTmpVectorName(getSymbol(), getUpdatedColumnName())));
    }
    for (String name : names) {
      if (args.length() > 0) args.append(", ");
      args.append(String.format("'%s'=%s", name, getTmpVectorName(getSymbol(), name)));
    }
    if (!withIdColumn())
      args.append(String.format(", row.names=%s", getTmpVectorName(getSymbol(), "row.names")));

    log.info("data.frame arguments: {}", args);
    magmaAssignROperation.doEval(String.format("base::assign('%s', data.frame(%s, stringsAsFactors=FALSE))", getSymbol(), args));
  }

}
