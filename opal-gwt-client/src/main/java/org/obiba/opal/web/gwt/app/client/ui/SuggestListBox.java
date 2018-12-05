/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.ui.CloseableList.ItemRemovedHandler;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
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
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 *
 */
public class SuggestListBox extends FocusPanel {

  private final FlowPanel content;

  private final CloseableList closeables;

  private Typeahead aheadBox;

  private TextBox textBox;

  private final Anchor clear;

  private final Anchor empty;

  private final SuggestOracle oracle;

  private Typeahead.UpdaterCallback updaterCallback;

  private boolean readOnly;

  public SuggestListBox(SuggestOracle oracle) {
    this.oracle = oracle;
    addStyleName("obiba-SuggestListBox");

    content = new FlowPanel();

    closeables = new CloseableList();
    content.add(closeables);
    closeables.addItemRemovedHandler(new ItemRemovedHandler() {

      @Override
      public void onItemRemoved(ListItem item) {
        textBox.setFocus(true);
      }
    });

    rebuildSuggestBox();

    addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        textBox.setFocus(true);
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
        textBox.setText("");
        clear.setVisible(false);
        empty.setVisible(true);
        textBox.setFocus(false);
      }
    });
    content.add(clear);

    empty = new Anchor();
    empty.setVisible(true);
    content.add(empty);

    add(content);
  }

  public TextBox getTextBox() {
    return textBox;
  }

  public void setUpdaterCallback(final Typeahead.UpdaterCallback updaterCallback) {
    this.updaterCallback = new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        String rval = updaterCallback.onSelection(selectedSuggestion);
        textBox.setFocus(true);
        return rval;
      }
    };
    aheadBox.setUpdaterCallback(this.updaterCallback);
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    closeables.setReadOnly(readOnly);
  }

  public List<String> getSelectedItemsTexts() {
    return closeables.getItemTexts();
  }

  public void setDisplayItemCount(int limit) {
    aheadBox.setDisplayItemCount(limit);
  }

  private void rebuildSuggestBox() {
    if(textBox != null) {
      // do this because not able to clear suggest box text
      content.remove(textBox);
    }

    textBox = new TextBox();
    aheadBox = new Typeahead(oracle);
    aheadBox.add(textBox);
    aheadBox.setDisplayItemCount(11);

    // Set matchercallback to always return true so TypeHead does not filter values furthermore
    aheadBox.setMatcherCallback(new Typeahead.MatcherCallback() {
      @Override
      public boolean compareQueryToItem(String query, String item) {
        return true;
      }
    });

    content.add(aheadBox);
    addSuggestBoxHandlers();
  }

  private void addSuggestBoxHandlers() {
    textBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
          if(Strings.isNullOrEmpty(textBox.getText())) {
            closeables.focusOrRemoveLastItem();
          }
        } else {

          if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            onEnterSelection();
          }

          // remove focus
          closeables.removeLastItemFocus();

          // Lose focus on enter
          textBox.setFocus(event.getNativeKeyCode() != KeyCodes.KEY_ENTER);
        }
      }

      public void onEnterSelection() {
        SuggestOracle.Suggestion selectedSuggestion = ((VariableSuggestOracle) oracle).getSelectedSuggestion();
        if(selectedSuggestion != null) {
          updaterCallback.onSelection(selectedSuggestion);
        }
      }
    });

    textBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        clear.setVisible(!textBox.getText().isEmpty());
        empty.setVisible(textBox.getText().isEmpty());
      }
    });
  }

  @Override
  public void clear() {
    closeables.clear();
  }

  public boolean addItem(String text) {
    return closeables.addItem(text, false);
  }

  public void addItem(String text, ListItem item) {
    closeables.addItem(text, item);
  }

  @SuppressWarnings("UnusedDeclaration")
  public SuggestOracle getSuggestOracle() {
    return aheadBox.getSuggestOracle();
  }

  public void addItemRemovedHandler(ItemRemovedHandler handler) {
    closeables.addItemRemovedHandler(handler);
  }

}
