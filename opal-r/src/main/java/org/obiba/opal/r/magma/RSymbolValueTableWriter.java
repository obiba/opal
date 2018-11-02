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
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.obiba.opal.spi.r.datasource.magma.RDatasource;
import org.obiba.opal.spi.r.datasource.magma.RSymbolWriter;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;

/**
 * Writes a tibble in a R session, save it as a file and get this file back in the opal file system.
 */
public class RSymbolValueTableWriter implements ValueTableWriter {

  private final String tableName;

  private final StaticDatasource datasource;

  private final ValueTableWriter valueTableWriter;

  private final RSymbolWriter symbolWriter;

  private final RSessionHandler rSessionHandler;

  private final TransactionTemplate txTemplate;

  private final String idColumnName;

  public RSymbolValueTableWriter(StaticDatasource datasource, ValueTableWriter wrapped, String tableName, RSymbolWriter symbolWriter, RSessionHandler rSessionHandler, TransactionTemplate txTemplate, String idColumnName) {
    this.tableName = tableName;
    this.datasource = datasource;
    this.valueTableWriter = wrapped;
    this.rSessionHandler = rSessionHandler;
    this.txTemplate = txTemplate;
    this.idColumnName = Strings.isNullOrEmpty(idColumnName) ? RDatasource.DEFAULT_ID_COLUMN_NAME : idColumnName;
    this.symbolWriter = symbolWriter;
  }

  @Override
  public VariableWriter writeVariables() {
    return valueTableWriter.writeVariables();
  }

  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return valueTableWriter.writeValueSet(entity);
  }

  @Override
  public void close() {
    valueTableWriter.close();
    // get in-memory table and persist it in R
    ValueTable valueTable = datasource.getValueTable(tableName);
    if (valueTable.getValueSetCount()>0) {
      // push table to a R tibble
      rSessionHandler.getSession().execute(new MagmaAssignROperation(symbolWriter.getSymbol(valueTable), valueTable, txTemplate, idColumnName));
      symbolWriter.write(valueTable);
    }
  }

}
