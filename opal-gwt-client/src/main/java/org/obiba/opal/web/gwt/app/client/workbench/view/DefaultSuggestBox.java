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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;

/**
 * A suggest box with some default settings like key shortcuts and default suggestions.
 */
public class DefaultSuggestBox extends SuggestBox {

  public DefaultSuggestBox() {
    this(" .,-_");
  }

  public DefaultSuggestBox(String whitespaceChars) {
    super(new DefaultMultiWordSuggestOracle(whitespaceChars));

    addKeyUpHandler(new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.isControlKeyDown() && event.getNativeEvent().getCharCode() == 0) {
          DefaultSuggestBox.this.showSuggestionList();
        } else if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          ((DefaultSuggestionDisplay) DefaultSuggestBox.this.getSuggestionDisplay()).hideSuggestions();
        }

      }
    });
  }

  public MultiWordSuggestOracle getSuggestOracle() {
    return (MultiWordSuggestOracle) super.getSuggestOracle();
  }

  private static final class DefaultMultiWordSuggestOracle extends MultiWordSuggestOracle {

    private List<String> defaults = new ArrayList<String>();

    public DefaultMultiWordSuggestOracle() {
      super();
    }

    public DefaultMultiWordSuggestOracle(String whitespaceChars) {
      super(whitespaceChars);
    }

    @Override
    public void add(String suggestion) {
      super.add(suggestion);
      if(!defaults.contains(suggestion)) {
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
