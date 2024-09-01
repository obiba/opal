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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.search.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class VariablesIndexManagerImpl implements VariablesIndexManager, SystemService {

  private static final Logger log = LoggerFactory.getLogger(VariablesIndexManagerImpl.class);

  private static final String INDEX_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "index";
  public static final String VARIABLES_INDEX_DIR = INDEX_DIR + File.separatorChar + "opal-variables";

  private Directory tablesDirectory;
  private Directory variablesDirectory;

  @Override
  public String getName() {
    return "opal-variables";
  }

  @Override
  public ValueTableVariablesIndex getIndex(ValueTable valueTable) {
    return new VariablesIndexImpl(this, valueTable);
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new VariablesIndexerImpl(this, valueTable, (VariablesIndexImpl) index);
  }

  @Override
  public SearchQueryExecutor createQueryExecutor() {
    return new VariablesQueryExecutor(variablesDirectory);
  }

  @Override
  public boolean isEnabled() {
    return variablesDirectory != null;
  }

  @Override
  public boolean isReady() {
    return variablesDirectory != null;
  }

  @Override
  public void drop() {
    try {
      stop();
      FileUtil.delete(new File(VARIABLES_INDEX_DIR));
      start();
    } catch (IOException e) {
      log.warn("Cannot delete index folder: {}", VARIABLES_INDEX_DIR, e);
    }
  }

  @Override
  public void drop(ValueTable valueTable) {
    getIndex(valueTable).delete();
  }

  @Override
  public boolean isIndexUpToDate(ValueTable valueTable) {
    return getIndex(valueTable).isUpToDate();
  }

  @Override
  public boolean hasIndex(ValueTable valueTable) {
    return getIndex(valueTable).exists();
  }

  IndexWriter newVariablesIndexWriter() {
    try {
      Analyzer analyzer = AnalyzerFactory.newVariablesAnalyzer();
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      return new IndexWriter(variablesDirectory, config);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  IndexWriter newTablesIndexWriter() {
    try {
      Analyzer analyzer = AnalyzerFactory.newTablesAnalyzer();
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      return new IndexWriter(tablesDirectory, config);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void start() {
    try {
      this.variablesDirectory = FSDirectory.open(new File(VARIABLES_INDEX_DIR, "lucene-variables").toPath());
      this.tablesDirectory = FSDirectory.open(new File(VARIABLES_INDEX_DIR, "lucene-tables").toPath());
    } catch (IOException e) {
      log.error("Failed at opening lucene tables/variables index", e);
    }
  }

  @Override
  public void stop() {
    close(variablesDirectory);
    this.variablesDirectory = null;
    close(tablesDirectory);
    this.variablesDirectory = null;
  }

  private void close(Directory directory) {
    if (directory == null) return;
    try {
      directory.close();
    } catch (IOException e) {
      log.error("Failed at closing lucene variables index", e);
    }
  }
}
