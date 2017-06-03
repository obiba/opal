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
import org.obiba.opal.web.gwt.app.client.search.entity.SearchEntityUiHandlers;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;

import java.util.List;
import java.util.Map;

public class SearchEntitiesPresenter extends Presenter<SearchEntitiesPresenter.Display, SearchEntitiesPresenter.Proxy>
    implements HasPageTitle, SearchEntitiesUiHandlers {

  private final Translations translations;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final PlaceManager placeManager;

  private String selectedType;

  private String query;

  private JsArray<TableDto> tables;

  private Map<String, JsArray<VariableDto>> tableVariables = Maps.newHashMap();

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
    //GWT.log("onReveal=" + selectedType + ":" + selectedId + ":" + selectedTable);
    ResourceRequestBuilderFactory.<JsArray<VariableEntitySummaryDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_ENTITY_TYPES.create().build()).get()
        .withCallback(new ResourceCallback<JsArray<VariableEntitySummaryDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableEntitySummaryDto> resource) {
            getView().setEntityTypes(JsArrays.toList(resource), selectedType);
          }
        }).send();
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    selectedType = request.getParameter(ParameterTokens.TOKEN_TYPE, "Participant");
    query = request.getParameter(ParameterTokens.TOKEN_QUERY, null);
    tableVariables.clear();
    tables = null;
    getView().clearResults(true);
    //GWT.log("prepareFromRequest=" + selectedType + ":" + selectedId + ":" + selectedTable);
    if (!Strings.isNullOrEmpty(query)) {
      getView().setEntityType(selectedType);
      getView().setQuery(query);
      searchSelected();
    } else {
      getView().reset();
    }
  }

  @Override
  public void onSearch(String entityType, String query) {
    selectedType = entityType;
    this.query = query;
    //GWT.log("onSearch=" + selectedType + ":" + query + ":" + query);
    getView().clearResults(true);
    searchSelected();
  }

  //
  // Private methods
  //

  private void updateHistory() {
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .with(ParameterTokens.TOKEN_TYPE, selectedType)
        .with(ParameterTokens.TOKEN_QUERY, query);
    placeManager.updateHistory(builder.build(), true);
  }

  private void searchSelected() {
    //GWT.log("searchSelected=" + selectedType + ":" + selectedId + ":" + selectedTable);
    loadTables();
  }

  /**
   * Load the tables where the entity appears.
   */
  private void loadTables() {
  }

  /**
   * Load value set of the entity for the selected table. Load also the variables if necessary.
   */

  private MagmaPath.Parser parseTableReference(String tableReference) {
    return MagmaPath.Parser.parse(tableReference);
  }

  private String tableReferenceAsLink(String tableReference) {
    MagmaPath.Parser parser = parseTableReference(tableReference);
    return "/datasource/" + parser.getDatasource() + "/table/" + parser.getTable();
  }

  private TableDto tableReferenceAsDto(String tableReference) {
    for (TableDto tableDto : JsArrays.toIterable(tables)) {
      if (tableReference.equals(tableDto.getDatasourceName() + "." + tableDto.getName())) return tableDto;
    }
    return null;
  }

  @ProxyStandard
  @NameToken(Places.SEARCH_ENTITIES)
  public interface Proxy extends ProxyPlace<SearchEntitiesPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<SearchEntitiesUiHandlers> {

    void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType);

    void setEntityType(String selectedType);

    void setQuery(String query);

    void clearResults(boolean searchProgress);

    void reset();

  }

}
