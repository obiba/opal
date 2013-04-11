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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEditorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEvaluationPopupPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.DerivedVariableGenerator;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.util.VariableDtos.ValueType;

public class DeriveCustomVariablePresenter extends DerivationPresenter<DeriveCustomVariablePresenter.Display> {

  private final ScriptEvaluationPopupPresenter scriptEvaluationPopupPresenter;

  private final ScriptEditorPresenter scriptEditorPresenter;

  @Inject
  public DeriveCustomVariablePresenter(EventBus eventBus, Display view,
      ScriptEvaluationPopupPresenter scriptEvaluationPopupPresenter, ScriptEditorPresenter scriptEditorPresenter) {
    super(eventBus, view);
    this.scriptEvaluationPopupPresenter = scriptEvaluationPopupPresenter;
    this.scriptEditorPresenter = scriptEditorPresenter;
    this.scriptEditorPresenter.setVariableDtoFactory(new DeriveCustomVariableDtoFactory());
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Editor, scriptEditorPresenter);
  }

  @Override
  void initialize(TableDto originalTable, @Nullable TableDto destinationTable, VariableDto originalVariable,
      @Nullable VariableDto derivedVariable) {
    super.initialize(originalTable, destinationTable, originalVariable, derivedVariable);
    getView().getRepeatable().setValue(originalVariable.getIsRepeatable());
    getView().getValueType().setValue(originalVariable.getValueType());
    String name = originalVariable.getName();
    if(originalTable.hasViewLink()) {
      String datasourceName = originalTable.getDatasourceName();
      String tableName = originalTable.getName();
      scriptEditorPresenter.setScript("$('" + datasourceName + "." + tableName + ":" + name + "')");
    } else {
      scriptEditorPresenter.setScript("$('" + name + "')");
    }
    scriptEditorPresenter.setTable(originalTable);
  }

  @Override
  public void onClose() {
    scriptEvaluationPopupPresenter.getView().hide();
  }

  public class DeriveCustomVariableDtoFactory implements ScriptEditorPresenter.VariableDtoFactory {

    @Override
    public VariableDto create() {
      generateDerivedVariable();
      VariableDto variable = getDerivedVariable();
      String selectedScript = scriptEditorPresenter.getSelectedScript();
      if(!Strings.isNullOrEmpty(selectedScript)) {
        variable.setValueType(ValueType.TEXT.getLabel());
        variable.setIsRepeatable(false);
        VariableDtos.setScript(variable, selectedScript);
      }
      return variable;
    }
  }

  @Override
  public void generateDerivedVariable() {
    VariableDto derived = DerivedVariableGenerator.copyVariable(getOriginalVariable(), false);
    derived.setIsRepeatable(getView().getRepeatable().getValue());
    VariableDtos.setScript(derived, scriptEditorPresenter.getScript());
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

    enum Slots {
      Editor
    }

    BranchingWizardStepController.Builder getDeriveStepController();

    HasValue<String> getValueType();

    HasValue<Boolean> getRepeatable();

  }
}
