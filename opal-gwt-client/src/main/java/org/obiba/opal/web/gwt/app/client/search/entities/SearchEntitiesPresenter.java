/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entities;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
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
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.TableValuesIndexUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.gwt.app.client.ui.RQLIdentifierCriterionParser;
import org.obiba.opal.web.gwt.app.client.ui.RQLValueSetVariableCriterionParser;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rql.client.RQLParser;
import org.obiba.opal.web.gwt.rql.client.RQLQuery;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;
import org.obiba.opal.web.model.client.search.EntitiesResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.support.VariableDtoNature.*;

public class SearchEntitiesPresenter extends Presenter<SearchEntitiesPresenter.Display, SearchEntitiesPresenter.Proxy>
    implements HasPageTitle, SearchEntitiesUiHandlers {

  public static final String QUERY_SEP = ",";

  private static final int CRITERIA_LIMIT = 20;

  private final Translations translations;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final PlaceManager placeManager;

  private final ModalProvider<VariablesToViewPresenter> variablesToViewProvider;

  private String selectedType;

  private String idQuery;

  private List<String> queries;

  private List<VariableEntitySummaryDto> entityTypes;

  private List<TableDto> indexedTables;

  @Inject
  public SearchEntitiesPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
                                 DefaultBreadcrumbsBuilder breadcrumbsHelper, PlaceManager placeManager,
                                 ModalProvider<VariablesToViewPresenter> variablesToViewProvider) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.placeManager = placeManager;
    this.variablesToViewProvider = variablesToViewProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageSearchEntitiesTitle();
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(TableValuesIndexUpdatedEvent.getType(), new TableValuesIndexUpdatedEvent.Handler() {
      @Override
      public void onRefresh(TableValuesIndexUpdatedEvent event) {
        indexedTables = null;
      }
    });
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
    idQuery = request.getParameter(ParameterTokens.TOKEN_ID, "");
    String jQueries = request.getParameter(ParameterTokens.TOKEN_QUERY, "");
    queries = null;
    if (!jQueries.isEmpty()) {
      RQLQuery root = RQLParser.parse(jQueries);
      queries = Lists.newArrayList();
      for (int i = 0; i < root.getArgumentsSize(); i++) {
        RQLQuery q = root.getRQLQuery(i);
        queries.add(q.asString());
      }
      getView().clearResults(true);
      searchProvidedQueryIfReady();
    } else getView().reset();
  }

  @Override
  public void onClear() {
    queries.clear();
    updateHistory();
  }

  @Override
  public void onEntityType(String selection) {
    selectedType = selection;
    queries = null;
    indexedTables = null;
    renderTables();
  }

  @Override
  public void onAddToView(List<String> variableFullNames, List<String> magmaJsStatements) {
    VariablesToViewPresenter variablesToViewPresenter = variablesToViewProvider.get();
    String entityFilter = "";
    if (!magmaJsStatements.isEmpty()) {
      entityFilter = magmaJsStatements.get(0);
    }
    if (magmaJsStatements.size()>1) {
      entityFilter = entityFilter + "\n  .and(" + Joiner.on(")\n  .and(").join(magmaJsStatements.subList(1, magmaJsStatements.size())) + ")";
    }
    GWT.log(entityFilter);
    variablesToViewPresenter.show(variableFullNames, entityFilter);
  }

  @Override
  public void onSearch(String entityType, String idQuery, List<String> queries, int offset, int limit) {
    if (queries != null && queries.size() > CRITERIA_LIMIT) {
      fireEvent(NotificationEvent.newBuilder().warn("CriteriaLimitReached").args("" + CRITERIA_LIMIT).build());
      return;
    }
    selectedType = entityType;
    this.idQuery = idQuery;
    this.queries = queries;
    if (queries.isEmpty()) updateHistory();
    else {
      getView().clearResults(true);
      searchSelected(offset, limit);
    }
  }

  @Override
  public void onVariableCriterion(final String datasource, final String table, final String variable) {
    if (queries != null && queries.size() > CRITERIA_LIMIT) {
      fireEvent(NotificationEvent.newBuilder().warn("CriteriaLimitReached").args("" + CRITERIA_LIMIT).build());
      return;
    }
    ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(datasource, table, variable))
        .withCallback(new VariableCriterionProcessor(datasource, table)).get().send();
  }

  //
  // Private methods
  //

  private void updateHistory() {
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .with(ParameterTokens.TOKEN_TYPE, selectedType);
    if (queries.isEmpty()) {
      builder.without(ParameterTokens.TOKEN_ID);
      builder.without(ParameterTokens.TOKEN_QUERY);
    } else {
      builder.with(ParameterTokens.TOKEN_ID, idQuery);
      builder.with(ParameterTokens.TOKEN_QUERY, Joiner.on(QUERY_SEP).join(queries));
    }
    placeManager.updateHistory(builder.build(), true);
  }

  private void searchSelected() {
    searchSelected(0, Table.DEFAULT_PAGESIZE);
  }

  private void searchSelected(final int offset, final int limit) {
    String query = Joiner.on(",").join(queries);
    if (Strings.isNullOrEmpty(query)) return;
    UriBuilder builder = UriBuilders.DATASOURCES_ENTITIES_SEARCH.create()
        .query("type", selectedType)
        .query("format", "rql")
        .query("counts", "true")
        .query("offset", "" + offset)
        .query("limit", "" + limit);
    if (!Strings.isNullOrEmpty(idQuery)) builder.query("id", idQuery);
    if (queries.size() > 1) query = "and(" + query + ")";
    builder.query("query", query);
    ResourceRequestBuilderFactory.<EntitiesResultDto>newBuilder()
        .forResource(builder.build())
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // not supposed to happen
            getView().clearResults(false);
            fireEvent(NotificationEvent.newBuilder().error("MalformedSearchQuery").build());
          }
        }, Response.SC_BAD_REQUEST)
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // ignore
            getView().clearResults(false);
            fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
          }
        }, Response.SC_SERVICE_UNAVAILABLE)
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // ignore
            getView().clearResults(false);
            try {
              ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
              String msg = errorDto.getStatus();
              if (translations.userMessageMap().containsKey(msg))
                msg = TranslationsUtils.replaceArguments(translations.userMessageMap().get(errorDto.getStatus()), errorDto.getArgumentsArray());
              fireEvent(NotificationEvent.newBuilder().error(msg).build());
            } catch (Exception ignored) {
              fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
            }
          }
        }, Response.SC_INTERNAL_SERVER_ERROR)
        .withCallback(new ResourceCallback<EntitiesResultDto>() {
          @Override
          public void onResource(Response response, EntitiesResultDto resource) {
            getView().showResults(resource, offset, limit);
            updateHistory();
          }
        }).get().send();
  }

  private void renderEntityTypes() {
    if (entityTypes != null) return;
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
    if (indexedTables != null) return;
    getView().searchEnabled(false);
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_TABLES.create().query("indexed", "true").query("entityType", selectedType).build())
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            indexedTables = Lists.newArrayList(JsArrays.toIterable(resource));
            if (indexedTables.isEmpty()) {
              getView().searchUnavailable();
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
    List<String> invalidTableReferences = Lists.newArrayList();
    List<RQLValueSetVariableCriterionParser> criterions = Lists.newArrayList();
    for (String query : queries) {
      if (validQueries.size() == CRITERIA_LIMIT) {
        fireEvent(NotificationEvent.newBuilder().warn("CriteriaLimitReached").args("" + CRITERIA_LIMIT).build());
        break;
      }
      RQLValueSetVariableCriterionParser criterion = new RQLValueSetVariableCriterionParser(query);
      if (criterion.isValid()) {
        boolean found = false;
        for (TableDto table : indexedTables) {
          if (table.getDatasourceName().equals(criterion.getDatasourceName()) && table.getName().equals(criterion.getTableName())) {
            found = true;
            break;
          }
        }
        if (found) {
          validQueries.add(query);
          criterions.add(criterion);
        } else if (!invalidTableReferences.contains(criterion.getTableReference()))
          invalidTableReferences.add(criterion.getTableReference());
      }
    }
    queries = validQueries;
    if (invalidTableReferences.size() > 0)
      fireEvent(NotificationEvent.newBuilder().error("NotIndexedTable").args(Joiner.on(", ").join(invalidTableReferences)).build());
    // cascading rendering
    if (criterions.size() > 0) renderVariableCriterion(criterions, 0);
  }


  /**
   * Render a list of criterions in a cascading way.
   *
   * @param criterions
   * @param idx
   */
  private void renderVariableCriterion(final List<RQLValueSetVariableCriterionParser> criterions, final int idx) {
    if (idx < criterions.size()) {
      final RQLValueSetVariableCriterionParser criterion = criterions.get(idx);
      ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
          .build(criterion.getDatasourceName(), criterion.getTableName(), criterion.getVariableName()))
          .withCallback(new VariableCriterionProcessor(criterions, idx)).get().send();
    } else {
      searchSelected();
    }
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

    void addCategoricalCriterion(RQLIdentifierCriterionParser idCriterion, RQLValueSetVariableCriterionParser criterion, QueryResultDto facet, boolean opened);

    void addNumericalCriterion(RQLIdentifierCriterionParser idCriterion, RQLValueSetVariableCriterionParser criterion, QueryResultDto facet, boolean opened);

    void addDateCriterion(RQLIdentifierCriterionParser idCriterion, RQLValueSetVariableCriterionParser criterion, boolean opened);

    void addDefaultCriterion(RQLIdentifierCriterionParser idCriterion, RQLValueSetVariableCriterionParser criterion, boolean opened);

    void showResults(EntitiesResultDto results, int offset, int limit);

    void searchEnabled(boolean enabled);

    void searchUnavailable();

    void triggerSearch();
  }

  private class VariableCriterionProcessor implements ResourceCallback<VariableDto> {
    private final String datasource;
    private final String table;
    private RQLValueSetVariableCriterionParser criterion;
    private final List<RQLValueSetVariableCriterionParser> criterions;
    private final int currentIdx;
    private final boolean opened;

    public VariableCriterionProcessor(String datasource, String table) {
      this.criterion = null;
      this.criterions = null;
      this.currentIdx = -1;
      this.datasource = datasource;
      this.table = table;
      this.opened = true;
    }

    public VariableCriterionProcessor(List<RQLValueSetVariableCriterionParser> criterions, int idx) {
      this.criterion = criterions.get(idx);
      this.criterions = criterions;
      this.currentIdx = idx;
      this.datasource = criterion.getDatasourceName();
      this.table = criterion.getTableName();
      this.opened = false;
    }

    @Override
    public void onResource(Response response, VariableDto resource) {
      if (criterion != null) {
        criterion.setVariable(resource);
        addVariableCriterion();
      } else addVariableCriterion(resource);
    }

    private void addVariableCriterion(VariableDto variable) {
      this.criterion = new RQLValueSetVariableCriterionParser(datasource, table, variable);
      addVariableCriterion();
    }

    private void addVariableCriterion() {
      final VariableDtoNature nature = criterion.getNature();
      final RQLIdentifierCriterionParser idCriterion = new RQLIdentifierCriterionParser(idQuery);
      if (nature == CONTINUOUS || nature == CATEGORICAL) {
        // Filter for Categorical variable OR Numerical variable
        ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(
            UriBuilders.DATASOURCE_TABLE_FACET_VARIABLE_SEARCH.create()
                .build(datasource, table, criterion.getVariableName()))
            .withCallback(new ResourceCallback<QueryResultDto>() {
              @Override
              public void onResource(Response response, QueryResultDto resource) {
                if (nature == CONTINUOUS)
                  getView().addNumericalCriterion(idCriterion, criterion, resource, opened);
                else
                  getView().addCategoricalCriterion(idCriterion, criterion, resource, opened);
                renderNextVariableCriterionOrSearch();
              }
            }).get().send();
      } else {
        if (nature == TEMPORAL)
          getView().addDateCriterion(idCriterion, criterion, opened);
        else
          getView().addDefaultCriterion(idCriterion, criterion, opened);
        renderNextVariableCriterionOrSearch();
      }
    }

    private void renderNextVariableCriterionOrSearch() {
      if (criterions == null) getView().triggerSearch();
      else if (currentIdx < criterions.size() - 1) renderVariableCriterion(criterions, currentIdx + 1);
      else searchSelected();
    }
  }

}
