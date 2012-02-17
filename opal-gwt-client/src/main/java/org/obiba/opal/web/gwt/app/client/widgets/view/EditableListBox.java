/*******************************************************************************
 * Copyright 2011(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class EditableListBox extends Composite implements HasText, HasValue<String>, HasEnabled {

  private TextBox textBox = new TextBox();

  private Button ddBtn = new Button();

  private PopupPanel panel = new PopupPanel(true);

  private MenuBar menuBar = new MenuBar(true);

  private Map<String, MenuItem> menuItemsMap = new HashMap<String, MenuItem>();

  public EditableListBox() {
    FlowPanel layout = new FlowPanel();
    ddBtn.setStyleName("btn icon-before i-sortasc");
    ddBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        textBox.setFocus(true);
        displaySuggestions();
      }
    });

    layout.add(textBox);
    layout.add(ddBtn);

    initWidget(layout);

    textBox.addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        int key = event.getNativeKeyCode();
        if(key == KeyCodes.KEY_DOWN || (event.isControlKeyDown() && key == ' ')) {
          displaySuggestions();
        } else {
          panel.hide();
        }
      }
    });

    panel.add(menuBar);
    panel.setStyleName("obiba-EditableListBox gwt-MenuBarPopup");

    setStylePrimaryName("obiba-EditableListBox");
  }

  public void setTextStyleNames(String style) {
    textBox.addStyleName(style);
  }

  public boolean hasItem(final String value) {
    return menuItemsMap.containsKey(value);
  }

  public void addItem(final String value) {
    if(menuItemsMap.containsKey(value)) {
      removeItem(value);
    }
    MenuItem item = new MenuItem(value, new Command() {

      @Override
      public void execute() {
        panel.hide();
        textBox.setValue(value, true);
        textBox.setFocus(true);
      }
    });
    menuBar.addItem(item);
    menuItemsMap.put(value, item);
  }

  public void removeItem(String value) {
    MenuItem item = menuItemsMap.get(value);
    if(value != null) {
      menuBar.removeItem(item);
      menuItemsMap.remove(value);
    }
  }

  public void clear() {
    for(String value : menuItemsMap.keySet()) {
      menuBar.removeItem(menuItemsMap.get(value));
    }
    menuItemsMap.clear();
    setValue("");
  }

  private void displaySuggestions() {
    panel.showRelativeTo(textBox);
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
    ddBtn.setEnabled(enabled);
    panel.hide();
  }

  @Override
  public boolean isEnabled() {
    return textBox.isEnabled();
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    panel.hide();
  }
}