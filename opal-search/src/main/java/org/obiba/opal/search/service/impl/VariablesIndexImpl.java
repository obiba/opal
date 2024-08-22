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
import org.apache.lucene.document.TextField;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableNature;
import org.obiba.opal.search.service.ValueTableVariablesIndex;

import java.io.File;
import java.io.IOException;

public class VariablesIndexImpl extends AbstractValueTableIndex implements ValueTableVariablesIndex {

  public VariablesIndexImpl(String name, ValueTable table) {
    super(name, table);
  }

  @Override
  public String getIndexType() {
    return "variables";
  }

  @Override
  public void delete() {
    File file = getIndexFile();
    if (file.exists())
      getIndexFile().delete();
  }

  @Override
  public Timestamps getTimestamps() {
    return new FileTimestamps(getIndexFile());
  }

  private File getIndexFile() {
    return new File(VariablesIndexManagerImpl.VARIABLES_INDEX_DIR, getValueTableReference() + ".idx");
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

  public Document asDocument(Variable variable) {
    Document doc = new Document();
    String fullName = getValueTableReference();
    doc.add(new TextField("project", table.getDatasource().getName(), Field.Store.YES));
    doc.add(new TextField("datasource", table.getDatasource().getName(), Field.Store.YES));
    doc.add(new TextField("table", table.getName(), Field.Store.YES));
    doc.add(new TextField("name", variable.getName(), Field.Store.YES));
    doc.add(new TextField("fullName", String.format("%s:%s", getValueTableReference(), variable.getName()), Field.Store.YES));

    doc.add(new TextField("entityType", variable.getEntityType(), Field.Store.YES));
    doc.add(new TextField("valueType", variable.getValueType().getName(), Field.Store.YES));
    if (variable.getOccurrenceGroup() != null)
      doc.add(new TextField("occurrenceGroup", variable.getOccurrenceGroup(), Field.Store.YES));
    doc.add(new TextField("repeatable", variable.isRepeatable() + "", Field.Store.YES));
    if (variable.getMimeType() != null)
      doc.add(new TextField("mimeType", variable.getMimeType(), Field.Store.YES));
    if (variable.getUnit() != null)
      doc.add(new TextField("unit", variable.getUnit(), Field.Store.YES));
    if (variable.getReferencedEntityType() != null)
      doc.add(new TextField("referencedEntityType", variable.getReferencedEntityType(), Field.Store.YES));
    doc.add(new TextField("nature", VariableNature.getNature(variable).name(), Field.Store.YES));

    if (variable.hasAttributes()) {
      for (Attribute attribute : variable.getAttributes()) {
        String value = attribute.getValue().toString();
        if (value != null)
          doc.add(new TextField(getFieldName(attribute), value, Field.Store.YES));
      }
    }

    if (variable.hasCategories()) {
      for (Category category : variable.getCategories()) {
        doc.add(new TextField("category", category.getName(), Field.Store.YES));
        if (category.hasAttributes()) {
          for (Attribute attribute : category.getAttributes()) {
            String value = attribute.getValue().toString();
            if (value != null)
              doc.add(new TextField("category-" + getFieldName(attribute), value, Field.Store.YES));
          }
        }
      }
    }

    return doc;
  }

  @Override
  public String getFieldName(Attribute attribute) {
    String field = attribute.getName();
    if(attribute.hasNamespace()) {
      field = attribute.getNamespace() + "-" + field;
    }
    if(attribute.isLocalised()) {
      field += "-" + attribute.getLocale();
    }
    return field.replace(' ','+').replace('.','_');
  }
}
