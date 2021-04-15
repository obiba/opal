/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.search.event.SearchEntityEvent;
import org.obiba.opal.web.gwt.app.client.support.JSErrorNotificationEventBuilder;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.gwt.app.client.support.VariablesFilter;
import org.obiba.opal.web.gwt.app.client.ui.RQLValueSetVariableCriterionParser;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.search.ValueSetsResultDto;
import org.obiba.opal.web.model.client.search.VariableItemDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.http.client.Response.*;
import static org.obiba.opal.web.gwt.app.client.support.VariableDtoNature.*;

public class ValuesTablePresenter extends PresenterWidget<ValuesTablePresenter.Display>
    implements ValuesTableUiHandlers {

  private TableDto originalTable;

  private final PlaceManager placeManager;

  private Translations translations;

  private final ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider;

  private String currentVariablesFilterSelect = "";

  private boolean isIndexed;

  private Request variablesRequest;

  private Request valuesRequest;

  @Inject
  public ValuesTablePresenter(Display display, EventBus eventBus, PlaceManager placeManager,
                              ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider, Translations translations) {
    super(eventBus, display);
    this.placeManager = placeManager;
    this.translations = translations;
    this.valueSequencePopupProvider = valueSequencePopupProvider.setContainer(this);

    getView().setUiHandlers(this);
  }

  public void setTable(TableDto table, VariableDto variable) {
    originalTable = table;
    getView().setTable(table);
    JsArray<VariableDto> variables = JsArray.createArray().cast();
    variables.push(variable);
    getView().setVariables(variables);
    fetchIndexStatus();
  }

  public void setTable(final TableDto table) {
    if (originalTable == null || !originalTable.getLink().equals(table.getLink())) {
      originalTable = table;
      getView().clearTable();
      getView().setTable(table);
      getView().setVariableLabelFieldUpdater(new ValueUpdater<String>() {
        @Override
        public void update(String value) {
          placeManager
              .revealPlace(ProjectPlacesHelper.getVariablePlace(table.getDatasourceName(), table.getName(), value));
        }
      });
    } else if (table.hasValueSetCount()) {
      originalTable = table;
      getView().setTable(table);
    }
  }

  /**
   * When showing values tab from the TablePresenter, we have to copy the filter that
   * may have been set on the TablePresenter
   *
   * @param select
   */
  public void updateValuesDisplay(String select) {
    if (!select.isEmpty()) getView().getFilter().setText(select);
    updateVariables(select);
    currentVariablesFilterSelect = "";
    fetchIndexStatus();
  }

  public void setFilter(String filter) {
    getView().setFilterText(filter);
  }

  @Override
  protected void onReveal() {
    getView().addEntitySearchHandler(new EntitySearchHandlerImpl());
  }

  public void setViewMode(ViewMode mode) {
    getView().setViewMode(mode);
  }

  // Filter with Match instead of Es query
  private class VariablesDtoResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private final String link;

    private VariablesDtoResourceCallback(String link) {
      this.link = link;
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      if (link.equals(originalTable.getLink())) {
        JsArray<VariableDto> variables = resource == null
            ? JsArray.createArray().<JsArray<VariableDto>>cast()
            : resource;

        getView().setVariables(variables);
      }
    }
  }

  @Override
  public void onVariableFilter(String variableName) {
    // Fetch variable and show its filter
    ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(originalTable.getDatasourceName(), originalTable.getName(), variableName))
        .withCallback(new VariableFilterResourceCallback(originalTable, variableName)).get().send();
  }

  @Override
  public void onSearchValueSets(final List<VariableDto> variables, List<String> query, final int offset, final int limit) {
    setCurrentVariablesFilterSelect(variables);
    ResourceRequestBuilderFactory.<ValueSetsResultDto>newBuilder()
        .forResource(UriBuilders.DATASOURCE_TABLE_VALUESETS_SEARCH.create()//
            .query("query", Joiner.on(",").join(query))//
            .query("select", currentVariablesFilterSelect)//
            .query("offset", "" + offset)//
            .query("limit", "" + limit)//
            .build(originalTable.getDatasourceName(), originalTable.getName()))
        .withCallback(new ResourceCallback<ValueSetsResultDto>() {
          @Override
          public void onResource(Response response, ValueSetsResultDto resource) {
            // TODO make one call only
            getView().populateValues(offset, resource.getTotalHits(), resource.getValueSets());
          }
        })//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());

            // Do not show "SearchQueryIsInvalid" errors: the query might be invalid because it is being typed in
            if (errorDto != null && !"SearchQueryIsInvalid".equals(errorDto.getStatus())) {
              fireEvent(NotificationEvent.newBuilder().error(TranslationsUtils
                  .replaceArguments(translations.userMessageMap().get(errorDto.getStatus()),
                      errorDto.getArgumentsArray())).build());
            }
          }
        }, Response.SC_BAD_REQUEST)//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            onRequestValueSets(variables, offset, limit);
          }
        }, Response.SC_SERVICE_UNAVAILABLE)
        .get().send();
  }

  @Override
  public void onSearchEntities(String idQuery, List<String> queries) {
    PlaceRequest request = ProjectPlacesHelper.getSearchEntitiesPlace(originalTable.getEntityType(), idQuery, queries);
    placeManager.revealPlaceHierarchy(Lists.newArrayList(PlaceRequestHelper.createRequestBuilder(Places.SEARCH).build(), request));
  }

  private void fetchIndexStatus() {
    // Show Values Filter when ES is enabled
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder().forResource(
        UriBuilders.DATASOURCE_TABLE_INDEX.create().build(originalTable.getDatasourceName(), originalTable.getName()))
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setSearchAvailable(false);
            isIndexed = false;
          }
        }, SC_INTERNAL_SERVER_ERROR, SC_FORBIDDEN, SC_NOT_FOUND, SC_SERVICE_UNAVAILABLE)//
        .withCallback(new ResourceCallback<JsArray<TableIndexStatusDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {
            TableIndexStatusDto statusDto = TableIndexStatusDto.get(JsArrays.toSafeArray(resource));
            isIndexed = statusDto.getStatus().getName().equals(TableIndexationStatus.UPTODATE.getName());
            getView().setSearchAvailable(isIndexed);
          }
        }).get().send();
  }

  private class VariablesResourceCallback implements ResourceCallback<QueryResultDto> {

    private final String link;

    private VariablesResourceCallback(String link) {
      this.link = link;
    }

    @Override
    public void onResource(Response response, QueryResultDto resource) {

      if (link.equals(originalTable.getLink())) {

        JsArray<VariableDto> variables = JsArrays.create();
        QueryResultDto resultDto = JsonUtils.unsafeEval(response.getText());
        for (int i = 0; i < resultDto.getTotalHits(); i++) {
          VariableItemDto varDto = (VariableItemDto) resultDto.getHitsArray().get(i)
              .getExtension(VariableItemDto.ItemResultDtoExtensions.item);
          variables.push(varDto.getVariable());
        }
        getView().setVariables(variables);
      }
    }
  }

  private class ValueSetsResourceCallback implements ResourceCallback<ValueSetsDto> {

    private final int offset;

    private final String link;

    private ValueSetsResourceCallback(int offset, String link) {
      this.offset = offset;
      this.link = link;
    }

    @Override
    public void onResource(Response response, ValueSetsDto resource) {
      if (link.equals(originalTable.getLink())) {
        getView().populateValues(offset, resource == null ? 0 : originalTable.getValueSetCount(), resource == null ? ValueSetsDto.create() : resource);
      }
    }
  }

  private class BadRequestCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      notifyError(response);
    }

    @SuppressWarnings("unchecked")
    protected void notifyError(Response response) {
      NotificationEvent notificationEvent = new JSErrorNotificationEventBuilder()
          .build((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
      fireEvent(notificationEvent);
    }
  }

  public interface EntitySearchHandler {
    void onSearch(String identifier);
  }

  private class EntitySearchHandlerImpl implements EntitySearchHandler {

    @Override
    public void onSearch(final String identifier) {
      UriBuilder uriBuilder = UriBuilder.create().segment("entity", identifier, "type", originalTable.getEntityType());
      ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
          .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(NotificationEvent.newBuilder().error("EntityIdentifierNotFound")
                  .args(originalTable.getEntityType(), identifier, originalTable.getName()).build());
            }
          }).withCallback(Response.SC_OK, new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          requestEntitySearch(originalTable.getEntityType(), identifier);
        }
      }).send();
    }
  }

  @Override
  public void onRequestValueSets(List<VariableDto> variables, int offset, int limit) {
    StringBuilder link = getLinkBuilder(offset, limit);
    if (originalTable.getVariableCount() > variables.size()) {
      link.append("&select=");
      setCurrentVariablesFilterSelect(variables);
      link.append(currentVariablesFilterSelect);
    } else {
      currentVariablesFilterSelect = ""; //we need to clear currentVariablesFilterSelect, as it will be used later on
    }
    doRequest(offset, link.toString());
  }

  private void setCurrentVariablesFilterSelect(List<VariableDto> variables) {
    StringBuilder script = new StringBuilder("name().lowerCase().matches(/");
    if (variables.isEmpty()) {
      script.append("^$");
    } else {
      for (int i = 0; i < variables.size(); i++) {
        if (i > 0) script.append("|");
        script.append("^").append(escape(variables.get(i).getName().toLowerCase())).append("$");
      }
    }
    script.append("/)");
    currentVariablesFilterSelect = script.toString();
  }

  @Override
  public void onRequestValueSets(String filter, int offset, int limit, boolean exactMatch) {

    new VariablesFilter() {
      @Override
      public void beforeVariableResourceCallback() {
        // nothing
      }

      @Override
      public void onVariableResourceCallback() {
        List<VariableDto> variables = new ArrayList<VariableDto>();
        for (VariableDto result : results) {
          variables.add(result);
        }

        onRequestValueSets(variables, offset, limit);
      }
    }//
        .withQuery(filter)//
        .withVariable(true)//
        .withLimit(limit)//
        .withOffset(offset)//
        .isExactMatch(exactMatch).filter(getEventBus(), originalTable);
  }

  private String escape(String filter) {
    return filter.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
  }

  private void doRequest(int offset, String link) {
    if (valuesRequest != null) {
      valuesRequest.cancel();
      valuesRequest = null;
    }
    valuesRequest = ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder().forResource(link) //
        .get() //
        .withCallback(new ValueSetsResourceCallback(offset, originalTable.getLink())) //
        .withCallback(SC_BAD_REQUEST, new BadRequestCallback()).send();
  }

  private StringBuilder getLinkBuilder(int offset, int limit) {
    return new StringBuilder(originalTable.getLink()).append("/valueSets").append("?offset=").append(offset)
        .append("&limit=").append(limit);
  }

  @Override
  public void requestBinaryValue(VariableDto variable, String entityIdentifier) {
    fireEvent(new FileDownloadRequestEvent(
        originalTable.getLink() + "/valueSet/" + entityIdentifier + "/variable/" + variable.getName() + "/value"));
  }

  @Override
  public void requestGeoValue(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value) {
    fireEvent(new GeoValueDisplayEvent(variable, entityIdentifier, value));
  }

  @Override
  public void requestValueSequence(VariableDto variable, String entityIdentifier) {
    ValueSequencePopupPresenter valueSequencePopupPresenter = valueSequencePopupProvider.get();
    valueSequencePopupPresenter.initialize(originalTable, variable, entityIdentifier, false);
  }

  @Override
  public void requestEntitySearch(String entityType, String entityId) {
    String tableRef = null;
    if (originalTable.getEntityType().equals(entityType))
      tableRef = originalTable.getDatasourceName() + "." + originalTable.getName();
    fireEvent(new SearchEntityEvent(entityType, entityId, tableRef));
  }

  @Override
  public void updateVariables(String select) {
    final String query = select.isEmpty() ? "*" : select;

    // case the variable dto has not the variable count yet
    int limit = originalTable.hasVariableCount() ? originalTable.getVariableCount() : Integer.MAX_VALUE;

    String resource = UriBuilders.DATASOURCE_TABLE_VARIABLES_SEARCH.create()//
        .query("query", query)//
        .query("limit", String.valueOf(limit))//
        .query("sortField", "index") //
        .query("sortDir", "ASC") //
        .query("variable", "true")//
        .build(originalTable.getDatasourceName(), originalTable.getName());

    if (variablesRequest != null) {
      variablesRequest.cancel();
      variablesRequest = null;
    }
    getView().clearTable();

    variablesRequest = ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(resource).get()//
        .withCallback(new VariablesResourceCallback(originalTable.getLink()))
        .withCallback(Response.SC_BAD_REQUEST, new BadRequestCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            notifyError(response);
            setTable(originalTable);
          }
        }).withCallback(new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            // Use the previous way of filtering variables
            String link = originalTable.getLink() + "/variables";

            if (!"*".equals(query)) {
              currentVariablesFilterSelect = getVariablesFilterSelect(query);
              link += "?script=" + currentVariablesFilterSelect;
            }
            if (variablesRequest != null) {
              variablesRequest.cancel();
              variablesRequest = null;
            }
            getView().clearTable();
            variablesRequest = ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(link)
                .get()//
                .withCallback(new VariablesDtoResourceCallback(originalTable.getLink()))
                .withCallback(Response.SC_BAD_REQUEST, new BadRequestCallback() {
                  @Override
                  public void onResponseCode(Request request, Response response) {
                    notifyError(response);
                    setTable(originalTable);
                    updateValuesDisplay(currentVariablesFilterSelect);
                  }
                }).send();
          }

          private String getVariablesFilterSelect(String filter) {
            String regex = filter.replaceAll("/", "\\\\/").toLowerCase();
            return "name().lowerCase().matches(/" + regex + "/)";
          }

        }, Response.SC_SERVICE_UNAVAILABLE, Response.SC_NOT_FOUND, Response.SC_BAD_REQUEST)//
        .send();
  }

  public interface Display extends View, HasUiHandlers<ValuesTableUiHandlers> {

    void setTable(TableDto table);

    void clearTable();

    void setVariables(JsArray<VariableDto> variables);

    void addEntitySearchHandler(EntitySearchHandler handler);

    void setViewMode(ViewMode mode);

    void setVariableLabelFieldUpdater(ValueUpdater<String> updater);

    void setFilterText(String filter);

    String getFilterText();

    TextBoxClearable getFilter();

    void populateValues(int offset, int total, ValueSetsDto resource);

    void addCategoricalCriterion(RQLValueSetVariableCriterionParser criterion, QueryResultDto facet);

    void addNumericalCriterion(RQLValueSetVariableCriterionParser criterion, QueryResultDto facet);

    void addDateCriterion(RQLValueSetVariableCriterionParser criterion);

    void addDefaultCriterion(RQLValueSetVariableCriterionParser criterion);

    String getQueryString();

    void setSearchAvailable(boolean available);

    int getPageSize();

  }

  public enum ViewMode {
    DETAILED_MODE,
    SIMPLE_MODE
  }


  public interface ValueSetsProvider {
    void populateValues(int offset, int total, ValueSetsDto valueSets);
  }

  public interface EntitySelectionHandler {

    void onEntitySelection(String entityType, String entityId);

  }

  private class VariableFilterResourceCallback implements ResourceCallback<VariableDto> {

    private final TableDto table;

    private final String variableName;

    VariableFilterResourceCallback(TableDto table, String variableName) {
      this.table = table;
      this.variableName = variableName;
    }

    @Override
    public void onResource(Response response, VariableDto resource) {
      // Fetch facets
      if (response.getStatusCode() == Response.SC_OK) {
        final RQLValueSetVariableCriterionParser criterion = new RQLValueSetVariableCriterionParser(table.getDatasourceName(), table.getName(), resource);
        final VariableDtoNature nature = criterion.getNature();
        if (nature == CONTINUOUS || nature == CATEGORICAL) {
          // Filter for Categorical variable OR Numerical variable
          ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(
              UriBuilders.DATASOURCE_TABLE_FACET_VARIABLE_SEARCH.create()
                  .build(table.getDatasourceName(), table.getName(), variableName))
              .withCallback(new ResourceCallback<QueryResultDto>() {
                @Override
                public void onResource(Response response, QueryResultDto resource) {
                  if (nature == CONTINUOUS) getView().addNumericalCriterion(criterion, resource);
                  else getView().addCategoricalCriterion(criterion, resource);
                }
              }).get().send();
        } else if (nature == TEMPORAL)
          getView().addDateCriterion(criterion);
        else
          getView().addDefaultCriterion(criterion);
      }
    }
  }
}
