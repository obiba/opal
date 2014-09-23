/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueSequencePopupPresenter.ValueSetFetcher;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueSequencePopupUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.ToggleAnchor;
import org.obiba.opal.web.gwt.app.client.ui.ToggleAnchor.Delegate;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueOccurrenceColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueOccurrenceColumn.ValueOccurrence;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueOccurrenceColumn.ValueSelectionHandler;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class ValueSequencePopupView extends ModalPopupViewWithUiHandlers<ValueSequencePopupUiHandlers>
    implements ValueSequencePopupPresenter.Display {

  interface ValueSequencePopupViewUiBinder extends UiBinder<Widget, ValueSequencePopupView> {}

  private static final ValueSequencePopupViewUiBinder uiBinder = GWT.create(ValueSequencePopupViewUiBinder.class);

  private static final int MIN_WIDTH = 480;
  private static final int MIN_HEIGHT = 400;

  private final Widget widget;

  @UiField
  Modal dialogBox;

  @UiField
  Label occurrenceGroup;

  @UiField
  ToggleAnchor toggleGroup;

  @UiField
  Table<ValueOccurrence> valuesTable;

  @UiField
  Label noValues;

  @UiField
  OpalSimplePager pager;

  @UiField
  Button closeButton;

  private VariableDto variable;

  private ListDataProvider<ValueOccurrence> dataProvider;

  private ValueSetFetcher fetcher;

  private ValueSelectionHandler valueSelectionHandler;

  @Inject
  public ValueSequencePopupView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialogBox.setMinWidth(MIN_WIDTH);
    dialogBox.setMinHeight(MIN_HEIGHT);
    toggleGroup.setShowHideTexts();
    toggleGroup.setDelegate(new Delegate() {

      @Override
      public void executeOn() {
        valuesTable.setEmptyTableWidget(valuesTable.getLoadingIndicator());
        fetcher.request("occurrenceGroup().eq('" + variable.getOccurrenceGroup() + "')");
      }

      @Override
      public void executeOff() {
        valuesTable.setEmptyTableWidget(valuesTable.getLoadingIndicator());
        fetcher.request(null);
      }
    });

    initValuesTable();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HasClickHandlers getButton() {
    return closeButton;
  }

  @Override
  public void initialize(TableDto table, VariableDto variable, String entityIdentifier, boolean modal) {
    dialogBox.setTitle(variable.getName() + " - " + entityIdentifier);

    occurrenceGroup.setText(variable.getOccurrenceGroup());
    toggleGroup.setVisible(variable.getOccurrenceGroup() != null && !variable.getOccurrenceGroup().isEmpty());
    toggleGroup.setOn(true);
    this.variable = variable;
    valuesTable.setEmptyTableWidget(valuesTable.getLoadingIndicator());
    dataProvider.setList(new ArrayList<ValueOccurrence>());
    dataProvider.refresh();
    fetcher.request(null);
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @Override
  public void setValueSetFetcher(ValueSetFetcher fetcher) {
    this.fetcher = fetcher;
  }

  @Override
  public void populate(List<VariableDto> variables, ValueSetsDto valueSets) {

    populateVariables(variables, valueSets.getVariablesArray());

    // find the max number of occurrences among the group
    // and build the dataset
    List<ValueOccurrence> occurrences = new ArrayList<ValueOccurrence>();
    ValueSetDto valueSet = valueSets.getValueSetsArray().get(0);
    int max = 0;
    for(int i = 0; i < valueSet.getValuesArray().length(); i++) {
      JsArray<ValueDto> valueSequence = valueSet.getValuesArray().get(i).getValuesArray();
      if(valueSequence != null && valueSequence.length() > max) {
        max = valueSequence.length();
      }
    }
    for(int i = 0; i < max; i++) {
      occurrences.add(new ValueOccurrence(valueSet, i));
    }

    // refresh data provider
    dataProvider.setList(occurrences);
    dataProvider.refresh();
    valuesTable.setEmptyTableWidget(noValues);
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  //
  // Private methods
  //

  private void populateVariables(List<VariableDto> variables, JsArrayString variableNames) {
    // remove previously added variable columns
    while(valuesTable.getColumnCount() > 1) {
      valuesTable.removeColumn(valuesTable.getColumnCount() - 1);
    }

    // add the variables columns
    for(int i = 0; i < variableNames.length(); i++) {
      final String varName = variableNames.get(i);

      // find the variable object and create+add the column
      VariableDto var = findVariable(variables, varName);
      if(var != null) {
        valuesTable.addColumn(createValueOccurrenceColumn(var, i), new Header<String>(new TextCell()) {

          @Override
          public String getValue() {
            return varName;
          }

        });
      }
    }
  }

  private VariableDto findVariable(List<VariableDto> variables, String name) {
    for(VariableDto var : variables) {
      if(var.getName().equals(name)) {
        return var;
      }
    }
    return null;
  }

  private ValueOccurrenceColumn createValueOccurrenceColumn(VariableDto variable, final int pos) {
    ValueOccurrenceColumn col = new ValueOccurrenceColumn(variable, pos);
    if(valueSelectionHandler == null) {
      valueSelectionHandler = new ValueSelectionHandler() {

        @Override
        public void onBinaryValueSelection(VariableDto variable, int index, ValueSetDto valueSet) {
          fetcher.requestBinaryValue(variable, valueSet.getIdentifier(), index);
        }

        @Override
        public void onGeoValueSelection(VariableDto variable, int index, ValueSetDto valueSet, ValueSetsDto.ValueDto value) {
          fetcher.requestGeoValue(variable, valueSet.getIdentifier(), value, index);
        }

        @Override
        public void onEntityIDSelection(VariableDto variable, int index, ValueSetDto valueSet, ValueDto value) {
          fetcher.requestEntityID(variable, valueSet.getIdentifier(), value, index);
        }
      };
    }
    col.setValueSelectionHandler(valueSelectionHandler);
    return col;
  }

  private void initValuesTable() {
    valuesTable.setEmptyTableWidget(noValues);
    pager.setDisplay(valuesTable);
    pager.setPageSize(Table.DEFAULT_PAGESIZE);

    TextColumn<ValueOccurrence> occColumn = new TextColumn<ValueOccurrence>() {

      @Override
      public String getValue(ValueOccurrence value) {
        return Integer.toString(value.getIndex() + 1);
      }
    };
    valuesTable.addColumn(occColumn, "#");
    valuesTable.setColumnWidth(occColumn, 1, Unit.PX);

    dataProvider = new ListDataProvider<ValueOccurrence>();
    dataProvider.addDataDisplay(valuesTable);
  }

}
