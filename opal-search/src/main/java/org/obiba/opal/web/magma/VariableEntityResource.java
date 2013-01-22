/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.finder.AbstractElasticSearchFinder;
import org.obiba.opal.web.finder.AbstractFinder;
import org.obiba.opal.web.finder.AbstractFinderQuery;
import org.obiba.opal.web.finder.AbstractMagmaFinder;
import org.obiba.opal.web.finder.FinderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Scope("request")
@Path("/entity/{id}/type/{type}")
public class VariableEntityResource {

  private final OpalSearchService opalSearchService;

  @Autowired
  public VariableEntityResource(OpalSearchService opalSearchService) {
    if(opalSearchService == null) throw new IllegalArgumentException("opalSearchService cannot be null");
    this.opalSearchService = opalSearchService;
  }

  @Path("/tables")
  public VariableEntityTablesResource getTables() {

    VariableEntity entity = getEntity();

    FinderResult<List<ValueTable>> results = new FinderResult<List<ValueTable>>(new ArrayList<ValueTable>());
    VariableEntityTablesFinder finder = new VariableEntityTablesFinder();
    finder.find(new VariableEntityTablesQuery(entity), results);
    List<ValueTable> resultTables = results.getValue();

    // TODO...

    return null;
  }

  VariableEntity getEntity() {
    // TODO...
    return null;
  }

  public static class VariableEntityTablesQuery extends AbstractFinderQuery {

    private final VariableEntity entity;

    public VariableEntityTablesQuery(VariableEntity entity) {
      this.entity = entity;
    }

    public VariableEntity getEntity() {
      return entity;
    }
  }

  public static class EntityTablesFinder extends
      AbstractFinder<VariableEntityTablesQuery, FinderResult<List<ValueTable>>> {

    @Override
    public void find(VariableEntityTablesQuery query, FinderResult<List<ValueTable>> result) {
      // TODO find all tables with this entity and add them to tableFilter
//    query.getTableFilter().addAll(entityTables);
      next(query, result);
    }

  }

  @SuppressWarnings("ClassTooDeepInInheritanceTree")
  public static class EntityTablesElasticSearchFinder extends
      AbstractElasticSearchFinder<VariableEntityTablesQuery, FinderResult<List<ValueTable>>> {

    public EntityTablesElasticSearchFinder(OpalSearchService opalSearchService) {
      super(opalSearchService);
    }

    @Override
    public void executeQuery(VariableEntityTablesQuery query, FinderResult<List<ValueTable>> result,
        String... indexes) {
      // TODO elastic search query
      // http://www.elasticsearch.org/guide/reference/query-dsl/ids-filter.html
      // {
      //  "ids" : {
      //    "type" : [ " + indexedTables + " ],
      //    "values" : ["1", "4", "100"]
      //   }
      // }
      // SearchResponse response = opalSearchService.getClient()
      //   .prepareSearch(indexedTables.toArray(new String[indexedTables.size()]))
      // [...]
    }

  }

  public static class EntityTablesMagmaFinder extends
      AbstractMagmaFinder<VariableEntityTablesQuery, FinderResult<List<ValueTable>>> {
    @Override
    public void executeQuery(ValueTable valueTable, VariableEntityTablesQuery query,
        FinderResult<List<ValueTable>> result) {
      if(valueTable.hasValueSet(query.getEntity())) {
        result.getValue().add(valueTable);
      }
    }
  }

  public class VariableEntityTablesFinder extends
      AbstractFinder<VariableEntityTablesQuery, FinderResult<List<ValueTable>>> {

    @Override
    public void find(VariableEntityTablesQuery query, FinderResult<List<ValueTable>> result) {
      nextFinder(new EntityTablesFinder()) //
          .nextFinder(new EntityTablesElasticSearchFinder(opalSearchService)) //
          .nextFinder(new EntityTablesMagmaFinder());
      next(query, result);
    }
  }

}
