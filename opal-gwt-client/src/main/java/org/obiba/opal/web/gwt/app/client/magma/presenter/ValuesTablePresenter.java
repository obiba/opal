/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.JSErrorNotificationEventBuilder;
import org.obiba.opal.web.gwt.app.client.support.VariablesFilter;
import org.obiba.opal.web.gwt.app.client.ui.CategoricalCriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.CriterionPanel;
import org.obiba.opal.web.gwt.app.client.ui.DateTimeCriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.DefaultCriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.NumericalCriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.OpalMap;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.search.ValueSetsResultDto;
import org.obiba.opal.web.model.client.search.VariableItemDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_SERVICE_UNAVAILABLE;

public class ValuesTablePresenter extends PresenterWidget<ValuesTablePresenter.Display>
    implements ValuesTableUiHandlers {

  private TableDto originalTable;

  private DataFetcher fetcher;

  private final PlaceManager placeManager;

  private final ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider;

  private final ModalProvider<EntityModalPresenter> entityModalProvider;

  private OpalMap opalMap;

  private String currentVariablesFilterSelect = "";

  @Inject
  public ValuesTablePresenter(Display display, EventBus eventBus, PlaceManager placeManager,
      ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider,
      ModalProvider<EntityModalPresenter> entityModalProvider) {
    super(eventBus, display);
    this.placeManager = placeManager;
    this.valueSequencePopupProvider = valueSequencePopupProvider.setContainer(this);
    this.entityModalProvider = entityModalProvider.setContainer(this);

    getView().setUiHandlers(this);
  }

  public void setTable(TableDto table) {
    setTable(table, "");
  }

  public void setTable(TableDto table, VariableDto variable) {
    originalTable = table;

    getView().setTable(table);
    JsArray<VariableDto> variables = JsArray.createArray().cast();
    variables.push(variable);
    getView().setVariables(variables);
    currentVariablesFilterSelect = "";
    fetchIndexSchema();
  }

  public void setTable(final TableDto table, String select) {
    originalTable = table;

    getView().clearTable();
    getView().getFiltersPanel().clear();
    getView().setTable(table);
    getView().setVariableLabelFieldUpdater(new ValueUpdater<String>() {
      @Override
      public void update(String value) {
        placeManager
            .revealPlace(ProjectPlacesHelper.getVariablePlace(table.getDatasourceName(), table.getName(), value));
      }
    });
    fetcher.updateVariables(select);
    currentVariablesFilterSelect = "";
    fetchIndexSchema();
  }

  public void setFilter(String filter) {
    getView().setFilterText(filter);
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().setValueSetsFetcher(fetcher = new DataFetcherImpl());
    getView().addEntitySearchHandler(new EntitySearchHandlerImpl());

    registerHandler(getView().getFilter().getClear().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fetcher.updateVariables(getView().getFilter().getTextBox().getText());
      }
    }));
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
      if(link.equals(originalTable.getLink())) {
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
        .withCallback(new VariableFilterResourceCallback(variableName)).get().send();
  }

  private boolean hasValueSetsFilter() {
    return getView().getValuesFilterGroup().isVisible() && getView().getFiltersPanel().getWidgetCount() > 0;
  }

  private void applyAllValueSetsFilter() {
    applyAllValueSetsFilter(0);
  }

  private void applyAllValueSetsFilter(final int offset) {
    if(getView().getValuesFilterGroup().isVisible()) {
      String filters = getQueryString();

      ResourceRequestBuilderFactory.<ValueSetsResultDto>newBuilder()
          .forResource(UriBuilders.DATASOURCE_TABLE_VALUESETS_SEARCH.create()//
              .query("query", filters)//
              .query("select", currentVariablesFilterSelect)//
              .query("offset", String.valueOf(offset))//
              .query("limit", String.valueOf(getView().getPageSize()))//
              .build(originalTable.getDatasourceName(), originalTable.getName()))
          .withCallback(new ResourceCallback<ValueSetsResultDto>() {
            @Override
            public void onResource(Response response, ValueSetsResultDto resource) {

              getView().populateValues(offset, resource.getValueSets());
              getView().setRowCount(resource.getTotalHits());
            }
          })//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error("ESQueryBadRequest").build());
            }
          }, Response.SC_BAD_REQUEST)//
          .get().send();
    }
  }

  private String getQueryString() {
    // Get all Filters
    FlowPanel filtersPanel = getView().getFiltersPanel();

    Collection<String> filters = new ArrayList<String>();
    for(int i = 0; i < filtersPanel.getWidgetCount(); i++) {
      if(filtersPanel.getWidget(i) instanceof CriterionPanel) {
        String queryString = ((CriterionPanel) filtersPanel.getWidget(i)).getQueryString();
        if(!Strings.isNullOrEmpty(queryString)) filters.add(queryString);
      }
    }

    return filters.isEmpty() ? "*" : Joiner.on(" AND ").join(filters);
  }

  private void fetchIndexSchema() {
    // Show Values Filter when ES is enabled
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder().forResource(
        UriBuilders.DATASOURCE_TABLE_INDEX.create().build(originalTable.getDatasourceName(), originalTable.getName()))
        .get().withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        // Unavailable
        getView().getValuesFilterGroup().setVisible(false);
      }
    }, SC_INTERNAL_SERVER_ERROR, SC_FORBIDDEN, SC_NOT_FOUND, SC_SERVICE_UNAVAILABLE)
        .withCallback(new ResourceCallback<JsArray<TableIndexStatusDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {
            TableIndexStatusDto statusDto = TableIndexStatusDto.get(JsArrays.toSafeArray(resource));
            boolean isIndexed = statusDto.getStatus().getName().equals(TableIndexationStatus.UPTODATE.getName());
            getView().getValuesFilterGroup().setVisible(isIndexed);

            if(isIndexed) {
              // Fetch variable-field mapping for ES queries
              ResourceRequestBuilderFactory.<OpalMap>newBuilder().forResource(
                  UriBuilders.DATASOURCE_TABLE_INDEX_SCHEMA.create()
                      .build(originalTable.getDatasourceName(), originalTable.getName()))
                  .withCallback(new ResourceCallback<OpalMap>() {
                    @Override
                    public void onResource(Response response, OpalMap resource) {
                      if(response.getStatusCode() == Response.SC_OK) {
                        opalMap = resource;
                      }
                    }
                  }).get().send();
            }
          }
        }).send();
  }

  private class VariablesResourceCallback implements ResourceCallback<QueryResultDto> {

    private final String link;

    private VariablesResourceCallback(String link) {
      this.link = link;
    }

    @Override
    public void onResource(Response response, QueryResultDto resource) {

      if(link.equals(originalTable.getLink())) {

        JsArray<VariableDto> variables = JsArrays.create();
        QueryResultDto resultDto = JsonUtils.unsafeEval(response.getText());
        for(int i = 0; i < resultDto.getTotalHits(); i++) {
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
      if(link.equals(originalTable.getLink())) {
        if(hasValueSetsFilter()) {
          applyAllValueSetsFilter(offset);
        } else {
          getView().populateValues(offset, resource == null ? ValueSetsDto.create() : resource);
        }
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
      getEventBus().fireEvent(notificationEvent);
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
          fetcher.requestEntityDialog(originalTable.getEntityType(), identifier);
        }
      }).send();
    }
  }

  private class DataFetcherImpl implements DataFetcher {

    private Request variablesRequest;

    private Request valuesRequest;

    @Override
    public void request(List<VariableDto> variables, int offset, int limit) {
      StringBuilder link = getLinkBuilder(offset, limit);
      if(originalTable.getVariableCount() > variables.size()) {
        link.append("&select=");
        StringBuilder script = new StringBuilder("name().lowerCase().matches(/");
        if(variables.isEmpty()) {
          script.append("^$");
        } else {
          for(int i = 0; i < variables.size(); i++) {
            if(i > 0) script.append("|");
            script.append("^").append(escape(variables.get(i).getName().toLowerCase())).append("$");
          }
        }
        script.append("/)");
        currentVariablesFilterSelect = script.toString();
        link.append(currentVariablesFilterSelect);
      }
      doRequest(offset, link.toString());
    }

    @Override
    public void request(String filter, int offset, int limit, boolean exactMatch) {

      new VariablesFilter() {
        @Override
        public void beforeVariableResourceCallback() {
          // nothing
        }

        @Override
        public void onVariableResourceCallback() {
          List<VariableDto> variables = new ArrayList<VariableDto>();
          for(VariableDto result : results) {
            variables.add(result);
          }

          request(variables, offset, limit);
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
      if(valuesRequest != null) {
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
      StringBuilder link = new StringBuilder(originalTable.getLink());
      link.append("/valueSet/").append(entityIdentifier).append("/variable/").append(variable.getName())
          .append("/value");
      getEventBus().fireEvent(new FileDownloadRequestEvent(link.toString()));
    }

    @Override
    public void requestGeoValue(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value) {
      getEventBus().fireEvent(new GeoValueDisplayEvent(variable, entityIdentifier, value));
    }

    @Override
    public void requestValueSequence(VariableDto variable, String entityIdentifier) {
      ValueSequencePopupPresenter valueSequencePopupPresenter = valueSequencePopupProvider.get();
      valueSequencePopupPresenter.initialize(originalTable, variable, entityIdentifier, false);
    }

    @Override
    public void requestEntityDialog(String entityType, String entityId) {
      EntityModalPresenter entityModalPresenter = entityModalProvider.get();
      entityModalPresenter.initialize(originalTable, entityType, entityId, getView().getFilterText());
    }

    @Override
    public void updateVariables(String select) {
      final String query = select.isEmpty() ? "*" : select;

      String resource = UriBuilders.DATASOURCE_TABLE_VARIABLES_SEARCH.create()//
          .query("query", query)//
          .query("limit", String.valueOf(originalTable.getVariableCount()))//
          .query("variable", "true")//
          .build(originalTable.getDatasourceName(), originalTable.getName());

      if(variablesRequest != null) {
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

              if(!"*".equals(query)) {
                currentVariablesFilterSelect = getVariablesFilterSelect(query);
                link += "?script=" + currentVariablesFilterSelect;
              }
              if(variablesRequest != null) {
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
  }

  public interface Display extends View, HasUiHandlers<ValuesTableUiHandlers> {

    void setTable(TableDto table);

    void clearTable();

    void setVariables(JsArray<VariableDto> variables);

    void setValueSetsFetcher(DataFetcher fetcher);

    void addEntitySearchHandler(EntitySearchHandler handler);

    void setViewMode(ViewMode mode);

    void setVariableLabelFieldUpdater(ValueUpdater<String> updater);

    void setFilterText(String filter);

    String getFilterText();

    TextBoxClearable getFilter();

    void populateValues(int offset, ValueSetsDto resource);

    void addVariableFilter(CriterionPanel criterion);

    FlowPanel getFiltersPanel();

    ControlGroup getValuesFilterGroup();

    int getPageSize();

    void setRowCount(int totalHits);
  }

  public enum ViewMode {
    DETAILED_MODE,
    SIMPLE_MODE
  }

  public interface DataFetcher {
    void request(List<VariableDto> variables, int offset, int limit);

    void request(String filter, int offset, int limit, boolean exactMatch);

    void requestBinaryValue(VariableDto variable, String entityIdentifier);

    void requestGeoValue(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value);

    void requestValueSequence(VariableDto variable, String entityIdentifier);

    void requestEntityDialog(String entityType, String entityId);

    void updateVariables(String select);
  }

  public interface ValueSetsProvider {
    void populateValues(int offset, ValueSetsDto valueSets);
  }

  public interface EntitySelectionHandler {

    void onEntitySelection(String entityType, String entityId);

  }

  private class EmptyNotEmptyFilterRequest implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {

      applyAllValueSetsFilter();
      // Maybe call fetcher.updateVariables and let it call applyFilterVariables
//      fetcher.updateVariables(getView().getFilterText());
    }
  }

  private class VariableFilterResourceCallback implements ResourceCallback<VariableDto> {

    private final String variableName;

    VariableFilterResourceCallback(String variableName) {
      this.variableName = variableName;
    }

    @Override
    public void onResource(Response response, VariableDto resource) {
      // Fetch facets
      if(response.getStatusCode() == Response.SC_OK) {

        List<String> keys = JsArrays.toList(opalMap.getKeysArray());
        String indexedFieldName = opalMap.getValues(keys.indexOf(resource.getName()));

        if(JsArrays.toSafeArray(resource.getCategoriesArray()).length() > 0 ||
            "integer".equals(resource.getValueType()) || "decimal".equals(resource.getValueType())) {
          // Filter for Categorical variable OR Numerical variable
          ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(
              UriBuilders.DATASOURCE_TABLE_FACET_VARIABLE_SEARCH.create()
                  .build(originalTable.getDatasourceName(), originalTable.getName(), variableName))
              .withCallback(new FacetVariableResourceCallback(resource, indexedFieldName)).get().send();

        } else {
          if("date".equals(resource.getValueType()) || "datetime".equals(resource.getValueType())) {
            addDateFilter(resource, indexedFieldName);
          } else {
            // Default filter variable
            addDefaultFilter(resource, indexedFieldName);
          }
        }
      }
    }

    private void addDefaultFilter(final VariableDto resource, final String indexedFieldName) {
      DefaultCriterionDropdown criterion = new DefaultCriterionDropdown(resource, indexedFieldName) {
        @Override
        public void doFilterValueSets() {
          applyAllValueSetsFilter();
        }
      };
      criterion.addChangeHandler(new EmptyNotEmptyFilterRequest());
      getView().addVariableFilter(new CriterionPanel(criterion));
    }

    private void addDateFilter(final VariableDto resource, final String indexedFieldName) {
      // DataTime filter
      DateTimeCriterionDropdown criterion = new DateTimeCriterionDropdown(resource, indexedFieldName) {
        @Override
        public void doFilterValueSets() {
          applyAllValueSetsFilter();
        }
      };
      criterion.addChangeHandler(new EmptyNotEmptyFilterRequest());
      getView().addVariableFilter(new CriterionPanel(criterion));
    }
  }

  private class FacetVariableResourceCallback implements ResourceCallback<QueryResultDto> {

    private final VariableDto variableDto;

    private final String fieldName;

    FacetVariableResourceCallback(VariableDto variableDto, String indexedFieldName) {
      this.variableDto = variableDto;
      fieldName = indexedFieldName;
    }

    @Override
    public void onResource(Response response, QueryResultDto resource) {
      if("integer".equals(variableDto.getValueType()) || "decimal".equals(variableDto.getValueType())) {
        addNumericalFilter(resource);
      } else {
        addCategoricalFilter(resource);
      }
    }

    private void addCategoricalFilter(final QueryResultDto resource) {
      // Categorical variable
      CategoricalCriterionDropdown criterion = new CategoricalCriterionDropdown(variableDto, fieldName, resource) {
        @Override
        public void doFilterValueSets() {
          applyAllValueSetsFilter();
        }
      };
      criterion.addChangeHandler(new EmptyNotEmptyFilterRequest());
      getView().addVariableFilter(new CriterionPanel(criterion));
    }

    private void addNumericalFilter(final QueryResultDto resource) {
      // Numerical variable
      NumericalCriterionDropdown criterion = new NumericalCriterionDropdown(variableDto, fieldName, resource) {
        @Override
        public void doFilterValueSets() {
          applyAllValueSetsFilter();
        }
      };

      criterion.addChangeHandler(new EmptyNotEmptyFilterRequest());
      getView().addVariableFilter(new CriterionPanel(criterion));
    }

  }
}
