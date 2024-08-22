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

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.service.ValueTableValuesIndex;

import java.io.File;
import java.io.IOException;

public class ValuesIndexImpl extends AbstractValueTableIndex implements ValueTableValuesIndex {

  public ValuesIndexImpl(String name, ValueTable table) {
    super(name, table);
  }

  @Override
  public String getIndexType() {
    return "values";
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
}
