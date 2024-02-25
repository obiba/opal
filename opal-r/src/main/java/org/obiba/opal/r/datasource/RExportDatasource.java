/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.datasource;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.obiba.opal.spi.r.datasource.magma.RSymbolWriter;

import javax.validation.constraints.NotNull;

/**
 * After a table has been assigned to a tibble symbol in R, this tibble can be exported to another media.
 */
public class RExportDatasource extends RAssignDatasource {

  private final RSymbolWriter symbolWriter;

  public RExportDatasource(String name, RSessionHandler rSessionHandler, RSymbolWriter symbolWriter) {
    super(name, "D", rSessionHandler);
    this.symbolWriter = symbolWriter;
  }

  @Override
  protected String getSymbol(String tableName) {
    return symbolWriter.getSymbol(getValueTable(tableName));
  }

  @Override
  protected void onDispose() {
    Disposables.dispose(symbolWriter);
    super.onDispose();
  }

  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    return new RExportValueTableWriter(super.createWriter(tableName, entityType), tableName);
  }

  private class RExportValueTableWriter implements ValueTableWriter {

    private final ValueTableWriter wrapped;

    private final String tableName;

    private boolean valuesWritten;

    private RExportValueTableWriter(ValueTableWriter wrapped, String tableName) {
      this.wrapped = wrapped;
      this.tableName = tableName;
    }

    @Override
    public VariableWriter writeVariables() {
      return wrapped.writeVariables();
    }

    @NotNull
    @Override
    public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
      valuesWritten = true;
      return wrapped.writeValueSet(entity);
    }

    @Override
    public void close() {
      wrapped.close();
      if (valuesWritten) {
        // at this point the symbol refers to a tibble
        symbolWriter.write(getValueTable(tableName));
      }
    }
  }
}
