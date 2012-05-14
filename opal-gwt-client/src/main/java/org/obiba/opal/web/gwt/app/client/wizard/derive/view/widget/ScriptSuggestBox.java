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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.UIObject;

public class ScriptSuggestBox extends Composite {

  private final SuggestBox variable;

  private final MultiWordSuggestOracle suggest;

  private final InnerAutoCompleteTextArea inner;

  public ScriptSuggestBox() {
    suggest = new MultiWordSuggestOracle();
    SuggestBox.DefaultSuggestionDisplay display = new SuggestBox.DefaultSuggestionDisplay();
    inner = new InnerAutoCompleteTextArea();
    display.setPositionRelativeTo(new CustomPositionnedUIObject());
    variable = new SuggestBox(suggest, inner, display);
    initWidget(variable);
    addStyleName("code");
  }

  public void addAsyncSuggestions(TableDto table) {
    if(table.hasViewLink() == false) {
      requestVariables(table.getLink());
    } else {
      ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource(table.getViewLink()).get().withCallback(new ResourceCallback<ViewDto>() {

        @Override
        public void onResource(Response response, ViewDto view) {
          for(String table : JsArrays.toIterable(view.getFromArray())) {
            String[] tableNameParts = table.split("\\.");
            requestVariables("/datasource/" + tableNameParts[0] + "/table/" + tableNameParts[1]);
          }
        }
      }).send();
    }
  }

  private void requestVariables(String link) {
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(link + "/variables").get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {

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

  public void focus() {
    inner.setFocus(true);
  }

  public String getSelectedScript() {
    return inner.getSelectedText();
  }

  public boolean isTextSelected() {
    return inner.isTextSelected();
  }

  public void setValue(String value) {

    variable.setValue(value);
    inner.initializeText(value);
  }

  public String getValue() {
    return inner.getRealText();
  }

  class CustomPositionnedUIObject extends UIObject {
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
  }

  public void setEnabled(boolean enabled) {
    inner.setEnabled(enabled);
  }

  public void setReadOnly(boolean readOnly) {
    inner.setReadOnly(readOnly);
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return inner.addChangeHandler(handler);
  }

}
