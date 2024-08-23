/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service.impl;

import com.google.common.base.Stopwatch;
import org.apache.lucene.index.IndexWriter;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.magma.support.VariableNature;
import org.obiba.opal.search.service.IndexManager;
import org.obiba.opal.search.service.IndexSynchronization;
import org.obiba.opal.search.service.ValueTableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValuesIndexerImpl implements IndexSynchronization {

  private static final Logger log = LoggerFactory.getLogger(ValuesIndexerImpl.class);

  private final ValuesIndexManagerImpl indexManager;

  private final ValueTable table;

  private final ValuesIndexImpl index;

  private final int total;

  protected int done = 0;

  protected boolean stop = false;

  ValuesIndexerImpl(ValuesIndexManagerImpl indexManager, ValueTable table, ValuesIndexImpl index) {
    this.indexManager = indexManager;
    this.table = table;
    this.total = table.getVariableEntityCount();
    this.index = index;
  }

  @Override
  public IndexManager getIndexManager() {
    return indexManager;
  }

  @Override
  public ValueTableIndex getValueTableIndex() {
    return index;
  }

  @Override
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  public boolean hasStarted() {
    return done > 0;
  }

  @Override
  public boolean isComplete() {
    return total > 0 && done >= total;
  }

  @Override
  public float getProgress() {
    return done / (float) total;
  }

  @Override
  public void stop() {
    stop = true;
  }

  @Override
  public void run() {
    index.create();
    ConcurrentValueTableReader.Builder.newReader()
        .withThreads(indexManager.getThreadFactory())
        .ignoreReadErrors()
        .from(table)
        //.variablesFilter(index.getVariables())
        .to(new ValuesReaderCallback())
        .build()
        .read();
  }

  private class ValuesReaderCallback implements ConcurrentValueTableReader.ConcurrentReaderCallback {

    private final Map<Variable, VariableNature> natures = new HashMap<>();

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private IndexWriter writer;

    @Override
    public void onBegin(List<VariableEntity> list, Variable... variables) {
      stopwatch.start();
      for(Variable variable : variables) {
        natures.put(variable, VariableNature.getNature(variable));
      }
      writer = indexManager.newIndexWriter();
    }

    @Override
    public void onValues(VariableEntity entity, Variable[] variables, Value... values) {
      if (stop) {
        return;
      }

      // only precompute summaries
      for(int i = 0; i < variables.length; i++) {
        indexManager.getVariableSummaryHandler().stackVariable(getValueTable(), variables[i], values[i]);
      }
      try {
        writer.addDocument(index.asDocument(entity));
      } catch (IOException e) {
        log.warn("Values entity index failure", e);
      }
      done++;
    }

    @Override
    public void onComplete() {
      stopwatch.stop();
      try {
        writer.commit();
        writer.close();
      } catch (Exception e) {
        log.error("Values entity index failure", e);
      }
      if(stop) {
        index.delete();
        indexManager.getVariableSummaryHandler().clearComputingSummaries(getValueTable());
      } else {
        // index.updateTimestamps();
        log.info("Indexed table {} in {}", getValueTable().getTableReference(), stopwatch);

        // compute summaries in a new thread
        new Thread(new Runnable() {
          @Override
          public void run() {
            indexManager.getVariableSummaryHandler().computeSummaries(getValueTable());
          }
        }).start();
      }
    }

    @Override
    public boolean isCancelled() {
      return false;
    }
  }
}
