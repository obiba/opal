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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.search.service.IndexSynchronization;
import org.obiba.opal.search.service.SearchQueryExecutor;
import org.obiba.opal.search.service.TablesIndexManager;
import org.obiba.opal.search.service.ValueTableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class TablesIndexManagerImpl implements TablesIndexManager, SystemService {

  private static final Logger log = LoggerFactory.getLogger(TablesIndexManagerImpl.class);

  private static final String INDEX_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "index";
  public static final String TABLES_INDEX_DIR = INDEX_DIR + File.separatorChar + "opal-tables";

  private Directory tablesDirectory;

  private Directory entitiesDirectory;

  // one writer per directory, shared by the indexer and the table drops, as Lucene allows only one
  private IndexWriter tablesWriter;

  private IndexWriter entitiesWriter;

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
    // without a last update, a table cannot tell whether it changed since it was indexed
    Timestamps timestamps = valueTable.getTimestamps();
    Value lastUpdate = timestamps == null ? null : timestamps.getLastUpdate();
    return lastUpdate != null && !lastUpdate.isNull() && getIndex(valueTable).isUpToDate();
  }

  @Override
  public boolean hasIndex(ValueTable valueTable) {
    return getIndex(valueTable).exists();
  }

  IndexWriter getTablesIndexWriter() {
    return tablesWriter;
  }

  @Override
  public Set<String> findTableReferences(VariableEntity entity) {
    return entitiesDirectory == null ? Set.of() : findTableReferences(entitiesDirectory, entity);
  }

  IndexWriter getEntitiesIndexWriter() {
    return entitiesWriter;
  }

  static IndexWriter newEntitiesIndexWriter(Directory directory) {
    try {
      return new IndexWriter(directory, new IndexWriterConfig(AnalyzerFactory.newEntitiesAnalyzer()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Replace the entity documents of a table, to be committed by the caller.
   */
  static void writeEntities(IndexWriter writer, String tableReference, Iterable<VariableEntity> entities) throws IOException {
    writer.deleteDocuments(new Term("table-ref", tableReference));
    for (VariableEntity entity : entities) {
      Document doc = new Document();
      doc.add(new StringField("table-ref", tableReference, Field.Store.YES));
      doc.add(new StringField("id", entity.getIdentifier(), Field.Store.NO));
      doc.add(new StringField("type", entity.getType(), Field.Store.NO));
      writer.addDocument(doc);
    }
  }

  static Set<String> findTableReferences(Directory directory, VariableEntity entity) {
    Set<String> references = new HashSet<>();
    try (IndexReader reader = DirectoryReader.open(directory)) {
      BooleanQuery query = new BooleanQuery.Builder()
          .add(new TermQuery(new Term("id", entity.getIdentifier())), BooleanClause.Occur.FILTER)
          .add(new TermQuery(new Term("type", entity.getType())), BooleanClause.Occur.FILTER)
          .build();
      IndexSearcher searcher = new IndexSearcher(reader);
      StoredFields storedFields = reader.storedFields();
      // the hits are the tables of the entity, few compared to the documents of the index
      for (ScoreDoc hit : searcher.search(query, Math.max(1, searcher.count(query))).scoreDocs) {
        references.add(storedFields.document(hit.doc).get("table-ref"));
      }
    } catch (IndexNotFoundException e) {
      // nothing indexed yet
    } catch (IOException e) {
      log.error("Failed at searching lucene entities index", e);
    }
    return references;
  }

  @Override
  public void start() {
    try {
      // tables indexed before entities were: reindex them all
      if (!new File(TABLES_INDEX_DIR, "lucene-entities").exists()) FileUtil.delete(new File(TABLES_INDEX_DIR));
      this.tablesDirectory = FSDirectory.open(new File(TABLES_INDEX_DIR, "lucene").toPath());
      this.entitiesDirectory = FSDirectory.open(new File(TABLES_INDEX_DIR, "lucene-entities").toPath());
      this.tablesWriter = new IndexWriter(tablesDirectory, new IndexWriterConfig(AnalyzerFactory.newTablesAnalyzer()));
      this.entitiesWriter = newEntitiesIndexWriter(entitiesDirectory);
    } catch (IOException e) {
      log.error("Failed at opening lucene tables/variables index", e);
    }
  }

  @Override
  public void stop() {
    close(tablesWriter);
    close(entitiesWriter);
    this.tablesWriter = null;
    this.entitiesWriter = null;
    close(tablesDirectory);
    close(entitiesDirectory);
    this.tablesDirectory = null;
    this.entitiesDirectory = null;
  }

  private void close(Closeable closeable) {
    if (closeable == null) return;
    try {
      closeable.close();
    } catch (IOException e) {
      log.error("Failed at closing lucene tables index", e);
    }
  }
}
