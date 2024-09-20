/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search;

import com.google.common.collect.Lists;
import org.obiba.magma.Initialisable;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.views.View;
import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.core.magma.QueryWhereClause;
import org.obiba.opal.search.service.ValuesIndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * {@link org.obiba.magma.views.WhereClause} that apply a where filter by issuing a query on a table index.
 */
@Component("searchQueryWhereClause")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SearchQueryWhereClause extends AbstractSearchUtility implements QueryWhereClause, Initialisable {

  private static final Logger log = LoggerFactory.getLogger(SearchQueryWhereClause.class);

  private ValueTable valueTable;

  private String query;

  private boolean allEntities = false;

  private Collection<VariableEntity> entities = Lists.newArrayList();

  @Override
  public void initialise() {
    throw new DeprecatedOperationException("Unsupported operation: since Opal 5 values are not indexed");
  }

  @Override
  public void setValueTable(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public void setQuery(String query) {
    this.query = query;
    entities.clear();
    allEntities = false;
  }

  @Override
  public boolean where(ValueSet valueSet) {
    if (allEntities) return true;
    return entities.contains(valueSet.getVariableEntity());
  }

  @Override
  public boolean where(ValueSet valueSet, View view) {
    if (allEntities) return true;
    return entities.contains(valueSet.getVariableEntity());
  }

  @Override
  protected String getSearchPath() {
    ValuesIndexManager manager = opalSearchService.getValuesIndexManager();
    return manager.getName() + "/" + manager.getIndex(valueTable).getIndexType();
  }
}
