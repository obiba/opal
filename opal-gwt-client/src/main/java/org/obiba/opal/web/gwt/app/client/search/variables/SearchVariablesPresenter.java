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
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
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
import org.obiba.opal.web.gwt.app.client.search.event.*;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;
import java.util.Map;

public class SearchVariablesPresenter extends Presenter<SearchVariablesPresenter.Display, SearchVariablesPresenter.Proxy>
    implements HasPageTitle, SearchVariablesUiHandlers {

  private final Translations translations;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final PlaceManager placeManager;

  private String query;

  private int offset = 0;

  private int limit = 50;

  private List<String> locales = Lists.newArrayList();

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
    renderTaxonomies();
    renderTables();
    initializeLocales();
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    query = request.getParameter(ParameterTokens.TOKEN_QUERY, null);
    offset = Integer.parseInt(request.getParameter(ParameterTokens.TOKEN_OFFSET, "0"));
    limit = Integer.parseInt(request.getParameter(ParameterTokens.TOKEN_LIMIT, "50"));
    getView().setQuery(query);
    if (!Strings.isNullOrEmpty(query)) query();
    else getView().reset();
  }

  @Override
  public void onSearch(String query) {
    onSearchRange(query, 0, 50);
  }

  @Override
  public void onSearchRange(String query, int offset, int limit) {
    //GWT.log("onSearchRange?q=" + query + "&o=" + offset + "&lm=" + limit);
    this.query = query;
    this.offset = offset;
    this.limit = limit;
    if (!Strings.isNullOrEmpty(query)) query();
    else {
      getView().reset();
      updateHistory();
    }
  }

  @Override
  public void onClear() {
    query = null;
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .without(ParameterTokens.TOKEN_QUERY)
        .without(ParameterTokens.TOKEN_OFFSET)
        .without(ParameterTokens.TOKEN_LIMIT);
    placeManager.updateHistory(builder.build(), true);
  }

  @Override
  public void onFacet(String field, int size, FacetHandler handler) {
    facet(field, size, handler);
  }

  //
  // Private methods
  //

  private void query() {
    UriBuilder ub = UriBuilders.DATASOURCES_VARIABLES_SEARCH.create()//
        .query("query", query)//
        .query("offset", "" + offset)//
        .query("limit", "" + limit)//
        .query("sort", "name")//
        .query("field", "name", "field", "datasource", "field", "table", "field", "label", "field", "label-en");

    for (String locale : locales) {
      if (!"en".equals(locale))
        ub.query("field", "label-" + locale);
    }

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
              getView().showResults(resource, offset, limit);
              updateHistory();
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

  private void renderTaxonomies() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build()).get()
        .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
          @Override
          public void onResource(Response response, JsArray<TaxonomyDto> resource) {
            getView().setTaxonomies(JsArrays.toList(resource));
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
          }
        }).get().send();
  }

  private void initializeLocales() {
    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
        .withCallback(new ResourceCallback<GeneralConf>() {
          @Override
          public void onResource(Response response, GeneralConf resource) {
            locales.clear();
            for(int i = 0; i < resource.getLanguagesArray().length(); i++) {
              locales.add(resource.getLanguages(i));
            }
          }
        }).get().send();
  }

  private void updateHistory() {
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .with(ParameterTokens.TOKEN_QUERY, query)
        .with(ParameterTokens.TOKEN_OFFSET, "" + offset)
        .with(ParameterTokens.TOKEN_LIMIT, "" + limit);
    placeManager.updateHistory(builder.build(), true);
  }

  @ProxyStandard
  @NameToken(Places.SEARCH_VARIABLES)
  public interface Proxy extends ProxyPlace<SearchVariablesPresenter> {}

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<SearchVariablesUiHandlers> {

    void setTaxonomies(List<TaxonomyDto> taxonomies);

    void setTables(List<TableDto> tables);

    void setQuery(String query);

    void showResults(QueryResultDto results, int offset, int limit);

    void clearResults();

    void reset();
  }

}
