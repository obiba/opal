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

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;

import java.util.List;
import java.util.Map;

public class SearchEntitiesView extends ViewWithUiHandlers<SearchEntitiesUiHandlers> implements SearchEntitiesPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchEntitiesView> {}

  private final Translations translations;

  private final PlaceManager placeManager;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Panel entityPanel;

  @UiField
  DropdownButton typeDropdown;

  @UiField
  TextBox entityId;

  @UiField
  Panel entityResultPanel;

  @UiField
  TableChooser tableChooser;

  @UiField
  ValueSetTable valueSetTable;

  @UiField
  OpalSimplePager valueSetPager;

  ListDataProvider<VariableValueRow> valueSetProvider = new ListDataProvider<VariableValueRow>();

  @Inject
  public SearchEntitiesView(SearchEntitiesView.Binder uiBinder, Translations translations, PlaceManager placeManager) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    this.placeManager = placeManager;
    initValueSetTable();
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("searchButton")
  public void onSearch(ClickEvent event) {
    if (!entityId.getValue().isEmpty()) getUiHandlers().onSearch(typeDropdown.getText().trim(), entityId.getValue());
  }

  @UiHandler("entityId")
  public void onEntityIdTyped(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) onSearch(null);
  }

  @UiHandler("tableChooser")
  public void onTableSelection(ChosenChangeEvent event) {
    getUiHandlers().onTableChange(tableChooser.getSelectedValue());
  }

  @Override
  public void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType) {
    typeDropdown.clear();
    String selectedEntityType = Strings.isNullOrEmpty(selectedType) ? "Participant" : selectedType;
    boolean hasSelectedEntityType = false;
    boolean hasParticipantType = false;
    for (VariableEntitySummaryDto typeSummary : entityTypes) {
      final NavLink item = new NavLink(typeSummary.getEntityType());
      item.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
         typeDropdown.setText(item.getText());
        }
      });
      typeDropdown.add(item);
      if (selectedEntityType.equals(typeSummary.getEntityType())) hasSelectedEntityType = true;
      if ("Participant".equals(typeSummary.getEntityType())) hasParticipantType = true;
    }
    if (hasSelectedEntityType) typeDropdown.setText(selectedEntityType);
    else if (!entityTypes.isEmpty()) {
      if (hasParticipantType) typeDropdown.setText("Participant");
      else typeDropdown.setText(entityTypes.get(0).getEntityType());
    }
    entityPanel.setVisible(!entityTypes.isEmpty());
  }

  @Override
  public void setEntityType(String selectedType) {
    typeDropdown.setText(Strings.isNullOrEmpty(selectedType) ? "Participant" : selectedType);
  }

  @Override
  public void setEntityId(String selectedId) {
    entityId.setValue(selectedId);
    if (Strings.isNullOrEmpty(selectedId)) clearResults();
  }

  @Override
  public void clearResults() {
    tableChooser.clear();
    entityResultPanel.setVisible(false);
  }

  @Override
  public void showTables(JsArray<TableDto> tables) {
    //GWT.log("showTables");
    tableChooser.clear();
    tableChooser.addTableSelections(tables);
    entityResultPanel.setVisible(tables != null && tables.length()>0);
  }

  @Override
  public void showValueSet(String datasource, String table, JsArray<VariableDto> variables, ValueSetsDto valueSets) {
    //GWT.log("showValueSet");
    List<VariableValueRow> rows = Lists.newArrayList();
    JsArray<ValueSetsDto.ValueDto> values = valueSets.getValueSetsArray().get(0).getValuesArray();
    JsArrayString variableNames = valueSets.getVariablesArray();
    Map<String, VariableDto> variablesMap = Maps.newHashMap();
    for (VariableDto var : JsArrays.toIterable(variables)) {
      variablesMap.put(var.getName(), var);
    }
    for (int i=0; i<variableNames.length(); i++) {
      String varName = variableNames.get(i);
      rows.add(new VariableValueRow(datasource, table, varName, values.get(i), variablesMap.get(varName)));
    }
    valueSetProvider.setList(rows);
    valueSetPager.firstPage();
    valueSetPager.setPagerVisible(valueSetProvider.getList().size() > Table.DEFAULT_PAGESIZE);
    valueSetProvider.refresh();
    tableChooser.setSelectedValue(datasource + "." + table);
  }

  private void initValueSetTable() {
    valueSetTable.setPlaceManager(placeManager);
    valueSetTable.setVariableValueSelectionHandler(new ValueSetTable.VariableValueSelectionHandler() {
      @Override
      public void onValueSelection(VariableValueRow variableValueRow) {
        VariableDto variable = variableValueRow.getVariableDto();
        if(variable.getIsRepeatable()) {
          getUiHandlers().requestValueSequenceView(variable);
        } else if("binary".equalsIgnoreCase(variable.getValueType())) {
          getUiHandlers().requestBinaryValueView(variable);
        } else if(variable.getValueType().matches("point|linestring|polygon")) {
          getUiHandlers().requestGeoValueView(variable, variableValueRow.getValueDto());
        } else if("text".equalsIgnoreCase(variable.getValueType()) &&
            !Strings.isNullOrEmpty(variable.getReferencedEntityType())) {
          getUiHandlers().requestEntityView(variable, variableValueRow.getValueDto());
        }
      }
    });
    valueSetPager.setDisplay(valueSetTable);
    valueSetProvider.addDataDisplay(valueSetTable);
  }
}
