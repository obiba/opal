/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.identifiers.view;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersTablePresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersTableUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class IdentifiersTableView extends ViewWithUiHandlers<IdentifiersTableUiHandlers>
    implements IdentifiersTablePresenter.Display {

  interface Binder extends UiBinder<Widget, IdentifiersTableView> {}

  @UiField
  Heading title;

  @UiField
  Label timestamps;

  @UiField
  Label systemIdsCount;

  @UiField
  Label idMappingsCount;

  @UiField
  Panel tablePanel;

  private SimplePager pager;

  private ValueSetsDataProvider provider;

  private TableDto table;

  private Table<ValueSetsDto.ValueSetDto> idTable;

  private JsArrayString variableNames;

  @Inject
  public IdentifiersTableView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    provider = new ValueSetsDataProvider();
  }

  @Override
  public void showIdentifiersTable(TableDto table) {
    this.table = table;
    title.setText(table.getEntityType());
    timestamps.setText(Moment.create(table.getTimestamps().getLastUpdate()).fromNow());
    systemIdsCount.setText("" + table.getValueSetCount());
    idMappingsCount.setText("" + table.getVariableCount());
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    tablePanel.clear();

    pager = new SimplePager(SimplePager.TextLocation.RIGHT);
    pager.addStyleName("pull-right bottom-margin");
    tablePanel.add(pager);
    createAndInitializeIdTable(variables);
    tablePanel.add(idTable);

    for(HasData<ValueSetsDto.ValueSetDto> display : provider.getDataDisplays()) {
      provider.removeDataDisplay(display);
    }
    provider.addDataDisplay(idTable);
  }

  @Override
  public void setValueSets(int offset, ValueSetsDto valueSets) {
    variableNames = valueSets.getVariablesArray();
    provider.updateRowData(offset, JsArrays.toList(valueSets.getValueSetsArray()));
    idTable.setVisibleRange(offset, idTable.getPageSize());
  }

  @UiHandler("deleteIdTable")
  void onDeleteTable(ClickEvent event) {
    getUiHandlers().onDeleteIdentifiersTable();
  }

  @UiHandler("importSystemId")
  void onImportSystemIdentifiers(ClickEvent event) {
    getUiHandlers().onImportSystemIdentifiers();
  }

  //
  // Private methods
  //

  private class ValueSetsDataProvider extends AsyncDataProvider<ValueSetsDto.ValueSetDto> {
    @Override
    protected void onRangeChanged(HasData<ValueSetsDto.ValueSetDto> display) {
      // Get the new range.
      Range range = display.getVisibleRange();
      getUiHandlers().onIdentifiersRequest(table, "true", range.getStart(), range.getLength());
    }
  }

  private void createAndInitializeIdTable(JsArray<VariableDto> variables) {
    idTable = new Table<ValueSetsDto.ValueSetDto>();
    idTable.setPageSize(20);
    idTable.addColumn(new TextColumn<ValueSetsDto.ValueSetDto>() {

      @Override
      public String getValue(ValueSetsDto.ValueSetDto value) {
        return value.getIdentifier();
      }
    }, "ID");
    idTable.setColumnWidth(0, 1, Style.Unit.PX);
    for(VariableDto variable : JsArrays.toIterable(variables)) {
      idTable.addColumn(new IdentifierColumn(variable), variable.getName());
    }
    idTable.addStyleName("pull-left");
    pager.setDisplay(idTable);
    idTable.setRowCount(table.getValueSetCount());
    idTable.setPageStart(0);
  }

  private class IdentifierColumn extends TextColumn<ValueSetsDto.ValueSetDto> {
    private final VariableDto variable;

    private int position = -1;

    IdentifierColumn(VariableDto variable) {
      this.variable = variable;
    }

    @Override
    public String getValue(ValueSetsDto.ValueSetDto valueSet) {
      if(valueSet.getValuesArray() == null || valueSet.getValuesArray().length() <= 0) return "";
      if(position < 0) {
        for(int i = 0; i < variableNames.length(); i++) {
          if(variableNames.get(i).equals(variable.getName())) {
            position = i;
            return valueSet.getValuesArray().get(i).getValue();
          }
        }
      } else {
        return valueSet.getValuesArray().get(position).getValue();
      }
      return "";
    }
  }
}
