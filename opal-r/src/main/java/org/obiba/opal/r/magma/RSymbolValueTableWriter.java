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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.r.magma.util.RCopyBufferSizeProvider;
import org.obiba.opal.r.magma.util.RCopyBufferStaticSizeProvider;
import org.obiba.opal.spi.r.BindRowsAssignROperation;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.obiba.opal.spi.r.datasource.magma.RDatasource;
import org.obiba.opal.spi.r.datasource.magma.RSymbolWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Writes a tibble in a R session, save it as a file and get this file back in the opal file system.
 */
public class RSymbolValueTableWriter implements ValueTableWriter {

  private static final Logger log = LoggerFactory.getLogger(RSymbolValueTableWriter.class);

  private final String tableName;

  private final ValueTableWriter wrappedValueTableWriter;

  private final StaticDatasource datasource;

  private final RSymbolWriter symbolWriter;

  private final RSessionHandler rSessionHandler;

  private final TransactionTemplate txTemplate;

  private final String idColumnName;

  private int bufferedTableCount = 0;

  private int optimizedDataPoints = 0;

  private final long usedMemoryInit;

  private final long maxFreeMemoryInit;

  private final RCopyBufferSizeProvider memorySizeProvider;

  public RSymbolValueTableWriter(StaticDatasource datasource, ValueTableWriter wrappedValueTableWriter, String tableName, RSymbolWriter symbolWriter, RSessionHandler rSessionHandler, TransactionTemplate txTemplate, String idColumnName, RCopyBufferSizeProvider memorySizeProvider) {
    this.tableName = tableName;
    this.datasource = datasource;
    this.wrappedValueTableWriter = wrappedValueTableWriter;
    this.rSessionHandler = rSessionHandler;
    this.txTemplate = txTemplate;
    this.idColumnName = Strings.isNullOrEmpty(idColumnName) ? RDatasource.DEFAULT_ID_COLUMN_NAME : idColumnName;
    this.symbolWriter = symbolWriter;
    // clean memory and get initial state
    System.gc();
    this.usedMemoryInit = getApplicationUsedMemory();
    this.maxFreeMemoryInit = Runtime.getRuntime().maxMemory() - usedMemoryInit;
    this.memorySizeProvider = memorySizeProvider == null ? new RCopyBufferStaticSizeProvider() : memorySizeProvider;
  }

  @Override
  public VariableWriter writeVariables() {
    return wrappedValueTableWriter.writeVariables();
  }

  @Override
  public synchronized ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    if (datasource.hasValueTable(tableName)) {
      StaticValueTable table = (StaticValueTable) datasource.getValueTable(tableName);

      if (optimizedDataPoints <= 0) {
        long bufferMemory = getApplicationUsedMemory() - usedMemoryInit;
        optimizedDataPoints = memorySizeProvider.getOptimizedDataPointsCount(table, maxFreeMemoryInit, bufferMemory);
      }

      if (table.getVariableEntityBatchSize() <= table.getVariableEntityCount() || optimizedDataPoints > 0) {
        int entityCount = table.getVariableEntityCount();
        int variableCount = table.getVariableCount();
        int dataPointsCount = entityCount * variableCount;

        if (table.getVariableEntityBatchSize() <= table.getVariableEntityCount() || dataPointsCount > optimizedDataPoints) {
          log.debug("Buffered data points: {}", dataPointsCount);
          flushValueTable();
        }
      }
    }
    return wrappedValueTableWriter.writeValueSet(entity);
  }

  private long getApplicationUsedMemory() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  @Override
  public void close() {
    boolean hasValueSets = flushValueTable() > 0;
    wrappedValueTableWriter.close();

    StaticValueTable table = (StaticValueTable) datasource.getValueTable(tableName);
    if (hasValueSets) {
      String symbol = symbolWriter.getSymbol(table);
      List<String> symbols = Lists.newArrayList();
      for (int i = 0; i < bufferedTableCount; i++) {
        symbols.add(asValueTableSymbol(symbol, i));
      }
      if (symbols.size() > 0) {
        rSessionHandler.getSession().execute(new BindRowsAssignROperation(symbol, symbols));
      }

      symbolWriter.write(table);
      table.removeAllValues();
    }
  }

  /**
   * Assign StaticValueTable as a R Tibble.
   */
  private synchronized int flushValueTable() {
    // get in-memory table and persist it in R
    StaticValueTable valueTable = (StaticValueTable) datasource.getValueTable(tableName);
    int valueSetCount = valueTable.getValueSetCount();
    if (valueSetCount > 0) {
      // push table to a R tibble
      String tableSymbol = asValueTableSymbol(symbolWriter.getSymbol(valueTable), bufferedTableCount);
      log.debug("Assigning table: {} to {}", tableName, tableSymbol);
      rSessionHandler.getSession().execute(new MagmaAssignROperation(tableSymbol, valueTable, txTemplate, idColumnName));

      log.debug("Truncating table: {}", tableName);
      valueTable.removeAllValues();
      bufferedTableCount++;
    }
    return valueSetCount;
  }

  private String asValueTableSymbol(String symbol, int id) {
    return "." + symbol + "__" + id;
  }

}
