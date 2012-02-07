/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueOccurrenceColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueOccurrenceColumn.ValueOccurrence;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueOccurrenceColumn.ValueSelectionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter.ValueSetFetcher;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.gwt.app.client.workbench.view.ToggleAnchor;
import org.obiba.opal.web.gwt.app.client.workbench.view.ToggleAnchor.Delegate;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class ValueSequencePopupView extends PopupViewImpl implements ValueSequencePopupPresenter.Display {

  @UiTemplate("ValueSequencePopupView.ui.xml")
  interface ValueSequencePopupViewUiBinder extends UiBinder<Widget, ValueSequencePopupView> {
  }

  private static ValueSequencePopupViewUiBinder uiBinder = GWT.create(ValueSequencePopupViewUiBinder.class);

  private final Widget widget;

  @UiField
  DialogBox dialogBox;

  @UiField
  DockLayoutPanel content;

  @UiField
  Label occurrenceGroup;

  @UiField
  ToggleAnchor toggleGroup;

  @UiField
  Table<ValueOccurrence> valuesTable;

  @UiField
  Label noValues;

  @UiField
  Button closeButton;

  @UiField
  ResizeHandle resizeHandle;

  private VariableDto variable;

  private ListDataProvider<ValueOccurrence> dataProvider;

  private ValueSetFetcher fetcher;

  private ValueSelectionHandler valueSelectionHandler;

  @Inject
  public ValueSequencePopupView(EventBus eventBus) {
    super(eventBus);
    this.widget = uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(content);
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
  public void initialize(TableDto table, VariableDto variable, String entityIdentifier) {
    dialogBox.setText(variable.getName() + " - " + entityIdentifier);
    occurrenceGroup.setText(variable.getOccurrenceGroup());
    toggleGroup.setVisible(variable.getOccurrenceGroup() != null && variable.getOccurrenceGroup().isEmpty() == false);
    toggleGroup.setOn(true);
    this.variable = variable;
    valuesTable.setEmptyTableWidget(valuesTable.getLoadingIndicator());
    dataProvider.setList(new ArrayList<ValueOccurrence>());
    dataProvider.refresh();
    fetcher.request(null);
  }

  @Override
  public void setValueSetFetcher(ValueSetFetcher fetcher) {
    this.fetcher = fetcher;
  }

  @Override
  public void populate(List<VariableDto> variables, ValueSetDto valueSet) {

    populateVariables(variables, valueSet.getVariablesArray());

    // find the max number of occurrences among the group
    // and build the dataset
    List<ValueOccurrence> occurrences = new ArrayList<ValueOccurrence>();
    int max = 0;
    for(int i = 0; i < valueSet.getValuesArray().length(); i++) {
      JsArrayString valueSequence = valueSet.getValuesArray().get(i).getSequenceArray();
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

  private ValueOccurrenceColumn createValueOccurrenceColumn(VariableDto variable, int pos) {
    ValueOccurrenceColumn col = new ValueOccurrenceColumn(variable, pos);
    if(valueSelectionHandler == null) {
      valueSelectionHandler = new ValueSelectionHandler() {

        @Override
        public void onBinaryValueSelection(VariableDto variable, int index, ValueSetDto valueSet) {
          fetcher.requestBinaryValue(variable, valueSet.getEntity().getIdentifier(), index);
        }
      };
    }
    col.setValueSelectionHandler(valueSelectionHandler);
    return col;
  }

  private void initValuesTable() {
    valuesTable.setEmptyTableWidget(noValues);

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
