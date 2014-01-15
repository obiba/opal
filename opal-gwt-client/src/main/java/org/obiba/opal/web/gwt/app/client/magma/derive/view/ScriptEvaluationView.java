/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.derive.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.ScriptEvaluationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.ScriptEvaluationPresenter.ValueSetFetcher;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.ScriptEvaluationPresenter.ValueSetsProvider;
import org.obiba.opal.web.gwt.app.client.magma.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.ui.NavTabsPanel;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.CodeBlock;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class ScriptEvaluationView extends ViewImpl implements ScriptEvaluationPresenter.Display {

  @UiTemplate("ScriptEvaluationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ScriptEvaluationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private static final int DEFAULT_PAGE_SIZE = 20;

  private final Widget widget;

  @UiField
  Panel summary;

  @UiField
  Table<ValueSetsDto.ValueSetDto> valuesTable;

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noValues;

  @UiField
  NavTabsPanel tabs;

  @UiField
  Label valueType;

  @UiField
  CodeBlock script;

  @UiField
  TextArea commentBox;

  private ValueSelectionHandler valueSelectionHandler;

  private ValueSetsDataProvider dataProvider;

  private TableDto table;

  private ValueSetFetcher fetcher;

  @Inject
  public ScriptEvaluationView() {
    widget = uiBinder.createAndBindUi(this);
    valuesTable.setEmptyTableWidget(noValues);
    pager.setDisplay(valuesTable);
    pager.setPageSize(DEFAULT_PAGE_SIZE);
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == Display.Slots.Summary) {
      summary.clear();
      summary.add(content);
    }
  }

  @Override
  public void setSummaryTabWidget(SummaryTabPresenter.Display widget) {
    summary.clear();
    summary.add(widget.asWidget());
  }

  @Override
  public void setValuesVisible(boolean visible) {
    tabs.setTabVisible(1, visible);
  }

  @Override
  public void setCommentVisible(boolean visible) {
    tabs.setTabVisible(3, visible);
  }

  @Override
  public ValueSetsProvider getValueSetsProvider() {
    return dataProvider;
  }

  @Override
  public void setOriginalTable(TableDto table) {
    this.table = table;

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }

    valuesTable.setRowCount(table.getValueSetCount(), true);

    while(valuesTable.getColumnCount() > 0) {
      valuesTable.removeColumn(0);
    }
    EntityColumn col;
    valuesTable.insertColumn(0, col = new EntityColumn(), table.getEntityType());
    valuesTable.setColumnWidth(col, 1, Unit.PX);
  }

  @Override
  public void setOriginalVariable(final VariableDto variable) {
    valueType.setText(variable.getValueType());
    script.setText(VariableDtos.getScript(variable));

    commentBox.setValue(VariableDtos.getComment(variable));
    commentBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        VariableDtos.setComment(variable, commentBox.getValue());
      }
    });

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
    valueSelectionHandler = handler;
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

  private final class ValueSetsDataProvider extends AbstractDataProvider<ValueSetsDto.ValueSetDto>
      implements ValueSetsProvider {

    @Override
    protected void onRangeChanged(HasData<ValueSetDto> display) {
      // Get the new range.
      Range range = display.getVisibleRange();

      // query the valuesets
      int start = range.getStart();

      if(start > table.getValueSetCount() && table.getValueSetCount() > 0) return;

      int length = range.getLength();
      if(start + length > table.getValueSetCount() && table.getValueSetCount() > 0) {
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
