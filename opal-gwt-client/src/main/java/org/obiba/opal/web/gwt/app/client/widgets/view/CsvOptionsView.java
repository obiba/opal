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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasText;
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

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private FileSelectionPresenter.Display fileSelection;

  @UiField
  SimplePanel selectCsvFilePanel;

  @UiField
  TextBox row;

  @UiField
  EditableListBox field;

  @UiField
  EditableListBox quote;

  @UiField
  CharacterSetView charsetView;

  @UiField
  DisclosurePanel advancedOptions;

  //
  // Constructors
  //

  public CsvOptionsView() {
    initWidget(uiBinder.createAndBindUi(this));
    populateField();

    for(String s : new String[] { ",", ";", ":", "tab", "|" }) {
      field.addItem(s);
    }
    for(String s : new String[] { "\"", "'" }) {
      quote.addItem(s);
    }
    clear();
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {

  }

  @Override
  public void setInSlot(Object slot, Widget content) {

  }

  public void setAdvancedOptionsVisible(boolean visible) {
    advancedOptions.setVisible(visible);
  }

  //
  // CsvOptionsDisplay Methods
  //

  @Override
  public void setCsvFileSelectorWidgetDisplay(Display display) {
    selectCsvFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public HasText getRowText() {
    return row;
  }

  @Override
  public HasText getCharsetText() {
    return charsetView.getCharsetText();
  }

  public HasText getFieldSeparatorText() {
    return field;
  }

  @Override
  public String getFieldSeparator() {
    return field.getValue();
  }

  public HasText getQuoteText() {
    return quote;
  }

  @Override
  public String getQuote() {
    return quote.getValue();
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    charsetView.setDefaultCharset(defaultCharset);
  }

  public Widget asWidget() {
    return this;
  }

  @Override
  public void resetFieldSeparator() {
    field.setValue(",");
  }

  @Override
  public void resetQuote() {
    quote.setValue("\"");
  }

  @Override
  public void resetCommonCharset() {
  }

  @Override
  public void clear() {
    if(fileSelection != null) {
      fileSelection.clearFile();
    }

    row.setText("1");
    resetFieldSeparator();
    resetQuote();
  }

  //
  // Methods
  //

  private void populateField() {
    row.setText("1");
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CsvOptionsView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CsvOptionsView> {}
}
