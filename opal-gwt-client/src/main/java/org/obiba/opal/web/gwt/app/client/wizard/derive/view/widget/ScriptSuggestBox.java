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

  private SuggestBox variable;

  private MultiWordSuggestOracle suggest;

  private InnerAutoCompleteTextArea inner;

  public ScriptSuggestBox() {
    suggest = new MultiWordSuggestOracle();
    SuggestBox.DefaultSuggestionDisplay display = new SuggestBox.DefaultSuggestionDisplay();
    inner = new InnerAutoCompleteTextArea();

    display.setPositionRelativeTo(new UIObject() {
      @Override
      public int getAbsoluteLeft() {
        return inner.getAbsoluteLeft() + inner.getOffsetWidth();
      }

      @Override
      public int getOffsetHeight() {
        return 1;
      }

      @Override
      public int getAbsoluteTop() {
        return inner.getAbsoluteTop();
      }

      @Override
      public int getOffsetWidth() {
        return 1;
      }
    });

    variable = new SuggestBox(suggest, inner, display);
    initWidget(variable);
  }

  public void pushAsyncSuggestions(LinkDto link) {
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(link.getLink() + "/variables").get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {

      @Override
      public void onResource(Response response, JsArray<VariableDto> resource) {
        for(int i = 0; i < resource.length(); i++) {
          String suggestion = "$('" + resource.get(i).getName() + "')";
          suggest.add(suggestion);
          inner.addSuggestion(suggestion);
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
      inner.setCursorPos(0);
      return inner.getValue();
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
