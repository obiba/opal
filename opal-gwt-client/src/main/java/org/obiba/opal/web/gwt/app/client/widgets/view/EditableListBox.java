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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class EditableListBox extends TextBox implements HasText, HasValue<String> {

  private PopupPanel panel = new PopupPanel(true);

  private ListBox listBox = new ListBox(true);

  public EditableListBox() {
    panel.add(listBox);

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

    listBox.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        useSuggestion();
      }
    });
  }

  public void addItem(String value) {
    listBox.addItem(value);
  }

  public void removeItem(String value) {
    int index = getIndex(value);
    if(index >= 0) {
      listBox.removeItem(index);
    }
  }

  private int getIndex(String value) {
    for(int i = 0; i < listBox.getItemCount(); i++) {
      if(value.equals(listBox.getValue(i))) {
        return i;
      }
    }
    return -1;
  }

  private void displaySuggestions() {
    listBox.setSelectedIndex(getIndex(getValue()));
    panel.showRelativeTo(this);
  }

  private void useSuggestion() {
    panel.hide();
    setText(listBox.getValue(listBox.getSelectedIndex()));
    setFocus(true);
  }

}