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

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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

  private CloseableList menu;

  private DefaultSuggestBox suggestBox;

  public SuggestListBox() {
    super();
    addStyleName("obiba-SuggestListBox");

    menu = new CloseableList();
    super.add(menu);

    super.add(suggestBox = new DefaultSuggestBox());
    suggestBox.setWidth("25px");
    addSuggestBoxHandlers();
  }

  private void addSuggestBoxHandlers() {

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

    suggestBox.getTextBox().addBlurHandler(new BlurHandler() {

      @Override
      public void onBlur(BlurEvent event) {
        suggestBox.setText("");
      }
    });
  }

  public void addItem(String text) {
    menu.addItem(text);
  }

  public void removeItem(String text) {
    menu.removeItem(text);
  }

  public List<String> getItemTexts() {
    return menu.getItemTexts();
  }

  public MultiWordSuggestOracle getSuggestOracle() {
    return suggestBox.getSuggestOracle();
  }

  private final class CloseableList extends UList {

    public CloseableList() {
      super();
      addStyleName("closeables");
    }

    public void addItem(final String text) {
      if(Strings.isNullOrEmpty(text)) return;

      final ListItem item = new ListItem();

      item.add(new InlineLabel(text));
      Anchor close = new Anchor("x");
      close.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          CloseableList.this.remove(item);
        }
      });
      item.add(close);

      add(item);
    }

    public void removeItem(String text) {
      ListItem it = getItem(text);

      if(it != null) {
        remove(it);
      }
    }

    private ListItem getItem(String text) {
      ListItem it = null;
      for(int i = 0; i < getWidgetCount(); i++) {
        ListItem item = (ListItem) getWidget(i);
        Widget label = item.getWidget(0);
        if(label instanceof HasText && ((HasText) label).getText().equals(text)) {
          it = item;
          break;
        }
      }
      return it;
    }

    public List<String> getItemTexts() {
      ImmutableList.Builder<String> builder = ImmutableList.<String> builder();
      for(int i = 0; i < getWidgetCount(); i++) {
        ListItem item = (ListItem) getWidget(i);
        Widget label = item.getWidget(0);
        if(label instanceof HasText) {
          builder.add(((HasText) label).getText());
        }
      }
      return builder.build();
    }
  }

}
