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

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

/**
 * A suggest box with some default settings like key shortcuts and default suggestions.
 */
public class SearchVariablesSuggestBox extends DefaultSuggestBox {

  private String text = "";

  public SearchVariablesSuggestBox() {
    this(" .,-_");
  }

  public SearchVariablesSuggestBox(String whitespaceChars) {
    super(whitespaceChars);

    getTextBox().addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        text = getTextBox().getText();

        getTextBox().setText("");
        showSuggestionList();
      }
    });
    getTextBox().addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        hideSuggestions();
        getTextBox().setText(text);
      }
    });
  }

  @Override
  public void clear() {
    //setText("");
    hideSuggestions();
  }

  public void hideSuggestions() {
    ((DefaultSuggestionDisplay) getSuggestionDisplay()).hideSuggestions();
  }

  @Override
  public MultiWordSuggestOracle getSuggestOracle() {
    return (MultiWordSuggestOracle) super.getSuggestOracle();
  }

  public void setDefaultSuggestionsEnabled(boolean enable) {
    DefaultMultiWordSuggestOracle oracle = (DefaultMultiWordSuggestOracle) super.getSuggestOracle();
    oracle.setWithDefaults(enable);
  }

  private static final class DefaultMultiWordSuggestOracle extends MultiWordSuggestOracle {

    private final List<String> defaults = new ArrayList<String>();

    private boolean withDefaults = true;

    private DefaultMultiWordSuggestOracle(String whitespaceChars) {
      super(whitespaceChars);
    }

    public void setWithDefaults(boolean withDefaults) {
      this.withDefaults = withDefaults;
      if(!withDefaults) {
        setDefaultSuggestionsFromText(new ArrayList<String>());
      }
    }

    @Override
    public void add(String suggestion) {
      super.add(suggestion);
      if(withDefaults && !defaults.contains(suggestion)) {
        defaults.add(suggestion);
        setDefaultSuggestionsFromText(defaults);
      }
    }

    @Override
    public void clear() {
      defaults.clear();
      setDefaultSuggestionsFromText(defaults);
      super.clear();
    }
  }

}
