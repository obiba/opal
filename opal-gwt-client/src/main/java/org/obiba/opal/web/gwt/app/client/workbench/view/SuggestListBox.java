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

  private CloseableList closeables;

  private DefaultSuggestBox suggestBox;

  public SuggestListBox() {
    super();
    addStyleName("obiba-SuggestListBox");

    FlowPanel content = new FlowPanel();
    add(content);

    closeables = new CloseableList();
    content.add(closeables);

    content.add(suggestBox = new DefaultSuggestBox());
    suggestBox.setWidth("25px");
    addSuggestBoxHandlers();

    addFocusHandler(new FocusHandler() {

      @Override
      public void onFocus(FocusEvent event) {
        suggestBox.setFocus(true);
      }
    });

    setItemValidator(new DefaultItemValidator());
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
            if(addItem(suggestBox.getText())) {
              suggestBox.setText("");
            }
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

  public boolean addItem(String text) {
    return closeables.addItem(text);
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
      return text != null && !Strings.isNullOrEmpty(text.trim()) && !getItems().contains(text);
    }

  }

}
