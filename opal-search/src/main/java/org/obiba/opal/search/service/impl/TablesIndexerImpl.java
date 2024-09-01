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

import org.apache.lucene.index.IndexWriter;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.service.IndexManager;
import org.obiba.opal.search.service.IndexSynchronization;
import org.obiba.opal.search.service.ValueTableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TablesIndexerImpl implements IndexSynchronization {

  private static final Logger log = LoggerFactory.getLogger(TablesIndexerImpl.class);

  private final TablesIndexManagerImpl indexManager;

  private final ValueTable table;

  private final TablesIndexImpl index;

  protected int done = 0;

  protected boolean stop = false;

  TablesIndexerImpl(TablesIndexManagerImpl indexManager, ValueTable table, TablesIndexImpl index) {
    this.indexManager = indexManager;
    this.table = table;
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
    return done == 2;
  }

  @Override
  public float getProgress() {
    return done / 2f;
  }

  @Override
  public void stop() {
    stop = true;
  }

  @Override
  public void run() {
    index.create();
    try (IndexWriter writer = indexManager.newTablesIndexWriter()) {
      done = 1;
      writer.addDocument(index.asDocument());
      done = 2;
      writer.commit();
    } catch (Exception e) {
      log.error("Tables index writing failed", e);
    }
  }
}
