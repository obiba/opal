package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableEntityDto;

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
public class EntityDialogViewPresenter extends PresenterWidget<EntityDialogViewPresenter.Display> {

  private VariableEntityDto entityDto;

  private String preSelectedDatasource;

  private String preSelectedTable;

  private final Map<String, TableDto> tablesByName = new HashMap<String, TableDto>();

  @Inject
  public EntityDialogViewPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addChangeHandlers();
  }

  private void addChangeHandlers() {
    getView().getDatasourceList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        loadTables();
      }
    });
    getView().getTableList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        loadValueSets();
      }
    });

  }

  @Override
  public void onReveal() {
    loadDatasources();
  }

  private void loadDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            List<String> datasources = new ArrayList<String>();
            if(resource != null) {
              for(int i = 0; i < resource.length(); i++) {
                datasources.add(resource.get(i).getName());
              }
              Collections.sort(datasources);
            }
            getView().setDatasources(datasources, preSelectedDatasource);
            loadTables();
          }
        }).send();
  }

  private void loadTables() {

    UriBuilder uriBuilder = UriBuilder.create().segment("datasource", getView().getSelectedDatasource(), "tables");
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            List<String> tables = new ArrayList<String>();
            tablesByName.clear();
            if(resource != null) {
              for(int i = 0; i < resource.length(); i++) {
                TableDto tableDto = resource.get(i);
                tablesByName.put(tableDto.getName(), tableDto);
              }
              tables.addAll(tablesByName.keySet());
              Collections.sort(tables);
            }
            getView().setTables(tables, preSelectedTable);
            loadValueSets();
          }
        }).send();
  }

  private void loadValueSets() {
    // TODO load ValueSet for selected datasource, table and entityDto.
  }

  public void setEntityDto(VariableEntityDto entityDto) {
    this.entityDto = entityDto;
  }

  public void setPreSelectedDatasource(String preSelectedDatasource) {
    this.preSelectedDatasource = preSelectedDatasource;
  }

  public void setPreSelectedTable(String preSelectedTable) {
    this.preSelectedTable = preSelectedTable;
  }

  public interface Display extends PopupView {

    void showDialog();

    void hideDialog();

    void setDatasources(List<String> datasources, @Nullable String selectedDatasource);

    String getSelectedDatasource();

    void setTables(List<String> tables, @Nullable String selectedTable);

    String getSelectedTable();

    HasChangeHandlers getTableList();

    HasChangeHandlers getDatasourceList();
  }

}
