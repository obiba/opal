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

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
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
  Label entityType;

  @UiField
  Label occurrenceGroup;

  @UiField
  Table<ValueOccurrence> valuesTable;

  @UiField
  Label noValues;

  @UiField
  Button closeButton;

  @UiField
  ResizeHandle resizeHandle;

  private ValueSetDto valueSet;

  private VariableDto variable;

  private ListDataProvider<ValueOccurrence> dataProvider;

  @Inject
  public ValueSequencePopupView(EventBus eventBus) {
    super(eventBus);
    this.widget = uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(content);
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
    entityType.setText(variable.getEntityType());
    occurrenceGroup.setText(variable.getOccurrenceGroup());
    this.variable = variable;
    valuesTable.setEmptyTableWidget(valuesTable.getLoadingIndicator());
    dataProvider.setList(new ArrayList<ValueSequencePopupView.ValueOccurrence>());
    dataProvider.refresh();
  }

  @Override
  public void populate(ValueSetDto valueSet) {
    this.valueSet = valueSet;

    List<ValueOccurrence> occurrences = new ArrayList<ValueSequencePopupView.ValueOccurrence>();
    JsArray<ValueDto> valueSequence = valueSet.getValuesArray().get(0).getValuesArray();
    if(valueSequence != null) {
      for(int i = 0; i < valueSequence.length(); i++) {
        occurrences.add(new ValueOccurrence(i));
      }
    }

    dataProvider.setList(occurrences);
    dataProvider.refresh();
    valuesTable.setEmptyTableWidget(noValues);
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

    valuesTable.addColumn(createValueColumn(0), new Header<String>(new TextCell()) {

      @Override
      public String getValue() {
        return variable.getName();
      }

    });

    dataProvider = new ListDataProvider<ValueSequencePopupView.ValueOccurrence>();
    dataProvider.addDataDisplay(valuesTable);
  }

  private Column<ValueOccurrence, String> createValueColumn(final int pos) {
    return new TextColumn<ValueOccurrence>() {
      @Override
      public String getValue(ValueOccurrence value) {
        return value.getValue(pos);
      }
    };
  }

  private final class ValueOccurrence {

    private final int index;

    public ValueOccurrence(int index) {
      this.index = index;
    }

    public int getIndex() {
      return index;
    }

    public String getValue(int pos) {
      JsArray<ValueDto> valueSequence = valueSet.getValuesArray().get(pos).getValuesArray();
      if(valueSequence == null || index >= valueSequence.length()) return "";
      return valueSequence.get(index).getValue();
    }
  }
}
