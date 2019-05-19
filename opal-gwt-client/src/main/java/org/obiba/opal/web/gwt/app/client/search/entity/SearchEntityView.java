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

import com.github.gwtbootstrap.client.ui.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;

import java.util.List;
import java.util.Map;

public class SearchEntityView extends ViewWithUiHandlers<SearchEntityUiHandlers> implements SearchEntityPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchEntityView> {}


  private final PlaceManager placeManager;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Panel entityPanel;

  @UiField
  EntityTypeDropdown typeDropdown;

  @UiField(provided = true)
  Typeahead entityTypeahead;

  @UiField
  TextBox entityId;

  @UiField
  Panel entityResultPanel;

  @UiField
  Heading entityTitle;

  @UiField
  Image refreshPending;

  @UiField
  TableChooser tableChooser;

  @UiField
  TextBoxClearable filter;

  @UiField
  Controls filterControls;

  @UiField
  CheckBox showEmpties;

  @UiField
  Controls emptiesControls;

  @UiField
  ValueSetTable valueSetTable;

  @UiField
  OpalSimplePager valueSetPager;

  private EntityIdentifierSuggestOracle oracle;

  private List<VariableValueRow> variableValueRows;

  private ListDataProvider<VariableValueRow> valueSetProvider = new ListDataProvider<VariableValueRow>();

  @Inject
  public SearchEntityView(SearchEntityView.Binder uiBinder, Translations translations, PlaceManager placeManager) {
    initEntityTypeahead();
    initWidget(uiBinder.createAndBindUi(this));
    this.placeManager = placeManager;
    initValueSetTable();
    filter.getTextBox().setPlaceholder(translations.filterVariables());
    typeDropdown.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        oracle.setEntityType(typeDropdown.getSelection());
      }
    });
  }

  private void initEntityTypeahead() {
    oracle = new EntityIdentifierSuggestOracle("Participant");
    entityTypeahead = new Typeahead(oracle);
    entityTypeahead.setDisplayItemCount(10);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("searchButton")
  public void onSearch(ClickEvent event) {
    if (entityId.getValue().isEmpty()) reset();
    else getUiHandlers().onSearch(typeDropdown.getSelection(), entityId.getValue());
  }

  @UiHandler("entityId")
  public void onEntityIdTyped(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) onSearch(null);
  }

  @UiHandler("tableChooser")
  public void onTableSelection(ChosenChangeEvent event) {
    setValueSetsVisible(false);
    filter.setText("");
    showEmpties.setValue(true, false);
    getUiHandlers().onTableChange(tableChooser.getSelectedValue());
  }

  @UiHandler("filter")
  public void onFilterUpdate(KeyUpEvent event) {
    if (variableValueRows == null) return;
    showVariableValueRows(filterVariableValueRows(variableValueRows));
  }

  @UiHandler("showEmpties")
  public void onShowEmptiesUpdate(ClickEvent event) {
    if (variableValueRows == null) return;
    showVariableValueRows(filterVariableValueRows(variableValueRows));
  }


  @Override
  public void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType) {
    typeDropdown.setEntityTypes(entityTypes, selectedType);
    entityPanel.setVisible(!entityTypes.isEmpty());
  }

  @Override
  public void setEntityType(String selectedType) {
    typeDropdown.setSelection(selectedType);
  }

  @Override
  public void setEntityId(String selectedId) {
    entityId.setValue(selectedId);
    if (Strings.isNullOrEmpty(selectedId)) clearResults(false);
  }

  @Override
  public void clearResults(boolean searchProgress) {
    tableChooser.clear();
    filter.setText("");
    showEmpties.setValue(true, false);
    entityResultPanel.setVisible(false);
    setValueSetsVisible(false);
    refreshPending.setVisible(searchProgress);
  }

  @Override
  public void reset() {
    clearResults(false);
    entityId.setText("");
  }

  @Override
  public void showTables(JsArray<TableDto> tables) {
    tableChooser.clear();
    tableChooser.addTableSelections(tables);
    entityResultPanel.setVisible(tables != null && tables.length()>0);
  }

  @Override
  public void showValueSet(String datasource, String table, JsArray<VariableDto> variables, ValueSetsDto valueSets) {
    setVariableValueRows(datasource, table, variables, valueSets);
    showVariableValueRows(variableValueRows);
    tableChooser.setSelectedValue(datasource + "." + table);
  }

  /**
   * Build the complete variable value variableValueRows.
   *
   * @param datasource
   * @param table
   * @param variables
   * @param valueSets
   * @return
   */
  private void setVariableValueRows(String datasource, String table, JsArray<VariableDto> variables, ValueSetsDto valueSets) {
    variableValueRows = Lists.newArrayList();
    entityTitle.setText(valueSets.getEntityType() + " " + valueSets.getValueSetsArray().get(0).getIdentifier());
    JsArray<ValueSetsDto.ValueDto> values = valueSets.getValueSetsArray().get(0).getValuesArray();
    JsArrayString variableNames = valueSets.getVariablesArray();
    Map<String, VariableDto> variablesMap = Maps.newHashMap();
    for (VariableDto var : JsArrays.toIterable(variables)) {
      variablesMap.put(var.getName(), var);
    }
    for (int i=0; i<variableNames.length(); i++) {
      String varName = variableNames.get(i);
      variableValueRows.add(new VariableValueRow(datasource, table, varName, values.get(i), variablesMap.get(varName)));
    }
  }

  private void showVariableValueRows(List<VariableValueRow> rows) {
    valueSetProvider.setList(rows);
    valueSetPager.firstPage();
    valueSetPager.setPagerVisible(valueSetProvider.getList().size() > Table.DEFAULT_PAGESIZE);
    valueSetProvider.refresh();
    setValueSetsVisible(true);
  }

  private void setValueSetsVisible(boolean visible) {
    refreshPending.setVisible(!visible);
    valueSetTable.setVisible(visible);
    valueSetPager.setVisible(visible);
    filterControls.setVisible(visible);
    emptiesControls.setVisible(visible);
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
  
  private List<VariableValueRow> filterVariableValueRows(List<VariableValueRow> originalRows) {
    List<VariableValueRow> rows = Lists.newArrayList();
    List<String> tokens = FilterHelper.tokenize(filter.getText());
    boolean hasFilter = !tokens.isEmpty();
    boolean withEmpties = showEmpties.getValue();
    for (VariableValueRow row : originalRows) {
      boolean isIn = true;
      if (!withEmpties && row.hasEmptyValue()) isIn = false;
      if (hasFilter && !FilterHelper.matches(row.getVariableDto().getName(), tokens)) isIn = false;
      if (isIn) rows.add(row);
    }
    return rows;
  }
}
