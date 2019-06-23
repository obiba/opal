/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entity;

import com.google.common.base.Strings;
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
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;

import java.util.List;
import java.util.Map;

public class SearchEntityPresenter extends Presenter<SearchEntityPresenter.Display, SearchEntityPresenter.Proxy>
    implements HasPageTitle, SearchEntityUiHandlers {

  private final ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider;

  private final Translations translations;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final PlaceManager placeManager;

  private String selectedType;

  private String selectedId;

  private String selectedTable;

  private JsArray<TableDto> tables;

  private Map<String, JsArray<VariableDto>> tableVariables = Maps.newHashMap();

  @Inject
  public SearchEntityPresenter(EventBus eventBus, Display display, Proxy proxy,
                               ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider, Translations translations,
                               DefaultBreadcrumbsBuilder breadcrumbsHelper, PlaceManager placeManager) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.valueSequencePopupProvider = valueSequencePopupProvider.setContainer(this);
    this.translations = translations;
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageSearchEntityTitle();
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
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
    selectedId = request.getParameter(ParameterTokens.TOKEN_ID, null);
    selectedTable = request.getParameter(ParameterTokens.TOKEN_TABLE, null);
    tableVariables.clear();
    tables = null;
    getView().clearResults(true);
    if (!Strings.isNullOrEmpty(selectedId)) {
      getView().setEntityType(selectedType);
      getView().setEntityId(selectedId);
      searchSelected();
    } else {
      getView().reset();
    }
  }

  @Override
  public void onSearch(String entityType, String entityId) {
    selectedType = entityType;
    selectedId = entityId;
    selectedTable = null;
    getView().clearResults(true);
    searchSelected();
  }

  @Override
  public void onTableChange(String tableReference) {
    selectedTable = tableReference;
    loadTable(selectedTable);
    updateHistory();
  }

  @Override
  public void requestValueSequenceView(VariableDto variableDto) {
    ValueSequencePopupPresenter valueSequencePopupPresenter = valueSequencePopupProvider.get();
    valueSequencePopupPresenter.initialize(tableReferenceAsDto(selectedTable), variableDto, selectedId, true);
  }

  @Override
  public void requestBinaryValueView(VariableDto variableDto) {
    StringBuilder link = new StringBuilder(tableReferenceAsLink(selectedTable));
    link.append("/valueSet/").append(selectedId).append("/variable/").append(variableDto.getName()).append("/value");
    fireEvent(new FileDownloadRequestEvent(link.toString()));
  }

  @Override
  public void requestGeoValueView(VariableDto variable, ValueSetsDto.ValueDto value) {
    fireEvent(new GeoValueDisplayEvent(variable, selectedId, value));
  }

  @Override
  public void requestEntityView(VariableDto variable, ValueSetsDto.ValueDto value) {
    getView().setEntityType(variable.getReferencedEntityType());
    getView().setEntityId(value.getValue());
    onSearch(variable.getReferencedEntityType(), value.getValue());
  }

  //
  // Private methods
  //

  private void updateHistory() {
    PlaceRequest.Builder builder = PlaceRequestHelper.createRequestBuilder(placeManager.getCurrentPlaceRequest())
        .with(ParameterTokens.TOKEN_TYPE, selectedType)
        .with(ParameterTokens.TOKEN_ID, selectedId);
    if (!Strings.isNullOrEmpty(selectedTable)) builder.with(ParameterTokens.TOKEN_TABLE, selectedTable);
    placeManager.updateHistory(builder.build(), true);
  }

  private void searchSelected() {
    loadTables();
  }

  /**
   * Load the tables where the entity appears.
   */
  private void loadTables() {
    UriBuilder uriBuilder = UriBuilder.create().segment("entity", selectedId, "type", selectedType, "tables");
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            tables = JsArrays.toSafeArray(resource);
            if (tables.length() == 0) {
              selectedTable = null;
              getView().clearResults(false);
              notifyNoSuchEntity();
            } else {
              getView().showTables(tables);
              String tableRef = selectedTable;
              if (Strings.isNullOrEmpty(selectedTable)) {
                tableRef = getTableReference(0);
                selectedTable = tableRef;
              } else {
                // verify selected is one of the tables
                boolean seen = false;
                for (TableDto table : JsArrays.toIterable(tables)) {
                  if (selectedTable.equals(getTableReference(table))) {
                    seen = true;
                    break;
                  }
                }
                if (!seen) {
                  tableRef = getTableReference(0);
                  selectedTable = tableRef;
                }
              }
              loadTable(tableRef);
            }
            updateHistory();
          }

          private String getTableReference(int index) {
            return getTableReference(tables.get(index));
          }

          private String getTableReference(TableDto table) {
            return table.getDatasourceName() + "." + table.getName();
          }

        }).send();
  }

  /**
   * Load value set of the entity for the selected table. Load also the variables if necessary.
   */
  private void loadTable(final String tableRef) {
    MagmaPath.Parser parser = parseTableReference(tableRef);
    final String datasource = parser.getDatasource();
    final String table = parser.getTable();

    String valueSetUri = UriBuilders.DATASOURCE_TABLE_VALUESET.create().build(datasource, table, selectedId);
    ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder()
        .forResource(valueSetUri).get()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clearResults(false);
            notifyNoSuchEntity();
          }
        }, Response.SC_FORBIDDEN, Response.SC_NOT_FOUND)
        .withCallback(new ResourceCallback<ValueSetsDto>() {
          @Override
          public void onResource(Response response, ValueSetsDto resource) {
            if (tableVariables.containsKey(tableRef))
              getView().showValueSet(datasource, table, tableVariables.get(tableRef), resource);
            else
             loadSelectedTableVariables(datasource, table, resource);
          }

          private void loadSelectedTableVariables(final String datasource, final String table, final ValueSetsDto valueSets) {
            String variablesUri = UriBuilders.DATASOURCE_TABLE_VARIABLES.create().build(datasource, table);
            ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder() //
                .forResource(variablesUri) //
                .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
                  @Override
                  public void onResource(Response response, JsArray<VariableDto> resource) {
                    tableVariables.put(tableRef, JsArrays.toSafeArray(resource));
                    getView().showValueSet(datasource, table, tableVariables.get(tableRef), valueSets);
                  }
                }).get().send();
          }
        })
        .get().send();
  }

  private void notifyNoSuchEntity() {
    if (Strings.isNullOrEmpty(selectedTable))
      fireEvent(NotificationEvent.newBuilder().warn("NoSuchEntity").args(selectedId, selectedType).build());
    else
      fireEvent(NotificationEvent.newBuilder().warn("NoSuchEntityInTable").args(selectedId, selectedType, selectedTable).build());
  }

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
  @NameToken(Places.SEARCH_ENTITY)
  public interface Proxy extends ProxyPlace<SearchEntityPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<SearchEntityUiHandlers> {

    void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType);

    void setEntityType(String selectedType);

    void setEntityId(String selectedId);

    void clearResults(boolean searchProgress);

    void reset();

    void showTables(JsArray<TableDto> tables);

    void showValueSet(String datasource, String table, JsArray<VariableDto> variables, ValueSetsDto values);
  }

}
