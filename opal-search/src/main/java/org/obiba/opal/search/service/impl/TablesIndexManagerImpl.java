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
import org.obiba.opal.search.service.IndexSynchronization;
import org.obiba.opal.search.service.SearchQueryExecutor;
import org.obiba.opal.search.service.TablesIndexManager;
import org.obiba.opal.search.service.ValueTableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Component
public class TablesIndexManagerImpl implements TablesIndexManager, SystemService {

  private static final Logger log = LoggerFactory.getLogger(TablesIndexManagerImpl.class);

  private static final String INDEX_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "index";
  public static final String TABLES_INDEX_DIR = INDEX_DIR + File.separatorChar + "opal-tables";

  private Directory tablesDirectory;

  @Override
  public String getName() {
    return "opal-tables";
  }

  @Override
  public ValueTableIndex getIndex(ValueTable valueTable) {
    return new TablesIndexImpl(this, valueTable);
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new TablesIndexerImpl(this, valueTable, (TablesIndexImpl) index);
  }

  @Override
  public SearchQueryExecutor createQueryExecutor() {
    return new ContentQueryExecutor(tablesDirectory, Set.of("name", "project", "entity-type"), AnalyzerFactory.newTablesAnalyzer());
  }

  @Override
  public boolean isEnabled() {
    return tablesDirectory != null;
  }

  @Override
  public boolean isReady() {
    return tablesDirectory != null;
  }

  @Override
  public void drop() {
    try {
      stop();
      FileUtil.delete(new File(TABLES_INDEX_DIR));
      start();
    } catch (IOException e) {
      log.warn("Cannot delete index folder: {}", TABLES_INDEX_DIR, e);
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
      this.tablesDirectory = FSDirectory.open(new File(TABLES_INDEX_DIR, "lucene").toPath());
    } catch (IOException e) {
      log.error("Failed at opening lucene tables/variables index", e);
    }
  }

  @Override
  public void stop() {
    close(tablesDirectory);
    this.tablesDirectory = null;
  }

  private void close(Directory directory) {
    if (directory == null) return;
    try {
      directory.close();
    } catch (IOException e) {
      log.error("Failed at closing lucene tables index", e);
    }
  }
}
