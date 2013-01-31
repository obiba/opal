/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
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

  private TableDto selectedTable;

  @Inject
  public EntityDialogPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public void initialize(TableDto table, String entityType, String entityId) {
    selectedTable = table;
    this.entityType = entityType;
    this.entityId = entityId;
  }

  @Override
  protected void onBind() {
    addChangeHandlers();
    addCloseHandler();
  }

  private void addChangeHandlers() {
    getView().getTableChooser().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        TableDto table = getView().getSelectedTable();
        if(table != null) {
          loadValueSets(table);
        }
      }
    });
  }

  private void addCloseHandler() {
    registerHandler(getView().getButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getView().hide();
      }
    }));
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
    UriBuilder uriBuilder = UriBuilder.create().segment("entity", entityId, "type", entityType, "tables");
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            getView().setTables(resource, selectedTable);
            loadValueSets(selectedTable);
          }
        }).send();
  }

  private void loadValueSets(TableDto table) {
    UriBuilder uriBuilder = UriBuilder.create()
        .segment("datasource", table.getDatasourceName(), "table", table.getName(), "valueSet", entityId);

    ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<ValueSetsDto>() {
          @Override
          public void onResource(Response response, ValueSetsDto resource) {
            populateRows(resource);
          }
        }).send();
  }

  private void populateRows(ValueSetsDto valueSets) {
    JsArrayString variables = valueSets.getVariablesArray();
    JsArray<ValueSetsDto.ValueSetDto> valueSetList = valueSets.getValueSetsArray();
    JsArray<ValueSetsDto.ValueDto> values = valueSetList.get(0).getValuesArray();

    List<VariableValueRow> rows = new ArrayList<VariableValueRow>();
    int variableCount = variables.length();

    for(int i = 0; i < variableCount; i++) {
      rows.add(new VariableValueRow(variables.get(i), values.get(i)));
    }

    getView().renderRows(rows);
  }

  public interface Display extends PopupView {

    void setEntityType(String entityType);

    void setEntityId(String entityId);

    void setTables(JsArray<TableDto> tables, TableDto selectedTable);

    void renderRows(List<VariableValueRow> rows);

    HasClickHandlers getButton();

    TableDto getSelectedTable();

    HasChangeHandlers getTableChooser();
  }

  public static class VariableValueRow {

    private final String variable;
    private final ValueSetsDto.ValueDto valueDto;

    public VariableValueRow(String variable, ValueSetsDto.ValueDto value) {
      this.variable = variable;
      this.valueDto = value;
    }

    public String getVariable() {
      return variable;
    }

    public ValueSetsDto.ValueDto getValueDto() {
      return valueDto;
    }

  }


}
