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

import org.obiba.magma.*;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.rosuda.REngine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A value table based on a tibble.
 */
public class RValueTable extends AbstractValueTable {

  private static final Logger log = LoggerFactory.getLogger(RValueTable.class);

  private final String symbol;

  private int idPosition;

  public RValueTable(@NotNull RDatasource datasource, @NotNull String name, @NotNull String symbol, String entityType, String idColumn) {
    super(datasource, name);
    this.symbol = symbol;
    this.idPosition = 0;
    setVariableEntityProvider(new RVariableEntityProvider(this, entityType, idColumn));
    setEntityBatchSize(1000);
  }

  @Override
  public void initialise() {
    checkIsTibble();
    initialiseVariables();
    super.initialise();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new RValueSet(this, entity);
  }

  @Override
  protected ValueSetBatch getValueSetsBatch(List<VariableEntity> entities) {
    return new RValueSetBatch(this, entities);
  }

  String getSymbol() {
    return symbol;
  }

  boolean isMultilines() {
    return ((RVariableEntityProvider)getVariableEntityProvider()).isMultilines();
  }

  int getIdPosition() {
    return idPosition;
  }

  String getIdColumn() {
    return ((RVariableEntityProvider)getVariableEntityProvider()).getIdColumn();
  }

  //
  // Private methods
  //

  private void checkIsTibble() {
    REXP isTibble = execute(String.format("is.tibble(`%s`)", getSymbol()));
    if (isTibble.isLogical()) {
      REXPLogical isTibbleLogical = (REXPLogical) isTibble;
      if (isTibbleLogical.length() == 0 || !isTibbleLogical.isTRUE()[0]) throw new IllegalArgumentException(getSymbol() + " is not a tibble.");
    } else {
      throw new IllegalArgumentException("Cannot determine if " + getSymbol() + " is a tibble.");
    }
  }

  private void initialiseVariables() {
    REXPGenericVector columnDescs = (REXPGenericVector) execute(String.format("lapply(colnames(`%s`), function(n) { list(name=n,class=class(`%s`[[n]]),type=tibble::type_sum(`%s`[[n]]), attributes=attributes(`%s`[[n]])) })",
        getSymbol(), getSymbol(), getSymbol(), getSymbol()));
    RList columns = columnDescs.asList();
    try {
      for (int i=0; i<columns.size(); i++) {
        RList column = ((REXPGenericVector)columns.at(i)).asList();
        String colname = column.at("name").asString();
        if (!getIdColumn().equals(colname))
          addVariableValueSource(new RVariableValueSource(this, column, i + 1));
        else
          idPosition = i + 1;
      }
    } catch (REXPMismatchException e) {
      // ignore
      log.error("Variable init failure for tibble {}", getSymbol(), e);
    }
  }

  REXP execute(String script) {
    return execute(new RScriptROperation(script, false));
  }

  REXP execute(ROperationWithResult rop) {
    getRSession().execute(rop);
    return rop.getResult();
  }

  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

  private OpalRSession getRSession() {
    return ((RDatasource) getDatasource()).getRSession();
  }

}
