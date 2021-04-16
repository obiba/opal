/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entities;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.model.client.magma.TableDto;

import java.util.List;

/**
 * Suggest variables which values have been indexed: the table values have been indexed and the variable value type is indexable.
 */
public class IndexedVariableSuggestOracle extends VariableSuggestOracle {

  private List<String> tableReferences = Lists.newArrayList();

  public IndexedVariableSuggestOracle(EventBus eventBus) {
    super(eventBus);
  }

  public void setIndexedTables(List<TableDto> tables) {
    this.tableReferences.clear();
    for (TableDto table : tables) {
      tableReferences.add(table.getDatasourceName() + "." + table.getName().replaceAll(" ", "+"));
    }
  }

  @Override
  protected String getQueryPrefix() {
    String prefix = "NOT nature:(BINARY OR GEO) "; // because these types of values are not indexed
    if (tableReferences.isEmpty()) return prefix;
    prefix = prefix + "AND reference:(" + Joiner.on(" OR ").join(tableReferences) + ") ";
    return prefix;
  }

  @Override
  protected boolean addAdvancedSearchSuggestion() {
    return false;
  }
}
