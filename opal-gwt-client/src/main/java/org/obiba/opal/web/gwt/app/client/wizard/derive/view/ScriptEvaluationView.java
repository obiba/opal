/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter.ValueSetFetcher;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter.ValueSetsProvider;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.prettify.client.PrettyPrintLabel;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class ScriptEvaluationView extends ViewImpl implements ScriptEvaluationPresenter.Display {

  @UiTemplate("ScriptEvaluationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ScriptEvaluationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private static final int DEFAULT_PAGE_SIZE = 20;

  private final Widget widget;

  @UiField
  Panel summary;

  @UiField
  CellTable<ValueSetsDto.ValueSetDto> valuesTable;

  @UiField
  SimplePager pager;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  Label valueType;

  @UiField
  PrettyPrintLabel script;

  private ValueSelectionHandler valueSelectionHandler;

  private ValueSetsDataProvider dataProvider;

  private TableDto table;

  private ValueSetFetcher fetcher;

  private VariableDto variable;

  @Inject
  public ScriptEvaluationView() {
    super();
    this.widget = uiBinder.createAndBindUi(this);
    pager.setDisplay(valuesTable);
    pager.setPageSize(DEFAULT_PAGE_SIZE);
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == Display.Slots.Summary) {
      summary.clear();
      summary.add(content);
    }
  }

  @Override
  public void setSummaryTabWidget(WidgetDisplay widget) {
    summary.clear();
    summary.add(widget.asWidget());
  }

  @Override
  public ValueSetsProvider getValueSetsProvider() {
    return dataProvider;
  }

  @Override
  public void setTable(TableDto table) {
    this.table = table;
    valuesTable.setRowCount(table.getValueSetCount());

    while(valuesTable.getColumnCount() > 0) {
      valuesTable.removeColumn(0);
    }
    EntityColumn col;
    valuesTable.insertColumn(0, col = new EntityColumn(), table.getEntityType());
    valuesTable.setColumnWidth(col, 1, Unit.PX);
  }

  @Override
  public void setVariable(VariableDto variable) {
    this.variable = variable;

    valueType.setText(variable.getValueType());
    script.setText(Variables.getScript(variable));

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }

    ValueColumn col = new ValueColumn(variable);
    col.setValueSelectionHandler(valueSelectionHandler);
    if(valuesTable.getColumnCount() > 1) {
      valuesTable.removeColumn(1);
    }
    valuesTable.insertColumn(1, col, translations.valueLabel());

    dataProvider = new ValueSetsDataProvider();
    dataProvider.addDataDisplay(valuesTable);
  }

  @Override
  public HandlerRegistration setValueSelectionHandler(ValueSelectionHandler handler) {
    this.valueSelectionHandler = handler;
    if(valuesTable.getColumnCount() > 1) {
      ((ValueColumn) valuesTable.getColumn(1)).setValueSelectionHandler(handler);
    }
    return new HandlerRegistration() {

      @Override
      public void removeHandler() {
        ((ValueColumn) valuesTable.getColumn(1)).setValueSelectionHandler(null);
        valueSelectionHandler = null;
      }
    };
  }

  @Override
  public void setValueSetFetcher(ValueSetFetcher fetcher) {
    this.fetcher = fetcher;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  //
  // Inner classes
  //

  private final class ValueSetsDataProvider extends AbstractDataProvider<ValueSetsDto.ValueSetDto> implements ValueSetsProvider {

    @Override
    protected void onRangeChanged(HasData<ValueSetDto> display) {
      // Get the new range.
      final Range range = display.getVisibleRange();

      // query the valuesets
      int start = range.getStart();

      if(start > table.getValueSetCount()) return;

      int length = range.getLength();
      if(start + length > table.getValueSetCount()) {
        length = table.getValueSetCount() - start;
      }

      fetcher.request(start, length);
    }

    @Override
    public void populateValues(int offset, ValueSetsDto valueSets) {
      updateRowData(offset, JsArrays.toList(valueSets.getValueSetsArray()));
    }

  }

  private static final class EntityColumn extends TextColumn<ValueSetsDto.ValueSetDto> {
    @Override
    public String getValue(ValueSetDto valueSet) {
      return valueSet.getIdentifier();
    }
  }
}
