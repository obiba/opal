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
import org.obiba.opal.web.magma.support.finder.AbstractElasticSearchFinder;
import org.obiba.opal.web.magma.support.finder.AbstractFinder;
import org.obiba.opal.web.magma.support.finder.AbstractMagmaFinder;
import org.obiba.opal.web.magma.support.finder.AbstractQuery;
import org.obiba.opal.web.magma.support.finder.AccessFilterTablesFinder;
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

    List<ValueTable> results = new ArrayList<ValueTable>();
    VariableEntityTablesFinder finder = new VariableEntityTablesFinder();
    finder.find(new VariableEntityTablesQuery(entity), results);

    // TODO...

    return null;
  }

  VariableEntity getEntity() {
    // TODO...
    return null;
  }

  public static class VariableEntityTablesQuery extends AbstractQuery {

    private final VariableEntity entity;

    public VariableEntityTablesQuery(VariableEntity entity) {
      this.entity = entity;
    }

    public VariableEntity getEntity() {
      return entity;
    }
  }

  public class VariableEntityTablesFinder extends AbstractFinder<VariableEntityTablesQuery, ValueTable> {

    @Override
    public void find(VariableEntityTablesQuery query, List<ValueTable> result) {
      nextFinder(new AccessFilterTablesFinder<VariableEntityTablesQuery, ValueTable>()) //
          .nextFinder(new AbstractElasticSearchFinder<VariableEntityTablesQuery, ValueTable>(opalSearchService) {

            @Override
            public void executeQuery(VariableEntityTablesQuery query, List<ValueTable> result, String... indexes) {
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
          }) //
          .nextFinder(new AbstractMagmaFinder<VariableEntityTablesQuery, ValueTable>() {
            @Override
            public void executeQuery(ValueTable valueTable, VariableEntityTablesQuery query, List<ValueTable> result) {
              if(valueTable.hasValueSet(query.getEntity())) {
                result.add(valueTable);
              }
            }
          });

      next(query, result);
    }
  }

}
