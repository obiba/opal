package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class EntityDialogPresenter extends PresenterWidget<EntityDialogPresenter.Display> {

  private String entityType;

  private String entityId;

  private String preSelectedDatasource;

  private String preSelectedTable;

  // TODO use a sorted list table names
  private final Multimap<String, String> tablesByDatasource = ArrayListMultimap.create();

  @Inject
  public EntityDialogPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public void initialize(TableDto table, String entityType, String entityId) {
    preSelectedDatasource = table.getDatasourceName();
    preSelectedTable = table.getName();
    this.entityType = entityType;
    this.entityId = entityId;
  }

  @Override
  protected void onBind() {
    addChangeHandlers();
  }

  private void addChangeHandlers() {
    getView().getDatasourceList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateTables(getView().getSelectedDatasource());
      }
    });
    getView().getTableList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        loadValueSets();
      }
    });
  }

  private void updateTables(String datasource) {
    getView().setTables((List<String>) tablesByDatasource.get(datasource), preSelectedTable);
    loadValueSets();
  }

  @Override
  public void onReveal() {
    getView().setEntityType(entityType);
    getView().setEntityId(entityId);
    loadTables();
  }

  /**
   * find all table where this entity appears
   */
  private void loadTables() {

    UriBuilder uriBuilder = UriBuilder.create().segment("datasource", preSelectedDatasource, "tables");
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            tablesByDatasource.clear();
            if(resource != null) {
              for(int i = 0; i < resource.length(); i++) {
                TableDto tableDto = resource.get(i);
                tablesByDatasource.put(tableDto.getDatasourceName(), tableDto.getName());
              }
            }
            getView().setDatasources(Lists.newArrayList(tablesByDatasource.keys()), preSelectedDatasource);
            updateTables(preSelectedDatasource);
          }
        }).send();

//TODO uncomment this and delete previous REST request that was used for testing widget
//    UriBuilder uriBuilder = UriBuilder.create().segment("entity", entityId, "type", entityType);
//    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
//        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
//          @Override
//          public void onResource(Response response, JsArray<TableDto> resource) {
//            tablesByDatasource.clear();
//            if(resource != null) {
//              for(int i = 0; i < resource.length(); i++) {
//                TableDto tableDto = resource.get(i);
//                tablesByDatasource.put(tableDto.getDatasourceName(), tableDto.getName());
//              }
//            }
//            getView().setDatasources(Lists.newArrayList(tablesByDatasource.values()), preSelectedDatasource);
//            updateTables(preSelectedDatasource);
//          }
//        }).send();
  }

  private void loadValueSets() {
    // TODO load ValueSet for selected datasource, table and entityDto.
  }

  public interface Display extends PopupView {

    void setEntityType(String entityType);

    void setEntityId(String entityId);

    void setDatasources(List<String> datasources, @Nullable String selectedDatasource);

    String getSelectedDatasource();

    void setTables(List<String> tables, @Nullable String selectedTable);

    String getSelectedTable();

    HasChangeHandlers getTableList();

    HasChangeHandlers getDatasourceList();
  }

}
