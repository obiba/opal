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
import org.obiba.opal.spi.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.ThreadFactory;

@Component
public class DefaultValuesIndexManager implements ValuesIndexManager {

  private static final String INDEX_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "index";
  public static final String VALUES_INDEX_DIR = INDEX_DIR + File.separatorChar + "opal-values";

  private final VariableSummaryHandler variableSummaryHandler;

  private final ThreadFactory threadFactory;

  @Autowired
  public DefaultValuesIndexManager(VariableSummaryHandler variableSummaryHandler, ThreadFactory threadFactory) {
    this.variableSummaryHandler = variableSummaryHandler;
    this.threadFactory = threadFactory;
  }

  @Override
  public String getName() {
    return "opal-values";
  }

  @Override
  public ValueTableValuesIndex getIndex(ValueTable valueTable) {
    return new DefaultValuesIndex(getName(), valueTable);
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new DefaultValuesIndexer(this, valueTable, (DefaultValuesIndex) index);
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
