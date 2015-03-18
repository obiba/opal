/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.opal.core.magma.QueryWhereClause;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * {@link org.obiba.magma.views.WhereClause} that apply a where filter by issuing a query on a table index.
 */
@Component("searchQueryWhereClause")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SearchQueryWhereClause extends AbstractSearchUtility implements QueryWhereClause {

  private ValueTable valueTable;

  private String query;

  @Override
  public void setValueTable(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public boolean where(ValueSet valueSet) {
    return false;
  }

  @Override
  public boolean where(ValueSet valueSet, View view) {
    return false;
  }

  @Override
  protected String getSearchPath() {
    return variablesIndexManager.getIndex(valueTable).getRequestPath();
  }
}
