/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade.binary;

import java.io.IOException;

import org.obiba.core.util.TimedExecution;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
@Transactional
public class BinaryMover {

  private static final Logger log = LoggerFactory.getLogger(BinaryMover.class);

  @SuppressWarnings({ "MagicNumber", "ConstantConditions" })
  public void move(BinaryToMove binary) {

    TimedExecution timedExecution = new TimedExecution().start();

    Datasource datasource = MagmaEngine.get().getDatasource(binary.datasourceName);
    ValueTable table = datasource.getValueTable(binary.tableName);
    Variable variable = table.getVariable(binary.variableName);
    VariableEntity entity = new VariableEntityBean(table.getEntityType(), binary.entityId);
    ValueSet valueSet = table.getValueSet(entity);
    Value value = table.getValue(variable, valueSet);
    int size = 0;
    int nbOccurrences = 0;
    if(log.isDebugEnabled() && !value.isNull()) {
      if(value.isSequence()) {
        for(Value val : value.asSequence().getValue()) {
          nbOccurrences++;
          size += val.isNull() ? 0 : ((byte[]) val.getValue()).length;
        }
      } else {
        size = ((byte[]) value.getValue()).length;
      }
    }
    writeValue(datasource, table, variable, entity, value);

    log.debug("Moved {} KB ({} occurrences) for {} in {}", size / 1024, nbOccurrences, entity,
        timedExecution.end().formatExecutionTime());
  }

  private void writeValue(Datasource datasource, ValueTable table, Variable variable, VariableEntity entity,
      Value value) {
    ValueTableWriter tableWriter = null;
    ValueTableWriter.ValueSetWriter valueSetWriter = null;
    try {
      tableWriter = datasource.createWriter(table.getName(), table.getEntityType());
      valueSetWriter = tableWriter.writeValueSet(entity);
      valueSetWriter.writeValue(variable, value);
    } finally {
      if(valueSetWriter != null) {
        try {
          valueSetWriter.close();
        } catch(IOException ignored) {
        }
      }
      if(tableWriter != null) {
        try {
          tableWriter.close();
        } catch(IOException ignored) {
        }
      }
    }
  }
}
