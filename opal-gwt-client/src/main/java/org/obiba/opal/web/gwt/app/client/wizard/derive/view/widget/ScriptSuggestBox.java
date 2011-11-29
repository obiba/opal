/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.LinkDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.UIObject;

public class ScriptSuggestBox extends Composite implements HasValue<String> {

  SuggestBox variable;

  MultiWordSuggestOracle suggest;

  private InnerAutoCompleteTextArea innerAutoCompleteTextArea;

  public ScriptSuggestBox() {
    suggest = new MultiWordSuggestOracle();
    SuggestBox.DefaultSuggestionDisplay display = new SuggestBox.DefaultSuggestionDisplay();
    innerAutoCompleteTextArea = new InnerAutoCompleteTextArea();

    display.setPositionRelativeTo(new UIObject() {
      @Override
      public int getAbsoluteLeft() {
        return innerAutoCompleteTextArea.getAbsoluteLeft();
      }

      @Override
      public int getOffsetHeight() {
        return innerAutoCompleteTextArea.getOffsetHeight();
      }

      @Override
      public int getAbsoluteTop() {
        return innerAutoCompleteTextArea.getAbsoluteTop();
      }

      @Override
      public int getOffsetWidth() {
        return innerAutoCompleteTextArea.getOffsetWidth();
      }
    });

    variable = new SuggestBox(suggest, innerAutoCompleteTextArea, display);
    initWidget(variable);
  }

  public void pushAsyncSuggestions(LinkDto link) {
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(link.getLink() + "/variables").get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {

      @Override
      public void onResource(Response response, JsArray<VariableDto> resource) {
        for(int i = 0; i < resource.length(); i++) {
          String suggestion = "$('" + resource.get(i).getName() + "')";
          suggest.add(suggestion);
          innerAutoCompleteTextArea.addSuggestion(suggestion);
        }

      }
    }).send();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return variable.addValueChangeHandler(handler);
  }

  @Override
  public String getValue() {
    String value = variable.getValue();

    // due to getText() and setText() of innerAutoCompleteTextArea are overridden,
    // behaviour of TextArea is affected.
    if(Strings.isNullOrEmpty(value)) {
      innerAutoCompleteTextArea.setCursorPos(0);
      return innerAutoCompleteTextArea.getValue();
    }
    return value;
  }

  @Override
  public void setValue(String value) {
    variable.setValue(value);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    variable.setValue(value, fireEvents);
  }

}
