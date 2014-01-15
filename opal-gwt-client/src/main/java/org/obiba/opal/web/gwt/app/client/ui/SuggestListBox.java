/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Collection;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.ui.CloseableList.ItemRemovedHandler;

import com.github.gwtbootstrap.client.ui.NavWidget;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.github.gwtbootstrap.client.ui.constants.Constants;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SuggestBox;
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
    aheadBox.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        String rval = updaterCallback.onSelection(selectedSuggestion);
        textBox.setFocus(true);
        return rval;
      }
    });
  }

  public List<String> getSelectedItemsTexts() {
    return closeables.getItemTexts();
  }

  private void rebuildSuggestBox() {
    if(textBox != null) {
      // do this because not able to clear suggest box text
      content.remove(textBox);
    }

    textBox = new TextBox();
    aheadBox = new Typeahead(oracle);
    aheadBox.add(textBox);
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
          // remove focus
          closeables.removeLastItemFocus();

          // Lose focus on enter
          textBox.setFocus(event.getNativeKeyCode() != KeyCodes.KEY_ENTER);
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

  public boolean addItem(String text, VariableSearchListItem.ItemType type) {
    return closeables.addItem(text, false, type);
  }

  public boolean addItem(String text) {
    return closeables.addItem(text, false);
  }

  @SuppressWarnings("UnusedDeclaration")
  public SuggestOracle getSuggestOracle() {
    return aheadBox.getSuggestOracle();
  }

  public void addItemRemovedHandler(ItemRemovedHandler handler) {
    closeables.addItemRemovedHandler(handler);
  }

  private static class SuggestionDisplayImpl extends SuggestBox.SuggestionDisplay {

    private final UnorderedList suggestionMenu;

    private Collection<? extends SuggestOracle.Suggestion> suggestions;

    SuggestionDisplayImpl(UnorderedList suggestionMenu) {
      this.suggestionMenu = suggestionMenu;
      hideSuggestions();
    }

    @Override
    protected SuggestOracle.Suggestion getCurrentSelection() {
      int active = getActiveMenuItemIndex();
      if(active >= 0) {
        return ((NavSuggestion) suggestionMenu.getWidget(active)).getSuggestion();
      }
      if(suggestions != null && suggestions.size() > 0) {
        return suggestions.iterator().next();
      }
      return null;
    }

    @Override
    protected void hideSuggestions() {
      suggestionMenu.setVisible(false);
    }

    @Override
    protected void moveSelectionDown() {
      if(suggestionMenu.isVisible()) {
        if(suggestionMenu.isVisible()) {
          int active = getActiveMenuItemIndex();
          if(active >= 0 && active < suggestionMenu.getWidgetCount() - 1) {
            setActiveMenuItem(active, active + 1);
          }
        }
      }
    }

    @Override
    protected void moveSelectionUp() {
      if(suggestionMenu.isVisible()) {
        int active = getActiveMenuItemIndex();
        if(active > 0) {
          setActiveMenuItem(active, active - 1);
        }
      }
    }

    private int getActiveMenuItemIndex() {
      for(int i = 0; i < suggestionMenu.getWidgetCount(); i++) {
        NavWidget menuItem = (NavWidget) suggestionMenu.getWidget(i);
        if(menuItem.getElement().hasAttribute("class") &&
            menuItem.getElement().getClassName().equals(Constants.ACTIVE)) {
          return i;
        }
      }
      return -1;
    }

    private void setActiveMenuItem(int currentIdx, int idx) {
      suggestionMenu.getWidget(currentIdx).removeStyleName(Constants.ACTIVE);
      suggestionMenu.getWidget(idx).addStyleName(Constants.ACTIVE);
    }

    @Override
    protected void showSuggestions(SuggestBox suggestBox, Collection<? extends SuggestOracle.Suggestion> suggestions,
        boolean isDisplayStringHTML, boolean isAutoSelectEnabled, final SuggestBox.SuggestionCallback callback) {
      suggestionMenu.clear();
      this.suggestions = suggestions;

      // Hide the popup if there are no suggestions to display.
      boolean anySuggestions = suggestions != null && suggestions.size() > 0;
      if(!anySuggestions) {
        hideSuggestions();
        return;
      }

      for(final SuggestOracle.Suggestion curSuggestion : suggestions) {
        NavWidget menuItem = new NavSuggestion(curSuggestion);
        menuItem.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            callback.onSuggestionSelected(curSuggestion);
          }
        });
        suggestionMenu.add(menuItem);
        if(suggestionMenu.getWidgetCount() == 1) {
          menuItem.addStyleName(Constants.ACTIVE);
        }
      }

      suggestionMenu.setVisible(true);
    }

  }

  private static class NavSuggestion extends NavWidget {

    private final SuggestOracle.Suggestion suggestion;

    private NavSuggestion(SuggestOracle.Suggestion suggestion) {
      super(new HTMLPanel(suggestion.getDisplayString()));
      this.suggestion = suggestion;
    }

    private SuggestOracle.Suggestion getSuggestion() {
      return suggestion;
    }
  }

}
