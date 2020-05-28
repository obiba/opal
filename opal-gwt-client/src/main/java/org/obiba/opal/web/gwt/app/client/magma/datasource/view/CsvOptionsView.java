/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.datasource.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.CsvOptionsDisplay;
import org.obiba.opal.web.gwt.app.client.ui.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.EditableListBox;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.ui.ValueTypeChooser;

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
  CollapsiblePanel advancedOptions;

  @UiField
  ControlGroup rowGroup;

  @UiField
  ControlGroup fieldGroup;

  @UiField
  ControlGroup quoteGroup;

  @UiField
  ValueTypeChooser valueType;

  @UiField
  ControlGroup charsetGroup;

  @UiField
  ControlGroup selectCsvFileGroup;

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
  public void addToSlot(Object slot, IsWidget content) {
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {

  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {

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

  @Override
  public HasText getFieldSeparator() {
    return field;
  }

  @Override
  public HasText getDefaultValueType() {
    return new HasText() {
      @Override
      public String getText() {
        return valueType.getValue();
      }

      @Override
      public void setText(String text) {
        valueType.setSelectedValue(text);
      }
    };
  }

  @Override
  public HasText getQuote() {
    return quote;
  }

  @Override
  public HasType<ControlGroupType> getGroupType(String id) {
    CsvFormField field = CsvFormField.valueOf(id);
    switch (field) {
      case FILE:
        return selectCsvFileGroup;
      case ROW:
        return rowGroup;
      case FIELD:
        return fieldGroup;
      case QUOTE:
        return quoteGroup;
      case CHARSET:
        return charsetGroup;
    }

    throw new IllegalArgumentException("Invalid field type.");
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    charsetView.setDefaultCharset(defaultCharset);
  }

  @Override
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

  @Override
  public void setCsvFileSelectorVisible(boolean value) {
    selectCsvFileGroup.setVisible(value);
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
