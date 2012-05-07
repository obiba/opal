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
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.DerivedVariableGenerator;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ScriptSuggestBox;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
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
  public DeriveCustomVariablePresenter(EventBus eventBus, Display view,
      ScriptEvaluationPopupPresenter scriptEvaluationPopupPresenter) {
    super(eventBus, view);
    this.scriptEvaluationPopupPresenter = scriptEvaluationPopupPresenter;
  }

  @Override
  void initialize(TableDto originalTable, TableDto destinationTable, VariableDto originalVariable,
      VariableDto derivedVariable) {
    super.initialize(originalTable, destinationTable, originalVariable, derivedVariable);
    getView().getRepeatable().setValue(originalVariable.getIsRepeatable());
    getView().getTestButton().addClickHandler(new TestButtonClickHandler());
    getView().getValueType().setValue(originalVariable.getValueType());

    String name = getOriginalVariable().getName();
    if(originalTable.hasViewLink()) {
      String datasourceName = originalTable.getDatasourceName();
      String tableName = originalTable.getName();
      getView().getScriptBox().setValue("$('" + datasourceName + "." + tableName + ":" + name + "')");
    } else {
      getView().getScriptBox().setValue("$('" + name + "')");
    }
    getView().addSuggestions(originalTable);
  }

  @Override
  public void onClose() {
    scriptEvaluationPopupPresenter.getView().hide();
  }

  class TestButtonClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(getOriginalVariable().getParentLink().getLink())
          .get().withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto table) {
          generateDerivedVariable();
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
  public void generateDerivedVariable() {
    VariableDto derived = DerivedVariableGenerator.copyVariable(getOriginalVariable(), false);
    derived.setIsRepeatable(getView().getRepeatable().getValue());
    VariableDtos.setScript(derived, getView().getScriptBox().getValue());

    derived.setValueType(getView().getValueType().getValue());
    setDerivedVariable(derived);
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<DefaultWizardStepController.Builder>();
    stepBuilders.add(getView().getDeriveStepController());
    return stepBuilders;
  }

  public interface Display extends View {

    BranchingWizardStepController.Builder getDeriveStepController();

    HasClickHandlers getTestButton();

    void addSuggestions(TableDto table);

    void add(Widget widget);

    ScriptSuggestBox getScriptBox();

    HasValue<String> getValueType();

    HasValue<Boolean> getRepeatable();

  }
}
