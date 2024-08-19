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

import org.obiba.magma.Attribute;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.spi.search.ValueTableVariablesIndex;

import java.io.File;
import java.io.IOException;

public class DefaultVariablesIndex extends DefaultValueTableIndex implements ValueTableVariablesIndex {

  public DefaultVariablesIndex(String name, ValueTable table) {
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
    return new File(DefaultVariablesIndexManager.VARIABLES_INDEX_DIR, getValueTableReference() + ".idx");
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

  public void addVariable(Variable variable) {
    // TODO add variable to index
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
