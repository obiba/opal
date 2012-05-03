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

import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.BooleanVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class DeriveBooleanVariableStepPresenter extends DerivationPresenter<DeriveBooleanVariableStepPresenter.Display> {

  private BooleanVariableDerivationHelper derivationHelper;

  @Inject
  public DeriveBooleanVariableStepPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  //
  // DerivationPresenter methods
  //

  @Override
  void initialize(VariableDto variable, VariableDto derivedVariable) {
    super.initialize(variable, derivedVariable);
    derivationHelper = new BooleanVariableDerivationHelper(variable, derivedVariable);
    getView().populateValues(derivationHelper.getValueMapEntries());
  }

  @Override
  public void generateDerivedVariable() {
    setDerivedVariable(derivationHelper.getDerivedVariable());
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<DefaultWizardStepController.Builder>();
    stepBuilders.add(getView().getMapStepController() //
        .onStepIn(stepInHandler) //
        .onValidate(new ValidationHandler() {
          @Override
          public boolean validate() {
            // TODO
            return true;
          }
        }));
    return stepBuilders;
  }

  //
  // Interfaces
  //

  public interface Display extends View {

    DefaultWizardStepController.Builder getMapStepController();

    void populateValues(List<ValueMapEntry> valuesMap);

  }

}
