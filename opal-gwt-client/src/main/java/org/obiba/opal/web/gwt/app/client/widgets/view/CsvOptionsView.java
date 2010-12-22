/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.CsvOptionsDisplay;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class CsvOptionsView extends Composite implements CsvOptionsDisplay {
  //
  // Static variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  private FileSelectionPresenter.Display fileSelection;

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

  @UiField
  DisclosurePanel advancedOptions;

  //
  // Constructors
  //

  public CsvOptionsView() {
    initWidget(uiBinder.createAndBindUi(this));
    populateField();

    final ValueChangeHandler<Boolean> valueChangeHandler = new CharsetValueChangeHandler();
    charsetDefault.addValueChangeHandler(valueChangeHandler);
    charsetCommonList.addValueChangeHandler(valueChangeHandler);
    charsetSpecify.addValueChangeHandler(valueChangeHandler);
  }

  public void setAdvancedOptionsVisible(boolean visible) {
    advancedOptions.setVisible(visible);
  }

  //
  // CsvOptionsDisplay Methods
  //

  public void setCsvFileSelectorWidgetDisplay(Display display) {
    selectCsvFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  public HasText getRowText() {
    return row;
  }

  public String getFieldSeparator() {
    return field.getValue(field.getSelectedIndex());
  }

  public String getQuote() {
    return quote.getItemText(quote.getSelectedIndex());
  }

  public HasValue<Boolean> isDefaultCharacterSet() {
    return charsetDefault;
  }

  public void setDefaultCharset(String defaultCharset) {
    this.defaultCharset.setInnerText(defaultCharset);
  }

  public HasValue<Boolean> isCharsetCommonList() {
    return charsetCommonList;
  }

  public String getCharsetCommonList() {
    return charsetCommonListBox.getValue(charsetCommonListBox.getSelectedIndex());
  }

  public HasValue<Boolean> isCharsetSpecify() {
    return charsetSpecify;
  }

  public HasText getCharsetSpecifyText() {
    return charsetSpecifyTextBox;
  }

  @Override
  public String getSelectedCharacterSet() {
    String charset = null;
    if(isDefaultCharacterSet().getValue()) {
      charset = this.defaultCharset.getInnerText();
    } else if(isCharsetCommonList().getValue()) {
      charset = getCharsetCommonList();
    } else if(isCharsetSpecify().getValue()) {
      charset = getCharsetSpecifyText().getText();
    }
    return charset;
  }

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
  public void resetFieldSeparator() {
    field.setSelectedIndex(0);

  }

  @Override
  public void resetQuote() {
    quote.setSelectedIndex(0);
  }

  @Override
  public void resetCommonCharset() {
    charsetCommonListBox.setSelectedIndex(0);
  }

  @Override
  public void clear() {
    if(fileSelection != null) {
      fileSelection.clearFile();
    }

    row.setText("1");
    resetFieldSeparator();
    resetQuote();

    charsetDefault.setValue(true);

    charsetCommonList.setValue(false);
    charsetCommonListBox.setEnabled(false);
    charsetCommonListBox.setSelectedIndex(0);

    charsetSpecify.setValue(false);
    charsetSpecifyTextBox.setEnabled(false);
    charsetSpecifyTextBox.setText("");
  }

  //
  // Methods
  //

  private void populateField() {
    row.setSize("2em", "1em");
    row.setText("1");
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CsvOptionsView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CsvOptionsView> {
  }

  class CharsetValueChangeHandler implements ValueChangeHandler<Boolean> {

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
      if(event.getValue() == true) {
        if(event.getSource().equals(charsetDefault)) {
          charsetCommonListBox.setEnabled(false);
          charsetSpecifyTextBox.setEnabled(false);
          charsetSpecifyTextBox.setText("");
        } else if(event.getSource().equals(charsetCommonList)) {
          charsetCommonListBox.setEnabled(true);
          charsetSpecifyTextBox.setEnabled(false);
          charsetSpecifyTextBox.setText("");
        } else if(event.getSource().equals(charsetSpecify)) {
          charsetSpecifyTextBox.setEnabled(true);
          charsetCommonListBox.setEnabled(false);
        }
      }
    }
  }
}
