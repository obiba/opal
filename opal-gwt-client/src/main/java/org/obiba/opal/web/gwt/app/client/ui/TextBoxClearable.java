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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

public class TextBoxClearable extends FlowPanel implements HasKeyUpHandlers, HasClickHandlers {

  private final TextBox textBox;

  private final Anchor clear;

  private final Anchor empty;

  public TextBoxClearable() {
    super.addStyleName("inline");

    textBox = new TextBox();
    clear = new Anchor();
    textBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        clear.setVisible(!textBox.getText().isEmpty());
        empty.setVisible(textBox.getText().isEmpty());
      }
    });
    add(textBox);

    clear.setVisible(false);
    clear.addStyleName("icon-remove-circle");
    clear.addStyleName("textbox-clearable-anchor");
    clear.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        textBox.setText("");
        clear.setVisible(false);
        empty.setVisible(true);
      }
    });

    empty = new Anchor();
    empty.setVisible(true);

    add(empty);
    add(clear);
  }

  public void setText(String text) {
    textBox.setText(text);
    empty.setVisible(text.isEmpty());
    clear.setVisible(!text.isEmpty());
  }

  public String getText() {
    return textBox.getText();
  }

  public TextBox getTextBox() {
    return textBox;
  }

  public Anchor getClear() {
    return clear;
  }

  @Override
  public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
    return addDomHandler(handler, KeyUpEvent.getType());
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  @Override
  public void addStyleName(String style) {
    textBox.addStyleName(style);
  }
}
