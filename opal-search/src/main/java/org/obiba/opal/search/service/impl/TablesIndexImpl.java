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
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class TablesIndexImpl extends AbstractValueTableIndex {

  private static final Logger log = LoggerFactory.getLogger(TablesIndexImpl.class);

  private final TablesIndexManagerImpl indexManager;

  public TablesIndexImpl(TablesIndexManagerImpl indexManager, ValueTable table) {
    super(indexManager.getName(), table);
    this.indexManager = indexManager;
  }

  @Override
  public String getIndexType() {
    return "tables";
  }

  @Override
  public void delete() {
    File file = getIndexFile();
    if (file.exists()) {
      getIndexFile().delete();
      try (IndexWriter writer = indexManager.newTablesIndexWriter()) {
        // Create a query to match the documents to delete
        Query query = new TermQuery(new Term("table-ref", getValueTableReference()));
        // Delete documents that match the query
        writer.deleteDocuments(query);
        writer.commit();
      } catch (Exception e) {
        log.error("Tables index delete failed", e);
      }
    }
  }

  @Override
  public Timestamps getTimestamps() {
    return new FileTimestamps(getIndexFile());
  }

  @Override
  public boolean exists() {
    return getIndexFile().exists();
  }

  private File getIndexFile() {
    return new File(TablesIndexManagerImpl.TABLES_INDEX_DIR, getValueTableReference() + ".idx");
  }

  public void create() {
    try {
      delete();
      getIndexFile().getParentFile().mkdirs();
      getIndexFile().createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Document asDocument() {
    Document doc = new Document();
    doc.add(new StringField("table-ref", getValueTableReference(), Field.Store.YES));
    doc.add(new StringField("id", getValueTableReference(), Field.Store.YES));

    doc.add(new TextField("project", table.getDatasource().getName(), Field.Store.YES));
    doc.add(new TextField("datasource", table.getDatasource().getName(), Field.Store.YES));
    doc.add(new TextField("name", table.getName(), Field.Store.YES));
    doc.add(new TextField("entity-type", table.getEntityType(), Field.Store.YES));

    String content = String.format("%s %s %s", table.getDatasource().getName(), table.getName(), table.getEntityType());
    doc.add(new TextField("content", content, Field.Store.NO));

    return doc;
  }

}
