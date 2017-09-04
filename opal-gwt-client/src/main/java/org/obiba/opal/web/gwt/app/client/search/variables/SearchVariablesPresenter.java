/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.google.common.base.Strings;
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
import org.obiba.opal.web.gwt.app.client.cart.event.CartAddVariableEvent;
import org.obiba.opal.web.gwt.app.client.cart.event.CartAddVariablesEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.search.ItemFieldsDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchVariablesPresenter extends Presenter<SearchVariablesPresenter.Display, SearchVariablesPresenter.Proxy>
    implements HasPageTitle, SearchVariablesUiHandlers {

  private static final int QUERY_LIMIT = Table.DEFAULT_PAGESIZE;

  private static final int QUERY_ALL_LIMIT = 10000;

  private final Translations translations;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final PlaceManager placeManager;

  private String query;

  private String rqlQuery;

  private int offset = 0;

  private List<String> locales = Lists.newArrayList();

  private boolean viewInitialized;

  @Inject
  public SearchVariablesPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
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
    return translations.pageSearchVariablesTitle();
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    initializeView();
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    query = request.getParameter(ParameterTokens.TOKEN_QUERY, null);
    rqlQuery = request.getParameter(ParameterTokens.TOKEN_RQL_QUERY, null);
    offset = Integer.parseInt(request.getParameter(ParameterTokens.TOKEN_OFFSET, "0"));
    getView().setQuery(query);
    if (hasQuery()) query();
    else if (viewInitialized && hasRQLQuery()) {
      getView().reset();
      searchProvidedRQLQueryIfReady();
    } else getView().reset();
  }

  @Override
  public void onSearchRange(String query, String rqlQuery, int offset) {
    this.query = query;
    this.rqlQuery = rqlQuery;
    this.offset = offset;
    if (hasQuery()) query();
    else {
      getView().reset();
      updateHistory();
    }
  }

  @Override
  public void onSearchAll(String query, QueryResultHandler handler) {
    queryAll(query, 0, handler);
  }

  @Override
  public void onClear() {
    query = null;
    rqlQuery = null;
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .without(ParameterTokens.TOKEN_QUERY)
        .without(ParameterTokens.TOKEN_RQL_QUERY)
        .without(ParameterTokens.TOKEN_OFFSET);
    placeManager.updateHistory(builder.build(), true);
  }

  @Override
  public void onFacet(String field, int size, FacetHandler handler) {
    facet(field, size, handler);
  }

  @Override
  public void onAddToCart(List<ItemResultDto> selectedItems) {
    if (selectedItems.isEmpty()) return;
    Map<String, List<String>> namesByType = Maps.newHashMap();
    for (ItemResultDto item : selectedItems) {
      MagmaPath.Parser parser = MagmaPath.Parser.parse(item.getIdentifier());
      ItemFieldsDto fields = (ItemFieldsDto) item.getExtension("Search.ItemFieldsDto.item");
      String entityType = "Participant";
      for (EntryDto entry : JsArrays.toIterable(fields.getFieldsArray())) {
        if ("entityType".equals(entry.getKey())) {
          entityType = entry.getValue();
          break;
        }
      }
      if (!namesByType.containsKey(entityType))
        namesByType.put(entityType, new ArrayList<String>());
      namesByType.get(entityType).add(MagmaPath.Builder.datasource(parser.getDatasource()).table(parser.getTable()).variable(parser.getVariable()).build());
    }
    for (String type : namesByType.keySet())
      fireEvent(new CartAddVariablesEvent(type, namesByType.get(type)));
  }

  //
  // Private methods
  //

  private boolean hasQuery() {
    return !Strings.isNullOrEmpty(query);
  }

  private boolean hasRQLQuery() {
    return !Strings.isNullOrEmpty(rqlQuery);
  }

  private UriBuilder createUriBuilder(String query, int offset, int limit) {
    UriBuilder ub = UriBuilders.DATASOURCES_VARIABLES_SEARCH.create()//
        .query("query", query)//
        .query("offset", "" + offset)//
        .query("limit", "" + limit)//
        .query("sort", "name")//
        .query("field", "name", "field", "datasource", "field", "table", "field", "label", "field", "label-en", "field", "entityType");

    for (String locale : locales) {
      if (!"en".equals(locale))
        ub.query("field", "label-" + locale);
    }
    return ub;
  }

  private void query() {
    UriBuilder ub = createUriBuilder(query, offset, QUERY_LIMIT);
    // Get candidates from search words.
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clearResults();
            fireEvent(NotificationEvent.newBuilder().warn("MalformedSearchQuery").build());
          }
        }, Response.SC_BAD_REQUEST)//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clearResults();
            fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
          }
        }, Response.SC_SERVICE_UNAVAILABLE)//
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(Response response, QueryResultDto resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              getView().showResults(query, offset, resource);
              updateHistory();
            }
          }
        })//
        .send();
  }

  private void queryAll(final String query, final int allOffset, final QueryResultHandler handler) {
    UriBuilder ub = createUriBuilder(query, allOffset, QUERY_ALL_LIMIT);
    final int nextOffset = allOffset + QUERY_ALL_LIMIT;
    // Get candidates from search words.
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().warn("MalformedSearchQuery").build());
          }
        }, Response.SC_BAD_REQUEST)//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
          }
        }, Response.SC_SERVICE_UNAVAILABLE)//
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(Response response, QueryResultDto resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              handler.onQueryResult(query, allOffset, resource);
              if (nextOffset<resource.getTotalHits())
                queryAll(query, nextOffset, handler);
            }
          }
        })//
        .send();
  }

  private void facet(String field, int size, final FacetHandler handler) {
    UriBuilder ub = UriBuilders.DATASOURCES_VARIABLES_SEARCH.create()//
        .query("query", "*")//
        .query("offset", "0")//
        .query("limit", "0")//
        .query("facet", field + ":" + size);

    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clearResults();
            fireEvent(NotificationEvent.newBuilder().warn("MalformedSearchQuery").build());
          }
        }, Response.SC_BAD_REQUEST)//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clearResults();
            fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
          }
        }, Response.SC_SERVICE_UNAVAILABLE)//
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(Response response, QueryResultDto resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              handler.onResult(resource.getFacetsArray().get(0));
            }
          }
        })//
        .send();
  }

  /**
   * Cascade locales, taxonomies and tables init.
   */
  private void initializeView() {
    if (viewInitialized) return;
    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
        .withCallback(new ResourceCallback<GeneralConf>() {
          @Override
          public void onResource(Response response, GeneralConf resource) {
            locales.clear();
            for (int i = 0; i < resource.getLanguagesArray().length(); i++) {
              locales.add(resource.getLanguages(i));
            }
            // cascade taxonomies and tables rendering
            renderTaxonomies();
          }
        }).get().send();
  }

  private void renderTaxonomies() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build()).get()
        .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
          @Override
          public void onResource(Response response, JsArray<TaxonomyDto> resource) {
            getView().setTaxonomies(JsArrays.toList(resource));
            renderTables();
          }
        }).send();
  }

  private void renderTables() {
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_TABLES.create().build())
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            getView().setTables(JsArrays.toList(resource));
            viewInitialized = true;
            searchProvidedRQLQueryIfReady();
          }
        }).get().send();
  }

  private void searchProvidedRQLQueryIfReady() {
    if (viewInitialized && hasRQLQuery()) getView().setRQLQuery(rqlQuery);
  }

  private void updateHistory() {
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .with(ParameterTokens.TOKEN_OFFSET, "" + offset);
    if (Strings.isNullOrEmpty(rqlQuery)) {
      builder.with(ParameterTokens.TOKEN_QUERY, query);
      builder.without(ParameterTokens.TOKEN_RQL_QUERY);
    } else {
      builder.with(ParameterTokens.TOKEN_RQL_QUERY, rqlQuery);
      builder.without(ParameterTokens.TOKEN_QUERY);
    }
    placeManager.updateHistory(builder.build(), true);
  }

  @ProxyStandard
  @NameToken(Places.SEARCH_VARIABLES)
  public interface Proxy extends ProxyPlace<SearchVariablesPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<SearchVariablesUiHandlers> {

    void setTaxonomies(List<TaxonomyDto> taxonomies);

    void setTables(List<TableDto> tables);

    void setQuery(String query);

    void setRQLQuery(String rqlQuery);

    void showResults(String rqlQuery, int offset, QueryResultDto results);

    void clearResults();

    void reset();
  }

  public interface QueryResultHandler {
    void onQueryResult(String rqlQuery, int offset, QueryResultDto results);
  }

}
