/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.opal.spi.r.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * A value table based on a tibble.
 */
public class RValueTable extends AbstractValueTable implements TibbleTable {

  private static final Logger log = LoggerFactory.getLogger(RValueTable.class);

  private static final int MAX_DATA_POINTS = 10000;

  private final String symbol;

  private int idPosition;

  private Map<String, Integer> columnPositions = Maps.newHashMap();

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

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public boolean isMultilines() {
    return ((RVariableEntityProvider) getVariableEntityProvider()).isMultilines();
  }

  @Override
  public int getIdPosition() {
    return idPosition;
  }

  @Override
  public String getIdColumn() {
    return ((RVariableEntityProvider) getVariableEntityProvider()).getIdColumn();
  }

  //
  // Private methods
  //

  private void checkIsTibble() {
    boolean isTibble = executeLogical(String.format("is.tibble(`%s`)", getSymbol()));
    if (!isTibble) {
      throw new IllegalArgumentException(getSymbol() + " is not a tibble.");
    }
  }

  private void initialiseVariables() {
    String lambdaParam = "n";
    if (lambdaParam.equals(getSymbol())) lambdaParam = ".n";
    RServerResult columnDescs = execute(String.format(
        "lapply(colnames(`%s`), function(%s) { " +
            "attrs <- attributes(`%s`[[%s]]) ; " +
            "attrs$labels_names <- names(attrs$labels) ; " +
            "klass <- `%s` %%>%% select(%s) %%>%% head(10) %%>%% pull() %%>%% class ;" +
            "type <- `%s` %%>%% select(%s) %%>%% head(10) %%>%% pull() %%>%% tibble::type_sum() ;" +
            "list(name=%s, class=klass, type=type, attributes=attrs)" +
            "})",
        getSymbol(), lambdaParam,
        getSymbol(), lambdaParam,
        getSymbol(), lambdaParam,
        getSymbol(), lambdaParam,
        lambdaParam));
    List<RServerResult> columns = columnDescs.asList();
    try {
      int i = 0;
      for (RServerResult columnDesc : columns) {
        RNamedList<RServerResult> column = columnDesc.asNamedList();
        if (getIdColumn().equals(column.get("name").asStrings()[0])) {
          idPosition = i++;
          columnPositions.put(getIdColumn(), idPosition);
        } else {
          int pos = i++;
          RVariableValueSource varSource = new RVariableValueSource(this, columnDesc, pos);
          addVariableValueSource(varSource);
          columnPositions.put(varSource.getName(), pos);
        }
      }
    } catch (Exception e) {
      // ignore
      log.error("Variable init failure for tibble {}", getSymbol(), e);
    }
    int varCount = getVariableCount();
    int optimizedBatchSize = varCount > 0 ? MAX_DATA_POINTS / varCount : 0;
    log.debug("Optimized batch size: {}", optimizedBatchSize);
    if (optimizedBatchSize > getVariableEntityBatchSize())
      setVariableEntityBatchSize(optimizedBatchSize);
  }

  @Override
  public RServerResult execute(String script) {
    return execute(new RScriptROperation(script, false));
  }

  boolean executeLogical(String script) {
    return executeLogical(new RScriptROperation(script, false));
  }

  RServerResult execute(ROperationWithResult rop) {
    return doExecute(rop).getResult();
  }

  boolean executeLogical(ROperationWithResult rop) {
    return doExecute(rop).getResult().asLogical();
  }

  private ROperationWithResult doExecute(ROperationWithResult rop) {
    try {
      getRSession().execute(rop);
    } catch (Exception e) {
      log.error("R operation failed: {}", e.getMessage(), e);
      throw new MagmaRRuntimeException(e.getMessage());
    }
    return rop;
  }

  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

  @Override
  public String getDefaultLocale() {
    return ((RDatasource) getDatasource()).getLocale();
  }

  @Override
  public RVariableEntity getRVariableEntity(VariableEntity entity) {
    if (entity instanceof RVariableEntity) return (RVariableEntity) entity;
    return ((RVariableEntityProvider) getVariableEntityProvider()).getRVariableEntity(entity);
  }

  @Override
  public Map<String, Integer> getColumnPositions() {
    return columnPositions;
  }

  private ROperationTemplate getRSession() {
    return ((RDatasource) getDatasource()).getRSession();
  }

}
