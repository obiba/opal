/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.web.gwt.app.client.search.entities;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.support.JsOpalMap;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.gwt.app.client.ui.ValueSetVariableCriterion;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;
import org.obiba.opal.web.model.client.opal.OpalMap;
import org.obiba.opal.web.model.client.search.EntitiesResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;
import java.util.Map;

import static org.obiba.opal.web.gwt.app.client.support.VariableDtoNature.*;

public class SearchEntitiesPresenter extends Presenter<SearchEntitiesPresenter.Display, SearchEntitiesPresenter.Proxy>
    implements HasPageTitle, SearchEntitiesUiHandlers {

  public static final String QUERY_SEP = "#";

  private final Translations translations;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final PlaceManager placeManager;

  private String selectedType;

  private List<String> queries;

  private List<VariableEntitySummaryDto> entityTypes;

  private List<TableDto> indexedTables;

  private final Map<String, JsArray<VariableDto>> tableVariables = Maps.newHashMap();

  private final Map<String, JsOpalMap> tableIndexSchemas = Maps.newHashMap();

  @Inject
  public SearchEntitiesPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
                                 DefaultBreadcrumbsBuilder breadcrumbsHelper, PlaceManager placeManager) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageSearchEntitiesTitle();
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    renderEntityTypes();
    renderTables();
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    selectedType = request.getParameter(ParameterTokens.TOKEN_TYPE, "Participant");
    String jQueries = request.getParameter(ParameterTokens.TOKEN_QUERY, "");
    tableVariables.clear();
    entityTypes = null;
    indexedTables = null;
    queries = null;
    if (!jQueries.isEmpty()) {
      queries = Splitter.on(QUERY_SEP).splitToList(jQueries);
      getView().clearResults(true);
      searchSelected();
    }
    else getView().reset();
  }

  @Override
  public void onClear() {
    queries.clear();
    updateHistory();
  }

  @Override
  public void onSearch(String entityType, List<String> queries) {
    selectedType = entityType;
    this.queries = queries;
    if (queries.isEmpty()) updateHistory();
    else {
      getView().clearResults(true);
      searchSelected();
    }
  }

  @Override
  public void onVariableFilter(final String datasource, final String table, final String variable) {
    ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(datasource, table, variable))
        .withCallback(new VariableFilterProcessor(datasource, table)).get().send();
  }

  //
  // Private methods
  //

  private void updateHistory() {
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .with(ParameterTokens.TOKEN_TYPE, selectedType);
    if (queries.isEmpty()) builder.without(ParameterTokens.TOKEN_QUERY);
    else builder.with(ParameterTokens.TOKEN_QUERY, Joiner.on(QUERY_SEP).join(queries));
    placeManager.updateHistory(builder.build(), true);
  }

  private void searchSelected() {
    UriBuilder builder = UriBuilders.DATASOURCES_ENTITIES_COUNT.create().query("type", selectedType);
    for (String query : queries) builder.query("query", query);
    ResourceRequestBuilderFactory.<EntitiesResultDto>newBuilder()
        .forResource(builder.build())
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // ignore
            getView().clearResults(false);
          }
        }, Response.SC_BAD_REQUEST)
        .withCallback(new ResourceCallback<EntitiesResultDto>() {
          @Override
          public void onResource(Response response, EntitiesResultDto resource) {
            getView().showResults(resource);
            updateHistory();
          }
        }).get().send();
  }

  private void renderEntityTypes() {
    ResourceRequestBuilderFactory.<JsArray<VariableEntitySummaryDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_ENTITY_TYPES.create().build()).get()
        .withCallback(new ResourceCallback<JsArray<VariableEntitySummaryDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableEntitySummaryDto> resource) {
            entityTypes = JsArrays.toList(resource);
            getView().setEntityTypes(entityTypes, selectedType);
            searchProvidedQueryIfReady();
          }
        }).send();
  }

  /**
   * Fetch the tables which values have been indexed.
   */
  private void renderTables() {
    getView().searchEnabled(false);
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_TABLES.create().query("indexed", "true").query("entityType", selectedType).build())
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            indexedTables = JsArrays.toList(resource);
            if (indexedTables.isEmpty()) {
              getView().searchEnabled(false);
              fireEvent(NotificationEvent.newBuilder().error("NoTableIndexed").build());
            } else {
              getView().searchEnabled(true);
              getView().setIndexedTables(indexedTables);
              searchProvidedQueryIfReady();
            }
          }
        }).get().send();
  }

  private void searchProvidedQueryIfReady() {
    getView().reset();
    // any provided query
    if (queries == null || queries.isEmpty()) return;
    // view init is completed
    if (indexedTables == null || entityTypes == null) return;
    List<String> validQueries = Lists.newArrayList();
    for (String query : queries) {
      ValueSetVariableCriterion parser = new ValueSetVariableCriterion(query);
      if (parser.isValid()) {
        validQueries.add(query);
        renderVariableFilter(parser);
      }
    }
    queries = validQueries;
    searchSelected();
  }

  private void renderVariableFilter(final ValueSetVariableCriterion filter) {
    ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(filter.getDatasourceName(), filter.getTableName(), filter.getVariableName()))
        .withCallback(new VariableFilterProcessor(filter)).get().send();
  }

  private String asTableReference(String datasource, String table) {
    return datasource + "." + table;
  }

  @ProxyStandard
  @NameToken(Places.SEARCH_ENTITIES)
  public interface Proxy extends ProxyPlace<SearchEntitiesPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<SearchEntitiesUiHandlers> {

    void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType);

    void setIndexedTables(List<TableDto> tables);

    void setEntityType(String selectedType);

    void setQuery(String query);

    void clearResults(boolean searchProgress);

    void reset();

    void addCategoricalCriterion(ValueSetVariableCriterion filter, QueryResultDto facet);

    void addNumericalCriterion(ValueSetVariableCriterion filter, QueryResultDto facet);

    void addDateCriterion(ValueSetVariableCriterion filter);

    void addDefaultCriterion(ValueSetVariableCriterion filter);

    void showResults(EntitiesResultDto results);

    void searchEnabled(boolean enabled);
  }

  private class VariableFilterProcessor implements ResourceCallback<VariableDto> {
    private final String datasource;
    private final String table;
    private ValueSetVariableCriterion criterion;

    public VariableFilterProcessor(String datasource, String table) {
      this.datasource = datasource;
      this.table = table;
    }

    public VariableFilterProcessor(ValueSetVariableCriterion criterion) {
      this.datasource = criterion.getDatasourceName();
      this.table = criterion.getTableName();
      this.criterion = criterion;
    }

    @Override
    public void onResource(Response response, VariableDto resource) {
      if (criterion != null) {
        criterion.setVariable(resource);
        addVariableCriterion();
      }
      else if (tableIndexSchemas.containsKey(asTableReference(datasource, table)))
        addVariableCriterion(resource);
      else
        addIndexSchemaAndVariableCriterion(resource);
    }

    private void addIndexSchemaAndVariableCriterion(final VariableDto variableDto) {
      // Fetch variable-field mapping for ES queries
      ResourceRequestBuilderFactory.<OpalMap>newBuilder().forResource(
          UriBuilders.DATASOURCE_TABLE_INDEX_SCHEMA.create()
              .build(datasource, table))
          .withCallback(new ResourceCallback<OpalMap>() {
            @Override
            public void onResource(Response response, OpalMap resource) {
              if (response.getStatusCode() == Response.SC_OK) {
                tableIndexSchemas.put(asTableReference(datasource, table), new JsOpalMap(resource));
                addVariableCriterion(variableDto);
              }
            }
          }).get().send();
    }

    private void addVariableCriterion(VariableDto variable) {
      this.criterion = new ValueSetVariableCriterion(datasource, table, variable,
          tableIndexSchemas.get(asTableReference(datasource, table)).getValue(variable.getName()));
      addVariableCriterion();
    }

    private void addVariableCriterion() {
      final VariableDtoNature nature = criterion.getNature();
      if (nature == CONTINUOUS || nature == CATEGORICAL) {
        // Filter for Categorical variable OR Numerical variable
        ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(
            UriBuilders.DATASOURCE_TABLE_FACET_VARIABLE_SEARCH.create()
                .build(datasource, table, criterion.getVariableName()))
            .withCallback(new ResourceCallback<QueryResultDto>() {
              @Override
              public void onResource(Response response, QueryResultDto resource) {
                if (nature == CONTINUOUS)
                  getView().addNumericalCriterion(criterion, resource);
                else
                  getView().addCategoricalCriterion(criterion, resource);
              }
            }).get().send();
      }
      else if (nature == TEMPORAL) getView().addDateCriterion(criterion);
      else getView().addDefaultCriterion(criterion);
    }
  }

}
