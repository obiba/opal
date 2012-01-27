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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TextBoxItemInputView implements ItemInputDisplay {

  private TextBox textBox;

  private EnterKeyHandler enterKeyHandler;

  public TextBoxItemInputView() {
    textBox = new TextBox();
    textBox.addStyleName("itemInput");
    textBox.addStyleName("textBox");

    addEnterKeyHandler();
  }

  public void clear() {
    textBox.setText("");
  }

  public String getItem() {
    return textBox.getText();
  }

  public Widget asWidget() {
    return textBox;
  }

  public void setEnterKeyHandler(EnterKeyHandler handler) {
    this.enterKeyHandler = handler;
  }

  private void addEnterKeyHandler() {
    textBox.addKeyDownHandler(new KeyDownHandler() {

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