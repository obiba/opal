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

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ItemSelectorPresenter.EnterKeyHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ItemSelectorPresenter.ItemInputDisplay;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueItemInputView extends Composite implements ItemInputDisplay {
  //
  // Instance Variables
  //

  private TextBox keyTextBox;

  private TextBox valueTextBox;

  private EnterKeyHandler enterKeyHandler;

  //
  // Constructors
  //

  public KeyValueItemInputView() {
    keyTextBox = new TextBox();
    String keyTextBoxId = HTMLPanel.createUniqueId();
    keyTextBox.getElement().setId(keyTextBoxId);

    valueTextBox = new TextBox();
    String valueTextBoxId = HTMLPanel.createUniqueId();
    valueTextBox.getElement().setId(valueTextBoxId);

    String html =
    /**/"<span id='" + keyTextBoxId + "'></span>" +
    /**/"<span>=</span>" +
    /**/"<span id='" + valueTextBoxId + "'></span>";

    HTMLPanel container = new HTMLPanel(html);
    container.addStyleName("itemInput");
    container.addStyleName("keyValue");
    container.add(keyTextBox, keyTextBoxId);
    container.add(valueTextBox, valueTextBoxId);
    initWidget(container);

    addEnterKeyHandler();
  }

  //
  // ItemInputDisplay Methods
  //

  public void clear() {
    keyTextBox.setText("");
    valueTextBox.setText("");

    keyTextBox.setFocus(true);
  }

  public String getItem() {
    String key = keyTextBox.getText().trim();
    String value = valueTextBox.getText().trim();

    return (key.length() != 0 && value.length() != 0) ? keyTextBox.getText() + " = " + valueTextBox.getText() : "";
  }

  public Widget asWidget() {
    return this;
  }

  public void setEnterKeyHandler(EnterKeyHandler handler) {
    this.enterKeyHandler = handler;
  }

  //
  // Methods
  //

  private void addEnterKeyHandler() {
    valueTextBox.addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          if(enterKeyHandler != null) {
            enterKeyHandler.enterKeyPressed();
          }
        }
      }
    });
  }
}