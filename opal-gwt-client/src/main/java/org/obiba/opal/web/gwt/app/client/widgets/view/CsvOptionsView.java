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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
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

  //
  // Constructors
  //

  public CsvOptionsView() {
    initWidget(uiBinder.createAndBindUi(this));
    populateField();
  }

  //
  // CsvOptionsDisplay Methods
  //

  public void setCsvFileSelectorWidgetDisplay(Display display) {
    selectCsvFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  public void setDefaultCharset(String defaultCharset) {
    this.defaultCharset.setInnerText(defaultCharset);
  }

  public boolean isDefaultCharacterSet() {
    return charsetDefault.getValue();
  }

  public String getFieldSeparator() {
    return field.getValue(field.getSelectedIndex());
  }

  public String getQuote() {
    return quote.getItemText(quote.getSelectedIndex());
  }

  public HasText getRowText() {
    return row;
  }

  public HasText getCharsetSpecifyText() {
    return charsetSpecifyTextBox;
  }

  public boolean isCharsetCommonList() {
    return charsetCommonList.getValue();
  }

  public boolean isCharsetSpecify() {
    return charsetSpecify.getValue();
  }

  public String getCharsetCommonList() {
    return charsetCommonListBox.getValue(charsetCommonListBox.getSelectedIndex());
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
}
