/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

/**
 *
 */
public class DropdownSuggestBox extends Composite implements HasText, HasValue<String> {

  private final Typeahead suggestBox;

  private final TextBox textBox;

  public DropdownSuggestBox() {
    suggestBox = new Typeahead();
    suggestBox.add(textBox = new TextBox());

    FlowPanel layout = new FlowPanel();
    layout.add(suggestBox);
    initWidget(layout);
  }

  @Override
  public String getText() {
    return textBox.getText();
  }

  @Override
  public void setText(String text) {
    textBox.setText(text);
  }

  public void clear() {
    textBox.setText("");
  }

  public MultiWordSuggestOracle getSuggestOracle() {
    return (MultiWordSuggestOracle)suggestBox.getSuggestOracle();
  }

  public String getId() {
    return getElement().getId();
  }

  public void setId(String id) {
    getElement().setId(id);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return textBox.addValueChangeHandler(handler);
  }

  @Override
  public String getValue() {
    return getText();
  }

  @Override
  public void setValue(String value) {
    setText(value);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    textBox.setValue(value, fireEvents);
  }

}
