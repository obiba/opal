/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos.ValueType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEvaluationPopupPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController.Builder;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.DerivedVariableGenerator;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ScriptSuggestBox;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.LinkDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

public class DeriveCustomVariablePresenter extends DerivationPresenter<DeriveCustomVariablePresenter.Display> {

  private final ScriptEvaluationPopupPresenter scriptEvaluationPopupPresenter;

  @Inject
  public DeriveCustomVariablePresenter(final EventBus eventBus, final Display view, ScriptEvaluationPopupPresenter scriptEvaluationPopupPresenter) {
    super(eventBus, view);
    this.scriptEvaluationPopupPresenter = scriptEvaluationPopupPresenter;
  }

  @Override
  void initialize(VariableDto variable) {
    super.initialize(variable);
    getView().getRepeatable().setValue(variable.getIsRepeatable());
    getView().getTestButton().addClickHandler(new TestButtonClickHandler());
    getView().getValueType().setValue(variable.getValueType());
    getView().getScriptBox().setValue("$('" + originalVariable.getName() + "')");
    getView().addSuggestions(variable.getParentLink());
  }

  @Override
  public void onClose() {
    scriptEvaluationPopupPresenter.getView().hide();
  }

  class TestButtonClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource(originalVariable.getParentLink().getLink()).get().withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto table) {
          VariableDto variable = getDerivedVariable();
          if(getView().getScriptBox().isTextSelected()) {
            variable.setValueType(ValueType.TEXT.getLabel());
            variable.setIsRepeatable(false);
            VariableDtos.setScript(variable, getView().getScriptBox().getSelectedScript());
          }
          scriptEvaluationPopupPresenter.initialize(table, variable);
        }
      }).send();
      getView().getScriptBox().focus();
    }
  }

  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = DerivedVariableGenerator.copyVariable(originalVariable, false);
    derived.setIsRepeatable(getView().getRepeatable().getValue());
    DerivedVariableGenerator.setScript(derived, getView().getScriptBox().getValue());
    derived.setValueType(getView().getValueType().getValue());
    return derived;
  }

  @Override
  List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();
    stepCtrls.add(getView().getDeriveStepController().build());
    return stepCtrls;
  }

  public interface Display extends View {

    Builder getDeriveStepController();

    HasClickHandlers getTestButton();

    void addSuggestions(LinkDto parentLink);

    void add(Widget widget);

    ScriptSuggestBox getScriptBox();

    HasValue<String> getValueType();

    HasValue<Boolean> getRepeatable();

  }
}
