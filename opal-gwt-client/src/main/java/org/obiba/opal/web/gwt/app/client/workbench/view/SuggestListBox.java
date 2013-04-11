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

import org.obiba.opal.web.gwt.app.client.workbench.view.CloseableList.ItemRemovedHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.CloseableList.ItemValidator;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 *
 */
public class SuggestListBox extends FocusPanel {

  private final FlowPanel content;

  private final CloseableList closeables;

  private DefaultSuggestBox suggestBox;

  private final List<String> suggestions = new ArrayList<String>();

  private boolean strict = true;

  public SuggestListBox() {
    addStyleName("obiba-SuggestListBox");

    content = new FlowPanel();
    add(content);

    closeables = new CloseableList();
    content.add(closeables);
    closeables.addItemRemovedHandler(new ItemRemovedHandler() {

      @Override
      public void onItemRemoved(String text) {
        rebuildSuggestBox();
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

    setItemValidator(new DefaultItemValidator());
  }

  /**
   * Strict if only what is suggested can be added (default is true).
   *
   * @param strict
   */
  public void setStrict(boolean strict) {
    this.strict = strict;
  }

  private void rebuildSuggestBox() {
    if(suggestBox != null) {
      // do this because not able to clear suggest box text
      content.remove(suggestBox);
      suggestBox.hideSuggestions();
    }

    content.add(suggestBox = new DefaultSuggestBox());
    suggestBox.setWidth("25px");
    suggestBox.setDefaultSuggestionsEnabled(false);
    addSuggestBoxHandlers();
    for(String suggestion : suggestions) {
      if(!getItems().contains(suggestion)) {
        getSuggestOracle().add(suggestion);
      }
    }
  }

  private void addSuggestBoxHandlers() {

    suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

      @Override
      public void onSelection(SelectionEvent<Suggestion> event) {
        closeables.addItem(suggestBox.getText());
        rebuildSuggestBox();
        suggestBox.setFocus(true);
      }
    });

    suggestBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          if(!Strings.isNullOrEmpty(suggestBox.getText())) {
            closeables.addItem(suggestBox.getText());
            rebuildSuggestBox();
            suggestBox.setFocus(true);
          }
        } else if(event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
          if(Strings.isNullOrEmpty(suggestBox.getText())) {
            closeables.focusOrRemoveLastItem();
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
        suggestBox.clear();
      }
    });

    suggestBox.getTextBox().addBlurHandler(new BlurHandler() {

      @Override
      public void onBlur(BlurEvent event) {
        suggestBox.clear();
      }
    });
  }

  @Override
  public void clear() {
    closeables.clear();
    suggestions.clear();
    rebuildSuggestBox();
  }

  public void addSuggestion(String suggestion) {
    if(suggestions.contains(suggestion)) return;

    suggestions.add(suggestion);
    if(!getItems().contains(suggestion)) {
      getSuggestOracle().add(suggestion);
    }
  }

  public boolean addItem(String text) {
    return closeables.addItem(text, false);
  }

  public void removeItem(String text) {
    closeables.removeItem(text);
  }

  public List<String> getItems() {
    return closeables.getItemTexts();
  }

  public MultiWordSuggestOracle getSuggestOracle() {
    return suggestBox.getSuggestOracle();
  }

  public void setItemValidator(ItemValidator validator) {
    closeables.setItemValidator(validator);
  }

  public void addItemRemovedHandler(ItemRemovedHandler handler) {
    closeables.addItemRemovedHandler(handler);
  }

  public void removeItemRemovedHandler(ItemRemovedHandler handler) {
    closeables.removeItemRemovedHandler(handler);
  }

  /**
   * Validates text is not empty or blank and is unique.
   */
  public class DefaultItemValidator implements ItemValidator {

    @Override
    public boolean validate(String text) {
      if(text == null || Strings.isNullOrEmpty(text.trim()) || getItems().contains(text)) return false;
      return !strict || suggestions.contains(text);
    }

  }

}
