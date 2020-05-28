/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.base.Joiner;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.opal.spi.r.REvaluationRuntimeException;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RScriptROperation;
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

  private static final int MAX_DATA_POINTS = 10000;

  private final String symbol;

  private int idPosition;

  public RValueTable(@NotNull RDatasource datasource, @NotNull String name, @NotNull String symbol, String entityType, String idColumn) {
    super(datasource, name);
    this.symbol = symbol;
    this.idPosition = 0;
    setVariableEntityProvider(new RVariableEntityProvider(this, entityType, idColumn));
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
    String lambdaParam = "n";
    if (lambdaParam.equals(getSymbol())) lambdaParam = ".n";
    REXPGenericVector columnDescs = (REXPGenericVector) execute(String.format("lapply(colnames(`%s`), function(%s) { list(name=%s,class=class(`%s`[[%s]]),type=tibble::type_sum(`%s`[[%s]]), attributes=attributes(`%s`[[%s]])) })",
        getSymbol(), lambdaParam, lambdaParam, getSymbol(), lambdaParam, getSymbol(), lambdaParam, getSymbol(), lambdaParam));
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
    int optimizedBatchSize = MAX_DATA_POINTS / getVariableCount();
    log.debug("Optimized batch size: {}", optimizedBatchSize);
    if (optimizedBatchSize > getVariableEntityBatchSize())
      setVariableEntityBatchSize(optimizedBatchSize);
  }

  REXP execute(String script) {
    return execute(new RScriptROperation(script, false));
  }

  REXP execute(ROperationWithResult rop) {
    try {
      getRSession().execute(rop);
    } catch (REvaluationRuntimeException e) {
      String rmsg = Joiner.on("; ").join(e.getRMessages());
      log.error("R operation failed: {}", rmsg, e);
      throw new MagmaRRuntimeException(rmsg);
    }
    return rop.getResult();
  }

  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

  String getDefaultLocale() {
    return ((RDatasource) getDatasource()).getLocale();
  }

  private ROperationTemplate getRSession() {
    return ((RDatasource) getDatasource()).getRSession();
  }

  public RVariableEntity getRVariableEntity(VariableEntity entity) {
    if (entity instanceof RVariableEntity) return (RVariableEntity) entity;
    return ((RVariableEntityProvider)getVariableEntityProvider()).getRVariableEntity(entity);
  }
}
