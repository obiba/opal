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
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.search.service.ValueTableValuesIndex;
import org.obiba.opal.search.service.ValuesIndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ValuesIndexImpl extends AbstractValueTableIndex implements ValueTableValuesIndex {

  private static final Logger log = LoggerFactory.getLogger(ValuesIndexImpl.class);

  private final ValuesIndexManagerImpl indexManager;

  public ValuesIndexImpl(ValuesIndexManagerImpl indexManager, ValueTable table) {
    super(indexManager.getName(), table);
    this.indexManager = indexManager;
  }

  @Override
  public String getIndexType() {
    return "values";
  }

  @Override
  public void delete() {
    File file = getIndexFile();
    if (file.exists()) {
      getIndexFile().delete();
      try (IndexWriter writer = indexManager.newIndexWriter()) {
        // Create a query to match the documents to delete
        Query query = new TermQuery(new Term("tableId", getValueTableReference()));
        // Delete documents that match the query
        writer.deleteDocuments(query);
        writer.commit();
      } catch (Exception e) {
        log.error("Values entities index delete failed", e);
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
    return new File(ValuesIndexManagerImpl.VALUES_INDEX_DIR, getValueTableReference() + ".idx");
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

  @Override
  public String getFieldName(Variable variable) {
    return variable.getName();
  }

  @Override
  public String getFieldName(String variableName) {
    return variableName;
  }

  public Document asDocument(VariableEntity entity) {
    Document doc = new Document();
    doc.add(new StringField("tableId", getValueTableReference(), Field.Store.YES));
    doc.add(new StringField("id", entity.getIdentifier(), Field.Store.YES));
    doc.add(new StringField("type", entity.getType(), Field.Store.YES));
    doc.add(new StringField("project", table.getDatasource().getName(), Field.Store.YES));
    doc.add(new StringField("datasource", table.getDatasource().getName(), Field.Store.YES));
    doc.add(new StringField("table", table.getName(), Field.Store.YES));

    // tokenized
    doc.add(new TextField("id-tok", entity.getIdentifier(), Field.Store.YES));

    return  doc;
  }
}
