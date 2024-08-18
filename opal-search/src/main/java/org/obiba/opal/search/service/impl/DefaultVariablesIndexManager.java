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
import org.obiba.opal.spi.search.IndexSynchronization;
import org.obiba.opal.spi.search.ValueTableIndex;
import org.obiba.opal.spi.search.ValueTableVariablesIndex;
import org.obiba.opal.spi.search.VariablesIndexManager;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DefaultVariablesIndexManager implements VariablesIndexManager {

  private static final String INDEX_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "index";
  public static final String VARIABLES_INDEX_DIR = INDEX_DIR + File.separatorChar + "opal-variables";

  @Override
  public String getName() {
    return "opal-variables";
  }

  @Override
  public ValueTableVariablesIndex getIndex(ValueTable valueTable) {
    return new DefaultVariablesIndex(getName(), valueTable);
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new DefaultVariablesIndexer(this, valueTable, (DefaultVariablesIndex) index);
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
}
