/*******************************************************************************
 * Copyright 2011(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Collection;
import java.util.List;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;

public class EditableListBox extends Composite implements HasText, HasValue<String>, HasEnabled {

  private final Typeahead suggestBox;

  private final TextBox textBox;

  private final List<String> items = Lists.newArrayList();

  public EditableListBox() {
    suggestBox = new Typeahead();
    suggestBox.setWidget(textBox = new TextBox());
    suggestBox.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        textBox.setValue(selectedSuggestion.getReplacementString(), true);
        return selectedSuggestion.getReplacementString();
      }
    });
    FlowPanel layout = new FlowPanel();
    layout.add(suggestBox);
    initWidget(layout);
  }

  public void setTextStyleNames(String style) {
    textBox.addStyleName(style);
  }

  public boolean hasItem(String value) {
    return items.contains(value);
  }

  public MultiWordSuggestOracle getSuggestOracle() {
    return (MultiWordSuggestOracle) suggestBox.getSuggestOracle();
  }

  public final void addAllItems(Collection<String> collection) {
    getSuggestOracle().addAll(collection);
    items.addAll(collection);
  }

  public void addItem(String value) {
    getSuggestOracle().add(value);
    items.add(value);
  }

  public void removeItem(String value) {
    items.remove(value);
    getSuggestOracle().clear();
    for (String i : items) {
      getSuggestOracle().add(i);
    }
  }

  public void clear() {
    getSuggestOracle().clear();
    items.clear();
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
    return textBox.getValue();
  }

  @Override
  public void setValue(String value) {
    textBox.setValue(value);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    textBox.setValue(value, fireEvents);
  }

  @Override
  public String getText() {
    return textBox.getText();
  }

  @Override
  public void setText(String text) {
    textBox.setText(text);
  }

  @Override
  public void setEnabled(boolean enabled) {
    textBox.setEnabled(enabled);
  }

  @Override
  public boolean isEnabled() {
    return textBox.isEnabled();
  }

}