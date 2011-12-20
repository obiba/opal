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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class SuggestListBox extends FlowPanel {

  private UList menu;

  private List<ListItem> items;

  private DefaultSuggestBox suggestBox;

  public SuggestListBox() {
    super();
    addStyleName("obiba-SuggestListBox");

    menu = new UList();
    super.add(getMenu());

    super.add(suggestBox = new DefaultSuggestBox());

    suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

      @Override
      public void onSelection(SelectionEvent<Suggestion> event) {
        addItem(suggestBox.getText());
      }
    });

    suggestBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          if(!Strings.isNullOrEmpty(suggestBox.getText())) {
            addItem(suggestBox.getText());
            suggestBox.setText("");
          }
        }
      }
    });

    suggestBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        String width = (25 + 6 * suggestBox.getText().length()) + "px";
        suggestBox.setWidth(width);
      }
    });

    suggestBox.getTextBox().addFocusHandler(new FocusHandler() {

      @Override
      public void onFocus(FocusEvent event) {
        suggestBox.setText("");
      }
    });

    suggestBox.setWidth("25px");

    suggestBox.getTextBox().addBlurHandler(new BlurHandler() {

      @Override
      public void onBlur(BlurEvent event) {
        suggestBox.setText("");
      }
    });

    items = new ArrayList<ListItem>();
  }

  public MultiWordSuggestOracle getSuggestOracle() {
    return suggestBox.getSuggestOracle();
  }

  public void addItem(final String text) {
    if(Strings.isNullOrEmpty(text)) return;

    final ListItem item = new ListItem();

    item.add(new InlineLabel(text));
    Anchor close = new Anchor("x");
    close.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if(items.contains(item)) {
          items.remove(item);
          menu.remove(item);
        }
      }
    });
    item.add(close);

    menu.add(item);
    items.add(item);
  }

  public void removeItem(String text) {
    ListItem it = null;
    for(ListItem item : items) {
      Widget label = item.getWidget(0);
      if(label instanceof HasText && ((HasText) label).getText().equals(text)) {
        it = item;
        break;
      }
    }

    if(it != null) {
      items.remove(it);
      menu.remove(it);
    }
  }

  protected Widget getMenu() {
    return menu;
  }

}
