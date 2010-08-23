/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.CsvFormatStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CsvFormatStepView extends Composite implements CsvFormatStepPresenter.Display {

  @UiTemplate("CsvFormatStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CsvFormatStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private FileSelectionPresenter.Display fileSelection;

  @UiField
  Button nextButton;

  @UiField
  SimplePanel selectCsvFilePanel;

  @UiField
  TextBox row;

  @UiField
  ListBox field;

  @UiField
  ListBox quote;

  @UiField
  RadioButton charsetDefault;

  @UiField
  RadioButton charsetCommonList;

  @UiField
  ListBox charsetCommonListBox;

  @UiField
  RadioButton charsetSpecify;

  @UiField
  SpanElement defaultCharset;

  @UiField
  TextBox charsetSpecifyTextBox;

  public CsvFormatStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    populateField();
  }

  private void populateField() {
    row.setSize("2em", "1em");
    row.setText("1");
  }

  @Override
  public void setCsvFileSelectorWidgetDisplay(Display display) {
    selectCsvFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

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
  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return nextButton.addClickHandler(handler);
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    this.defaultCharset.setInnerText(defaultCharset);
  }

  @Override
  public boolean isDefaultCharacterSet() {
    return charsetDefault.getValue();
  }

  @Override
  public String getFieldSeparator() {
    return field.getValue(field.getSelectedIndex());
  }

  @Override
  public String getQuote() {
    return quote.getItemText(quote.getSelectedIndex());
  }

  @Override
  public HasText getRowText() {
    return row;
  }

  @Override
  public void setNextEnabled(boolean enabled) {
    nextButton.setEnabled(enabled);
  }

  @Override
  public HasText getCharsetSpecifyText() {
    return charsetSpecifyTextBox;
  }

  @Override
  public boolean isCharsetCommonList() {
    return charsetCommonList.getValue();
  }

  @Override
  public boolean isCharsetSpecify() {
    return charsetSpecify.getValue();
  }

  @Override
  public String getCharsetCommonList() {
    return charsetCommonListBox.getValue(charsetCommonListBox.getSelectedIndex());
  }

}
