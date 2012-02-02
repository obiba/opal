/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

/**
 *
 */
public class DropdownSuggestBox extends Composite implements HasText, HasValue<String> {

  private DefaultSuggestBox suggestBox;

  public DropdownSuggestBox() {
    super();
    this.suggestBox = new DefaultSuggestBox();
    FlowPanel layout = new FlowPanel();
    final Button ddBtn = new Button();
    ddBtn.setStyleName("btn icon-before i-sortasc");
    ddBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        suggestBox.setFocus(true);
        suggestBox.showSuggestionList();
      }
    });

    layout.add(suggestBox);
    layout.add(ddBtn);

    initWidget(layout);

    setStylePrimaryName("obiba-DropdownSuggestBox");
  }

  public void setTextStyleNames(String style) {
    suggestBox.addStyleName(style);
  }

  @Override
  public String getText() {
    return suggestBox.getText();
  }

  @Override
  public void setText(String text) {
    suggestBox.setText(text);
  }

  public MultiWordSuggestOracle getSuggestOracle() {
    return (MultiWordSuggestOracle) suggestBox.getSuggestOracle();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return suggestBox.addValueChangeHandler(handler);
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
    suggestBox.setValue(value, fireEvents);
  }
}
