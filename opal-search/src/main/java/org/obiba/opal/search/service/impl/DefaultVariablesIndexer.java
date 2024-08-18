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

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.spi.search.IndexManager;
import org.obiba.opal.spi.search.IndexSynchronization;
import org.obiba.opal.spi.search.ValueTableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultVariablesIndexer implements IndexSynchronization {

  private static final Logger log = LoggerFactory.getLogger(DefaultVariablesIndexer.class);

  private final DefaultVariablesIndexManager indexManager;

  private final ValueTable table;

  private final DefaultVariablesIndex index;

  private final int total;

  protected int done = 0;

  protected boolean stop = false;

  DefaultVariablesIndexer(DefaultVariablesIndexManager indexManager, ValueTable table, DefaultVariablesIndex index) {
    this.indexManager = indexManager;
    this.table = table;
    this.total = table.getVariableCount();
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
    for (Variable variable : index.getVariables()) {
      index.addVariable(variable);
      done++;
    }
  }

}
