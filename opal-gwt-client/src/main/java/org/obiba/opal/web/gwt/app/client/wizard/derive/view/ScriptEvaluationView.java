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
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.prettify.client.PrettyPrintLabel;
import org.obiba.opal.web.model.client.magma.ValueDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ScriptEvaluationView extends Composite implements ScriptEvaluationPresenter.Display {

  @UiTemplate("ScriptEvaluationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ScriptEvaluationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  Panel summary;

  @UiField
  CellTable<ValueDto> valuesTable;

  @UiField
  Anchor previousPage;

  @UiField
  Anchor nextPage;

  @UiField
  Label pageLow;

  @UiField
  Label pageHigh;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  Label valueType;

  @UiField
  PrettyPrintLabel script;

  public ScriptEvaluationView() {
    initWidget(uiBinder.createAndBindUi(this));
    valuesTable.setPageSize(20);
    valuesTable.addColumn(new TextColumn<ValueDto>() {

      @Override
      public String getValue(ValueDto value) {
        return value.getValue();
      }
    }, translations.valueLabel());

  }

  @Override
  public void setSummaryTabWidget(WidgetDisplay widget) {
    summary.clear();
    summary.add(widget.asWidget());
  }

  @Override
  public void populateValues(JsArray<ValueDto> values) {
    JsArrayDataProvider<ValueDto> dataProvider = new JsArrayDataProvider<ValueDto>();
    if(values != null && valuesTable.getPageSize() < values.length()) {
      valuesTable.setPageSize(values.length());
    }
    dataProvider.addDataDisplay(valuesTable);
    dataProvider.setArray(JsArrays.toSafeArray(values));
    dataProvider.refresh();
  }

  @Override
  public void setValueType(String type) {
    valueType.setText(type);
  }

  @Override
  public void setScript(String text) {
    script.setText(text);
  }

  //
  // Widget Display methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public HandlerRegistration addNextPageClickHandler(ClickHandler handler) {
    return nextPage.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addPreviousPageClickHandler(ClickHandler handler) {
    return previousPage.addClickHandler(handler);
  }

  @Override
  public void setPageLimits(int low, int high, int count) {
    if(low == 1) {
      previousPage.addStyleName("disabled");
    } else {
      previousPage.removeStyleName("disabled");
    }
    if(high >= count) {
      nextPage.addStyleName("disabled");
    } else {
      nextPage.removeStyleName("disabled");
    }
    pageLow.setText(Integer.toString(low));
    pageHigh.setText(Integer.toString(high));
  }

}
