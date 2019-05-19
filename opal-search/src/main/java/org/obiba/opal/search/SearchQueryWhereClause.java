/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.Initialisable;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.views.View;
import org.obiba.opal.core.magma.QueryWhereClause;
import org.obiba.opal.spi.search.ValuesIndexManager;
import org.obiba.opal.web.search.support.RQLParserFactory;
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
    if (!entities.isEmpty() || allEntities) return; // already initialised
    try {
      String esQuery = RQLParserFactory.parse(query, opalSearchService.getValuesIndexManager());
      if (Strings.isNullOrEmpty(esQuery)) {
        allEntities = true;
        return;
      }
      String safeQuery = "reference:\"" + valueTable.getTableReference() + "\" AND " + esQuery;
      opalSearchService.executeAllIdentifiersQuery(
          buildQuerySearch(safeQuery, 0, Integer.MAX_VALUE, Lists.newArrayList("identifier"), null, null, null),
          getSearchPath()).forEach(id -> entities.add(new VariableEntityBean(valueTable.getEntityType(), id)));
    } catch(Exception e) {
      log.error("Failed querying: {}", query, e);
    }
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
