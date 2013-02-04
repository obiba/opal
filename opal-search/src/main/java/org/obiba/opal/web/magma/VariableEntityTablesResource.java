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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.finder.AbstractElasticSearchFinder;
import org.obiba.opal.web.finder.AbstractFinder;
import org.obiba.opal.web.finder.AbstractFinderQuery;
import org.obiba.opal.web.finder.AbstractMagmaFinder;
import org.obiba.opal.web.finder.FinderResult;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.obiba.opal.web.search.support.IndexManagerHelper;
import org.obiba.opal.web.search.support.QueryTermJsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableEntityTablesResource extends AbstractTablesResource {

  private static final Logger log = LoggerFactory.getLogger(VariableEntityTablesResource.class);

  private final OpalSearchService opalSearchService;

  private final VariableEntityBean variableEntity;

  private final IndexManager indexManager;

  private final ElasticSearchProvider esProvider;

  public VariableEntityTablesResource(VariableEntityBean variableEntity, OpalSearchService opalSearchService,
      IndexManager indexManager, ElasticSearchProvider esProvider) {
    this.variableEntity = variableEntity;
    this.opalSearchService = opalSearchService;
    this.indexManager = indexManager;
    this.esProvider = esProvider;
  }

  @GET
  public List<Magma.TableDto> getTables() {

    FinderResult<List<Magma.TableDto>> results = new FinderResult<List<Magma.TableDto>>(
        new ArrayList<Magma.TableDto>());
    VariableEntityTablesFinder finder = new VariableEntityTablesFinder();
    finder.find(new VariableEntityTablesQuery(variableEntity), results);

    return results.getValue();
  }

  public List<Magma.TableDto> getTables(int limit) {

    FinderResult<List<Magma.TableDto>> results = new FinderResult<List<Magma.TableDto>>(
        new ArrayList<Magma.TableDto>());
    VariableEntityTablesFinder finder = new VariableEntityTablesFinder().withLimit(limit);
    finder.find(new VariableEntityTablesQuery(variableEntity), results);

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

  public static class EntityTablesFinder extends
      AbstractFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    @Override
    public void find(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {

      for(Datasource datasource : MagmaEngine.get().getDatasources()) {
        for(ValueTable valueTable : datasource.getValueTables()) {

          if(valueTable.getEntityType().equals(query.getEntity().getType())) {
            query.getTableFilter().add(valueTable);
          }
        }
      }

      next(query, result);
    }

  }

  @SuppressWarnings("ClassTooDeepInInheritanceTree")
  public static class EntityTablesElasticSearchFinder extends
      AbstractElasticSearchFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    private final IndexManager indexManager;

    private final ElasticSearchProvider esProvider;

    /**
     * Size of the resultset to return, 0 to return all
     */
    private int limit = 0;

    public EntityTablesElasticSearchFinder(OpalSearchService opalSearchService, IndexManager indexManager,
        ElasticSearchProvider esProvider) {
      super(opalSearchService);
      this.indexManager = indexManager;
      this.esProvider = esProvider;
    }

    @Override
    public void executeQuery(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result,
        String... indexes) {

      Map<String, ValueTable> map = buildIndexValueTableMap(query, new IndexManagerHelper(indexManager));

      if(map.isEmpty()) {
        return;
      }

      try {
        JSONObject jsonResponse = executeEsQuery(query.getEntity().getIdentifier(), new ArrayList<String>(map.keySet()),
            limit);
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
              Magma.TableDto tableDto = Magma.TableDto.newBuilder()
                  .setDatasourceName(valueTable.getDatasource().getName()).setName(valueTable.getName())
                  .setEntityType(valueTable.getEntityType()).build();

              result.getValue().add(tableDto);
            }
          }
        }

      } catch(JSONException e) {
        // ??? log error or throw RuntimeException
      }

    }

    private JSONObject executeEsQuery(String identifier, List<String> tableIndexNames, int limit) throws JSONException {
      QueryTermJsonBuilder.QueryTermsFiltersBuilder filtersBuilder = new QueryTermJsonBuilder.QueryTermsFiltersBuilder()
          .setFieldName("_type").addFilterValues(tableIndexNames);

      QueryTermJsonBuilder queryBuilder = new QueryTermJsonBuilder().setTermFieldName("_id")
          .setTermFieldValue(identifier).setTermFilters(filtersBuilder.build());

      if(limit > 0) {
        queryBuilder.setSize(limit).build();
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

        if(indexManager.isIndexable(valueTable) && indexManager.getIndex(valueTable).isUpToDate()) {
          String tableIndexName = indexManagerHelper.setDatasource(valueTable.getDatasource().getName())
              .setTable(valueTable.getName()).getIndexName();

          map.put(tableIndexName, valueTable);

          // No need for the next finder to treat this table
          iterator.remove();
        }
      }

      return map;
    }

    private EntityTablesElasticSearchFinder withLimit(int limit) {
      this.limit = limit;
      return this;
    }

  }

  @SuppressWarnings("ClassTooDeepInInheritanceTree")
  public static class EntityTablesMagmaFinder extends
      AbstractMagmaFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    private int limit = 0;

    @Override
    public void executeQuery(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {

      for(ValueTable valueTable : query.getTableFilter()) {

        if(valueTable.hasValueSet(query.getEntity())) {
          Magma.TableDto tableDto = Magma.TableDto.newBuilder().setDatasourceName(valueTable.getDatasource().getName())
              .setName(valueTable.getName()).setEntityType(valueTable.getEntityType()).build();

          result.getValue().add(tableDto);

          if(limit > 0 && result.getValue().size() == limit) {
            break;
          }
        }
      }
    }

    public EntityTablesMagmaFinder withLimit(int limit) {
      this.limit = limit;
      return this;
    }

  }

  public class VariableEntityTablesFinder extends
      AbstractFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    private int limit = 0;

    @Override
    public void find(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {
      nextFinder(new EntityTablesFinder()) //
          .nextFinder(
              new EntityTablesElasticSearchFinder(opalSearchService, indexManager, esProvider).withLimit(limit)) //
          .nextFinder(new EntityTablesMagmaFinder().withLimit(limit));
      next(query, result);
    }

    public VariableEntityTablesFinder withLimit(int limit) {
      this.limit = limit;
      return this;
    }
  }

}
