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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class EditableListBox extends TextBox implements HasText, HasValue<String> {

  private PopupPanel panel = new PopupPanel(true);

  private MenuBar menuBar = new MenuBar(true);

  private Map<String, MenuItem> menuItemsMap = new HashMap<String, MenuItem>();

  public EditableListBox() {
    panel.add(menuBar);
    panel.setStyleName("gwt-MenuBarPopup");

    addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        int key = event.getNativeKeyCode();
        String value = getValue();
        if(value == null || value.isEmpty() || (value.length() == 1 && key == KeyCodes.KEY_BACKSPACE)) {
          displaySuggestions();
        } else {
          panel.hide();
        }
      }
    });

    addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        displaySuggestions();
      }
    });
  }

  public void addItem(final String value) {
    if(menuItemsMap.containsKey(value)) {
      removeItem(value);
    }
    MenuItem item = new MenuItem(value, new Command() {

      @Override
      public void execute() {
        panel.hide();
        EditableListBox.this.setText(value);
        EditableListBox.this.setFocus(true);
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

  private void displaySuggestions() {
    panel.showRelativeTo(this);
  }
}