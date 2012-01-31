/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables.ValueType;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class EvaluateScriptPresenter extends WidgetPresenter<EvaluateScriptPresenter.Display> {

  private TableDto table;

  @Inject
  public EvaluateScriptPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addTestScriptClickHandler(new TestButtonClickHandler()));
  }

  public void setReadyOnly(boolean readyOnly) {
    getDisplay().setReadOnly(readyOnly);
  }

  public void setTable(TableDto table) {
    this.table = table;
  }

  public void setScript(String script) {
    getDisplay().setScript(script);
  }

  public String getScript() {
    String script = getDisplay().getScript();
    return script.trim().equals("") ? "null" : script;
  }

  public void showTest(boolean b) {
    getDisplay().showTest(b);
  }

  class TestButtonClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {

      String selectedScript = getDisplay().getSelectedScript();
      VariableDto derived = VariableDto.create();
      derived.setValueType(ValueType.TEXT.getLabel());
      derived.setIsRepeatable(false);
      if(!Strings.isNullOrEmpty(selectedScript)) {
        Variables.setScript(derived, selectedScript);
      } else {
        Variables.setScript(derived, getScript());
      }
      // eventBus.fireEvent(new ScriptEvaluationPopupEvent(derived, table));

    }
  }

  public interface Display extends WidgetDisplay {

    String getScript();

    void showTest(boolean b);

    void setScript(String script);

    String getSelectedScript();

    HandlerRegistration addTestScriptClickHandler(ClickHandler handler);

    void setReadOnly(boolean readOnly);

    void formEnable(boolean enabled);

    void formClear();

    HandlerRegistration addScriptChangeHandler(ChangeHandler handler);
  }

}
