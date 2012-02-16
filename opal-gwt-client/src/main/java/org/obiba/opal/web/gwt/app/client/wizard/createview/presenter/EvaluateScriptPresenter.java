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

import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos.ValueType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEvaluationPopupPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class EvaluateScriptPresenter extends PresenterWidget<EvaluateScriptPresenter.Display> {

  private TableDto table;

  private ScriptEvaluationPopupPresenter scriptEvaluationPopupPresenter;

  @Inject
  public EvaluateScriptPresenter(EventBus eventBus, Display view, ScriptEvaluationPopupPresenter scriptEvaluationPopupPresenter) {
    super(eventBus, view);
    this.scriptEvaluationPopupPresenter = scriptEvaluationPopupPresenter;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  private void addEventHandlers() {
    super.registerHandler(getView().addTestScriptClickHandler(new TestButtonClickHandler()));
  }

  public void setReadyOnly(boolean readyOnly) {
    getView().setReadOnly(readyOnly);
  }

  public void setTable(ViewDto viewDto) {
    ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource("/datasource/" + viewDto.getDatasourceName() + "/table/" + viewDto.getName()).get().withCallback(new ResourceCallback<TableDto>() {
      @Override
      public void onResource(Response response, TableDto resource) {
        setTable(resource);
      }

    }).send();
  }

  public void setTable(TableDto table) {
    this.table = table;
  }

  public void setScript(String script) {
    getView().setScript(script);
  }

  public String getScript() {
    String script = getView().getScript();
    return script.trim().equals("") ? "null" : script;
  }

  public void showTest(boolean b) {
    getView().showTest(b);
  }

  class TestButtonClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {

      String selectedScript = getView().getSelectedScript();
      VariableDto derived = VariableDto.create();
      derived.setValueType(ValueType.TEXT.getLabel());
      derived.setIsRepeatable(false);
      if(!Strings.isNullOrEmpty(selectedScript)) {
        VariableDtos.setScript(derived, selectedScript);
      } else {
        VariableDtos.setScript(derived, getScript());
      }
      scriptEvaluationPopupPresenter.initialize(table, derived);
    }
  }

  public interface Display extends View {

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
