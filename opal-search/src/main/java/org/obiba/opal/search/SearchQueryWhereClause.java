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

import java.util.Collection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.Initialisable;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.views.View;
import org.obiba.opal.core.magma.QueryWhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * {@link org.obiba.magma.views.WhereClause} that apply a where filter by issuing a query on a table index.
 */
@Component("searchQueryWhereClause")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SearchQueryWhereClause extends AbstractSearchUtility implements QueryWhereClause, Initialisable {

  private static final Logger log = LoggerFactory.getLogger(SearchQueryWhereClause.class);

  private ValueTable valueTable;

  private String query;

  private Collection<VariableEntity> entities = Lists.newArrayList();

  @Override
  public void initialise() {
    try {
      JSONObject jsonResponse = executeQuery(buildQuerySearch(query, 0, Integer.MAX_VALUE, null, null, null, null).build());
      if(jsonResponse.isNull("error")) {
        JSONObject jsonHits = jsonResponse.getJSONObject("hits");
        JSONArray hits = jsonHits.getJSONArray("hits");
        for(int i = 0; i < hits.length(); i++) {
          JSONObject jsonHit = hits.getJSONObject(i);
          entities.add(new VariableEntityBean(valueTable.getEntityType(), jsonHit.getString("_id")));
        }
      }
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
  }

  @Override
  public boolean where(ValueSet valueSet) {
    return entities.contains(valueSet.getVariableEntity());
  }

  @Override
  public boolean where(ValueSet valueSet, View view) {
    return entities.contains(valueSet.getVariableEntity());
  }

  @Override
  protected String getSearchPath() {
    return valuesIndexManager.getIndex(valueTable).getRequestPath();
  }
}
