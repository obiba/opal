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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.r.DataSaveROperation;
import org.obiba.opal.r.FileReadROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;

/**
 * Writes a tibble in a R session.
 */
public class RValueTableWriter implements ValueTableWriter {

  private final String tableName;

  private final StaticDatasource datasource;

  private final ValueTableWriter valueTableWriter;

  private final File destination;

  private final OpalRSession rSession;

  private final TransactionTemplate txTemplate;

  private final String idColumnName;

  public RValueTableWriter(String tableName, String entityType, File destination, OpalRSession rSession, TransactionTemplate txTemplate, String idColumnName) {
    this.tableName = tableName;
    this.datasource = new StaticDatasource(destination.getName());
    this.valueTableWriter = datasource.createWriter(tableName, entityType);
    this.destination = destination;
    this.rSession = rSession;
    this.txTemplate = txTemplate;
    this.idColumnName = Strings.isNullOrEmpty(idColumnName) ? RDatasource.DEFAULT_ID_COLUMN_NAME : idColumnName;
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
    ValueTable valueTable = datasource.getValueTable(tableName);
    if (valueTable.getValueSetCount()>0) {
      String symbol = "D";
      // push table to a R tibble
      rSession.execute(new MagmaAssignROperation(symbol, valueTable, txTemplate, idColumnName));
      // save tibble in file in R
      rSession.execute(new DataSaveROperation(symbol, destination.getName()));
      // read back file from R to opal
      rSession.execute(new FileReadROperation(destination.getName(), destination));
    }
  }
}
