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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;

import org.apache.shiro.SecurityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.search.ValuesIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.finder.AbstractElasticSearchFinder;
import org.obiba.opal.search.finder.AbstractFinder;
import org.obiba.opal.search.finder.AbstractFinderQuery;
import org.obiba.opal.search.finder.AbstractMagmaFinder;
import org.obiba.opal.search.finder.AccessFilterTablesFinder;
import org.obiba.opal.search.finder.FinderResult;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.obiba.opal.web.search.support.IndexManagerHelper;
import org.obiba.opal.web.search.support.QueryTermJsonBuilder;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableEntityTablesResource implements AbstractTablesResource {

  private static final Logger log = LoggerFactory.getLogger(VariableEntityTablesResource.class);

  private final OpalSearchService opalSearchService;

  private final VariableEntityBean variableEntity;

  private final ValuesIndexManager indexManager;

  private final ElasticSearchProvider esProvider;

  public VariableEntityTablesResource(VariableEntityBean variableEntity, OpalSearchService opalSearchService,
      ValuesIndexManager indexManager, ElasticSearchProvider esProvider) {
    this.variableEntity = variableEntity;
    this.opalSearchService = opalSearchService;
    this.indexManager = indexManager;
    this.esProvider = esProvider;
  }

  @GET
  @NoAuthorization
  public List<Magma.TableDto> getTables() {
    // maybe should return 404 if list is empty?
    return getTables(0);
  }

  public List<Magma.TableDto> getTables(int limit) {

    FinderResult<List<Magma.TableDto>> results = new FinderResult<List<Magma.TableDto>>(
        new ArrayList<Magma.TableDto>());

    new VariableEntityTablesFinder() //
        .withLimit(limit) //
        .find(new VariableEntityTablesQuery(variableEntity), results);

    return results.getValue();
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

  public static class EntityTablesFinder
      extends AccessFilterTablesFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    @Override
    protected boolean isTableSearchable(ValueTable valueTable, VariableEntityTablesQuery query) {
      return valueTable.getEntityType().equals(query.getEntity().getType()) && areEntitiesReadable(valueTable);
    }

    private boolean areEntitiesReadable(ValueTable valueTable) {
      return SecurityUtils.getSubject().isPermitted("magma:/datasource/" + valueTable.getDatasource().getName() +
          "/table/" + valueTable.getName() + "/entities:GET");
    }
  }

  @SuppressWarnings("ClassTooDeepInInheritanceTree")
  public static class EntityTablesElasticSearchFinder
      extends AbstractElasticSearchFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    private final ValuesIndexManager indexManager;

    private final ElasticSearchProvider esProvider;

    public EntityTablesElasticSearchFinder(OpalSearchService opalSearchService, ValuesIndexManager indexManager,
        ElasticSearchProvider esProvider) {
      super(opalSearchService);
      this.indexManager = indexManager;
      this.esProvider = esProvider;
    }

    @Override
    public void executeQuery(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {

      Map<String, ValueTable> map = buildIndexValueTableMap(query, new IndexManagerHelper(indexManager));

      if(map.isEmpty()) {
        return;
      }

      try {
        JSONObject jsonResponse = executeEsQuery(query.getEntity().getIdentifier(),
            new ArrayList<String>(map.keySet()));
        // parse the jsonResponse and by using the map, create the required TableDtos
        log.debug("JSON ES Response {}", jsonResponse);
        JSONObject jsonHitsInfo = jsonResponse.getJSONObject("hits");

        if(jsonHitsInfo.getInt("total") > 0) {
          JSONArray jsonHits = jsonHitsInfo.getJSONArray("hits");
          int hitCount = jsonHits.length();

          for(int i = 0; i < hitCount; ++i) {
            String indexName = jsonHits.getJSONObject(i).getString("_type");
            ValueTable valueTable = map.get(indexName);

            if(valueTable != null) {
              Magma.TableDto tableDto = Dtos.asDto(valueTable, false).build();
              result.getValue().add(tableDto);
            }
          }
        }

      } catch(JSONException e) {
        throw new RuntimeException(e);
      }

    }

    private JSONObject executeEsQuery(String identifier, List<String> tableIndexNames) throws JSONException {
      QueryTermJsonBuilder.QueryTermsFiltersBuilder filtersBuilder = new QueryTermJsonBuilder.QueryTermsFiltersBuilder()
          .setFieldName("_type").addFilterValues(tableIndexNames);

      QueryTermJsonBuilder queryBuilder = new QueryTermJsonBuilder().setTermFieldName("_id")
          .setTermFieldValue(identifier).setTermFilters(filtersBuilder.build());

      if(getLimit() > 0) {
        queryBuilder.setSize(getLimit()).build();
      }

      EsQueryExecutor queryExecutor = new EsQueryExecutor(esProvider);

      return queryExecutor.execute(queryBuilder.build());
    }

    private Map<String, ValueTable> buildIndexValueTableMap(VariableEntityTablesQuery query,
        IndexManagerHelper indexManagerHelper) {
      Map<String, ValueTable> map = new HashMap<String, ValueTable>();
      Iterator<ValueTable> iterator = query.getTableFilter().iterator();

      while(iterator.hasNext()) {
        ValueTable valueTable = iterator.next();

        if(indexManager.isReady() && indexManager.getIndex(valueTable).isUpToDate()) {
          String tableIndexName = indexManagerHelper.setDatasource(valueTable.getDatasource().getName())
              .setTable(valueTable.getName()).getIndexName();

          map.put(tableIndexName, valueTable);

          // No need for the next finder to treat this table
          iterator.remove();
        }
      }

      return map;
    }

  }

  @SuppressWarnings("ClassTooDeepInInheritanceTree")
  public static class EntityTablesMagmaFinder
      extends AbstractMagmaFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    @Override
    public void executeQuery(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {

      for(ValueTable valueTable : query.getTableFilter()) {

        if(valueTable.hasValueSet(query.getEntity())) {
          Magma.TableDto tableDto = Dtos.asDto(valueTable, false).build();
          result.getValue().add(tableDto);

          if(getLimit() > 0 && result.getValue().size() == getLimit()) {
            break;
          }
        }
      }
    }

  }

  public class VariableEntityTablesFinder
      extends AbstractFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    @Override
    public void find(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {
      nextFinder(new EntityTablesFinder()) //
          .nextFinder(
              new EntityTablesElasticSearchFinder(opalSearchService, indexManager, esProvider).withLimit(getLimit())) //
          .nextFinder(new EntityTablesMagmaFinder().withLimit(getLimit()));
      next(query, result);
    }

  }

}
