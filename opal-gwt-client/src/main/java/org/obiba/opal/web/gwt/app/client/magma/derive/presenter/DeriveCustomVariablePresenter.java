/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive.presenter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.magma.derive.helper.DerivedVariableGenerator;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ScriptEditorPresenter;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.ui.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.support.VariableDtos.ValueType;

public class DeriveCustomVariablePresenter extends DerivationPresenter<DeriveCustomVariablePresenter.Display> {

  private final ScriptEditorPresenter scriptEditorPresenter;

  @Inject
  public DeriveCustomVariablePresenter(EventBus eventBus, Display view,
      ScriptEditorPresenter scriptEditorPresenter) {
    super(eventBus, view);
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
    scriptEditorPresenter.setValueEntityType(originalVariable.getValueType());
    scriptEditorPresenter.setRepeatable(originalVariable.getIsRepeatable());
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

  public class DeriveCustomVariableDtoFactory implements ScriptEditorPresenter.VariableDtoFactory {

    @Override
    public VariableDto create() {
      generateDerivedVariable();
      VariableDto variable = getDerivedVariable();
      String selectedScript = scriptEditorPresenter.getSelectedScript();
      if(!Strings.isNullOrEmpty(selectedScript)) {
        variable.setValueType(ValueType.TEXT.getLabel());
        variable.setIsRepeatable(false);
        variable.setOccurrenceGroup("");
        VariableDtos.setScript(variable, selectedScript);
      }
      return variable;
    }
  }

  @Override
  public void generateDerivedVariable() {
    VariableDto derived = DerivedVariableGenerator
        .copyVariable(getOriginalVariable(), false, getOriginalVariable().getLink());
    derived.setIsRepeatable(scriptEditorPresenter.isRepeatable());
    derived.setValueType(scriptEditorPresenter.getValueEntityType().getLabel());
    VariableDtos.setScript(derived, scriptEditorPresenter.getScript());
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
  }
}
