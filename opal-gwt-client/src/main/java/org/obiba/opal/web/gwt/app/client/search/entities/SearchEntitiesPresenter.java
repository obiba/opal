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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
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
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
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

  private final Translations translations;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final PlaceManager placeManager;

  private String selectedType;

  private List<String> queries;

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
    //query = request.getParameter(ParameterTokens.TOKEN_QUERY, null);
    tableVariables.clear();
    indexedTables = null;
    getView().reset();
  }

  @Override
  public void onSearch(String entityType, List<String> queries) {
    selectedType = entityType;
    this.queries = queries;
    //GWT.log("onSearch=" + selectedType + ":" + query + ":" + query);
    getView().clearResults(true);
    searchSelected();
  }

  @Override
  public void onVariableFilter(final String datasource, final String table, final String variable) {
// Fetch variable and show its filter
    ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(datasource, table, variable))
        .withCallback(new VariableFieldProcessor(datasource, table)).get().send();
  }

  //
  // Private methods
  //

  private void updateHistory() {
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .with(ParameterTokens.TOKEN_TYPE, selectedType);
    placeManager.updateHistory(builder.build(), true);
  }

  private void searchSelected() {
    UriBuilder builder = UriBuilders.DATASOURCES_ENTITIES_COUNT.create().query("type", selectedType);
    for (String query : queries) builder.query("query", query);
    ResourceRequestBuilderFactory.<EntitiesResultDto>newBuilder()
        .forResource(builder.build())
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
            getView().setEntityTypes(JsArrays.toList(resource), selectedType);
          }
        }).send();
  }

  /**
   * Fetch the tables which values have been indexed.
   */
  private void renderTables() {
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_TABLES.create().query("indexed", "true").query("entityType", selectedType).build())
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            indexedTables = JsArrays.toList(resource);
            getView().setIndexedTables(indexedTables);
          }
        }).get().send();
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

    void addCategoricalVariableFilter(String datasource, String table, VariableDto variable, String fieldName, QueryResultDto facet);

    void addNumericalVariableFilter(String datasource, String table, VariableDto variable, String fieldName, QueryResultDto facet);

    void addDateVariableFilter(String datasource, String table, VariableDto variable, String fieldName);

    void addDefaultVariableFilter(String datasource, String table, VariableDto variable, String fieldName);

    void showResults(EntitiesResultDto results);
  }

  private class VariableFieldProcessor implements ResourceCallback<VariableDto> {
    private final String datasource;
    private final String table;

    public VariableFieldProcessor(String datasource, String table) {
      this.datasource = datasource;
      this.table = table;
    }

    @Override
    public void onResource(Response response, VariableDto resource) {
      if (tableIndexSchemas.containsKey(asTableReference(datasource, table)))
        addVariableCriterion(datasource, table, resource);
      else
        addIndexSchemaAndVariableCriterion(datasource, table, resource);
    }
  }

  private void addIndexSchemaAndVariableCriterion(final String datasource, final String table, final VariableDto variableDto) {
    // Fetch variable-field mapping for ES queries
    ResourceRequestBuilderFactory.<OpalMap>newBuilder().forResource(
        UriBuilders.DATASOURCE_TABLE_INDEX_SCHEMA.create()
            .build(datasource, table))
        .withCallback(new ResourceCallback<OpalMap>() {
          @Override
          public void onResource(Response response, OpalMap resource) {
            if(response.getStatusCode() == Response.SC_OK) {
              tableIndexSchemas.put(asTableReference(datasource, table), new JsOpalMap(resource));
              addVariableCriterion(datasource, table, variableDto);
            }
          }
        }).get().send();
  }

  private void addVariableCriterion(final String datasource, final String table, final VariableDto variable) {
    final String fieldName = tableIndexSchemas.get(asTableReference(datasource, table)).getValue(variable.getName());
    final VariableDtoNature nature = getNature(variable);
    if(nature == CONTINUOUS || nature == CATEGORICAL) {
      // Filter for Categorical variable OR Numerical variable
      ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(
          UriBuilders.DATASOURCE_TABLE_FACET_VARIABLE_SEARCH.create()
              .build(datasource, table, variable.getName()))
          .withCallback(new ResourceCallback<QueryResultDto>() {
            @Override
            public void onResource(Response response, QueryResultDto resource) {
              if(nature == CONTINUOUS)
                getView().addNumericalVariableFilter(datasource, table, variable, fieldName, resource);
              else
                getView().addCategoricalVariableFilter(datasource, table, variable, fieldName, resource);
            }
          }).get().send();
    }
    else if(nature == TEMPORAL) getView().addDateVariableFilter(datasource, table, variable, fieldName);
    else getView().addDefaultVariableFilter(datasource, table, variable, fieldName);
  }

}
