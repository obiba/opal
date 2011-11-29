/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEvaluationPopupPresenter.Display;
import org.obiba.opal.web.gwt.prettify.client.PrettyPrintLabel;
import org.obiba.opal.web.model.client.magma.ValueDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class ScriptEvaluationPopupView extends DialogBox implements Display {

  @UiTemplate("ScriptEvaluationPopupView.ui.xml")
  interface ViewUiBinder extends UiBinder<FlowPanel, ScriptEvaluationPopupView> {
  }

  private static String DIALOG_WIDTH = "70em";

  private static String DIALOG_HEIGHT = "40em";

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  FlowPanel content;

  @UiField
  DockLayoutPanel dock;

  @UiField
  CellTable<ValueDto> valuesTable;

  @UiField
  PrettyPrintLabel script;

  @UiField
  Button closeButton;

  @UiField
  Anchor previousPage;

  @UiField
  Anchor nextPage;

  @UiField
  Label pageLow;

  @UiField
  Label pageHigh;

  public ScriptEvaluationPopupView() {
    setHeight(DIALOG_HEIGHT);
    setWidth(DIALOG_WIDTH);
    setText(translations.scriptLabel());
    content = uiBinder.createAndBindUi(this);
    dock.setHeight(DIALOG_HEIGHT);
    dock.setWidth(DIALOG_WIDTH);
    valuesTable.addColumn(new TextColumn<ValueDto>() {

      @Override
      public String getValue(ValueDto value) {
        return value.getValue();
      }
    }, translations.valueLabel());
    content.add(dock);
    add(content);
  }

  @Override
  public void populateValues(JsArray<ValueDto> values) {
    JsArrayDataProvider<ValueDto> dataProvider = new JsArrayDataProvider<ValueDto>();
    dataProvider.addDataDisplay(valuesTable);
    dataProvider.setArray(JsArrays.toSafeArray(values));
    dataProvider.refresh();
  }

  @Override
  public HasClickHandlers getButton() {
    return closeButton;
  }

  @Override
  public void showDialog() {
    center();
    show();
  }

  @Override
  public void closeDialog() {
    hide();
  }

  @Override
  public void setScript(String script) {
    this.script.setText(script);
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

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }
}
