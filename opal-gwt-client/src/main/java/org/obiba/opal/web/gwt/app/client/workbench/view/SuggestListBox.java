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

import org.obiba.opal.web.gwt.app.client.workbench.view.CloseableList.ItemRemovedHandler;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 *
 */
public class SuggestListBox extends FocusPanel {

  private final FlowPanel content;

  private final CloseableList closeables;

  private SuggestBox suggestBox;

  private final Anchor clear;

  private final Anchor empty;

  private final SuggestOracle oracle;

  private boolean strict = true;

  public SuggestListBox(SuggestOracle oracle) {
    this.oracle = oracle;
    addStyleName("obiba-SuggestListBox");

    content = new FlowPanel();

    closeables = new CloseableList();
    content.add(closeables);
    closeables.addItemRemovedHandler(new ItemRemovedHandler() {

      @Override
      public void onItemRemoved(ListItem item) {
        suggestBox.setFocus(true);
      }
    });

    rebuildSuggestBox();

    addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        suggestBox.setFocus(true);
      }
    });

    clear = new Anchor();
    clear.setVisible(false);
    clear.addStyleName("icon-remove-circle");
    clear.addStyleName("textbox-clearable-anchor");
    clear.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        closeables.clear();
        suggestBox.setText("");
        clear.setVisible(false);
        empty.setVisible(true);
        suggestBox.setFocus(false);
      }
    });
    content.add(clear);

    empty = new Anchor();
    empty.addStyleName("textbox-clearable-empty");
    empty.setVisible(true);
    content.add(empty);

    add(content);
  }

  /**
   * Strict if only what is suggested can be added (default is true).
   *
   * @param strict
   */
  public void setStrict(boolean strict) {
    this.strict = strict;
  }

  public SuggestBox getSuggestBox() {
    return suggestBox;
  }

  public List<String> getSelectedItemsTexts() {
    return closeables.getItemTexts();
  }

  private void rebuildSuggestBox() {
    if(suggestBox != null) {
      // do this because not able to clear suggest box text
      content.remove(suggestBox);
    }

    content.add(suggestBox = new SuggestBox(oracle));
    addSuggestBoxHandlers();

  }

  private void addSuggestBoxHandlers() {

    suggestBox.getValueBox().addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
          if(Strings.isNullOrEmpty(suggestBox.getText())) {
            closeables.focusOrRemoveLastItem();
          }
        } else {
          // remove focus
          closeables.removeLastItemFocus();

          // Lose focus on enter
          suggestBox.setFocus(event.getNativeKeyCode() != KeyCodes.KEY_ENTER);
        }
      }
    });

    suggestBox.getValueBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        clear.setVisible(!suggestBox.getValueBox().getText().isEmpty());
        empty.setVisible(suggestBox.getValueBox().getText().isEmpty());
      }
    });

  }

  @Override
  public void clear() {
    closeables.clear();
  }

  public boolean addItem(String text, VariableSearchListItem.ItemType type) {
    return closeables.addItem(text, false, type);
  }

  public boolean addItem(String text) {
    return closeables.addItem(text, false);
  }

  public SuggestOracle getSuggestOracle() {
    return suggestBox.getSuggestOracle();
  }

  public void addItemRemovedHandler(ItemRemovedHandler handler) {
    closeables.addItemRemovedHandler(handler);
  }

}
