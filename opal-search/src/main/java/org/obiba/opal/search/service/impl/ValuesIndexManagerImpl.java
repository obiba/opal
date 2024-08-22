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

import org.obiba.core.util.FileUtil;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.service.VariableSummaryHandler;
import org.obiba.opal.search.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;

@Component
public class ValuesIndexManagerImpl implements ValuesIndexManager {

  private static final Logger log = LoggerFactory.getLogger(ValuesIndexManagerImpl.class);

  private static final String INDEX_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "index";
  public static final String VALUES_INDEX_DIR = INDEX_DIR + File.separatorChar + "opal-values";

  private final VariableSummaryHandler variableSummaryHandler;

  private final ThreadFactory threadFactory;

  @Autowired
  public ValuesIndexManagerImpl(VariableSummaryHandler variableSummaryHandler, ThreadFactory threadFactory) {
    this.variableSummaryHandler = variableSummaryHandler;
    this.threadFactory = threadFactory;
  }

  @Override
  public String getName() {
    return "opal-values";
  }

  @Override
  public ValueTableValuesIndex getIndex(ValueTable valueTable) {
    return new ValuesIndexImpl(getName(), valueTable);
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new ValuesIndexerImpl(this, valueTable, (ValuesIndexImpl) index);
  }

  @Override
  public SearchQueryExecutor createQueryExecutor() {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void drop() {
    try {
      FileUtil.delete(new File(VALUES_INDEX_DIR));
    } catch (IOException e) {
      log.warn("Cannot delete index folder: {}", VALUES_INDEX_DIR, e);
    }
  }

  @Override
  public void drop(ValueTable valueTable) {
    variableSummaryHandler.clearComputingSummaries(valueTable);
    getIndex(valueTable).delete();
  }

  @Override
  public boolean isIndexUpToDate(ValueTable valueTable) {
    return false;
  }

  @Override
  public boolean hasIndex(ValueTable valueTable) {
    return false;
  }

  ThreadFactory getThreadFactory() {
    return threadFactory;
  }

  VariableSummaryHandler getVariableSummaryHandler() {
    return variableSummaryHandler;
  }
}
