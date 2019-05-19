/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.view;

import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter.EnterKeyHandler;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter.ItemInputDisplay;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueItemInputView implements ItemInputDisplay {

  @UiTemplate("KeyValueItemInputView.ui.xml")
  interface MyUiBinder extends UiBinder<HTMLPanel, KeyValueItemInputView> {}

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final HTMLPanel container;

  @UiField
  Typeahead keyTypeahead;

  @UiField
  TextBox keyTextBox;

  @UiField
  Typeahead valueTypeahead;

  @UiField
  TextBox valueTextBox;

  private EnterKeyHandler enterKeyHandler;

  private final Map<String, List<String>> suggestions;

  public KeyValueItemInputView() {
    this(null);
  }

  public KeyValueItemInputView(Map<String, List<String>> suggestions) {
    container = uiBinder.createAndBindUi(this);
    this.suggestions = suggestions;

    addEnterKeyHandler();
    if (suggestions != null) {
      MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) keyTypeahead.getSuggestOracle();
      oracle.addAll(suggestions.keySet());
    }
  }

  @Override
  public void clear() {
    keyTextBox.setText("");
    valueTextBox.setText("");

    keyTextBox.setFocus(true);
  }

  @Override
  public String getItem() {
    String key = keyTextBox.getText().trim();
    String value = valueTextBox.getText().trim();

    return !key.isEmpty() && !value.isEmpty() ? keyTextBox.getText() + "=" + valueTextBox.getText() : "";
  }

  @Override
  public String renderItem(String item) {
    return item;
  }

  @Override
  public Widget asWidget() {
    return container;
  }

  @Override
  public void setEnterKeyHandler(EnterKeyHandler handler) {
    enterKeyHandler = handler;
  }

  private void addEnterKeyHandler() {
    keyTypeahead.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion suggestion) {
        String selection = suggestion.getReplacementString();
        MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) valueTypeahead.getSuggestOracle();
        oracle.clear();
        if (suggestions != null && suggestions.containsKey(selection) && !suggestions.get(selection).isEmpty()) {
          oracle.addAll(suggestions.get(selection));
        }
        valueTypeahead.reconfigure();
        return selection;
      }
    });
    valueTextBox.addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          if(enterKeyHandler != null) {
            enterKeyHandler.enterKeyPressed();
          }
        }
      }
    });
  }
}