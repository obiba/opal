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
import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.TemporalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class DeriveTemporalVariableStepPresenter extends DerivationPresenter<DeriveTemporalVariableStepPresenter.Display> {

  private TemporalVariableDerivationHelper derivationHelper;

  @Inject
  public DeriveTemporalVariableStepPresenter(final EventBus eventBus, final Display view) {
    super(eventBus, view);
  }

  @Override
  void initialize(VariableDto variable) {
    super.initialize(variable);
    getView().setTimeType(variable.getValueType());
  }

  @Override
  public List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();

    stepCtrls.add(getView().getMethodStepController().onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        if(getView().getFromDate() != null && getView().getToDate() != null) return true;
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("DatesRangeInvalid").build());
        return false;
      }
    }).build());
    stepCtrls.add(getView().getMapStepController().onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        // do not re-populate if group method selection has not changed
        if(derivationHelper == null //
            || !derivationHelper.getGroupMethod().toString().equalsIgnoreCase(getView().getGroupMethod()) //
            || !derivationHelper.getFromDate().equals(getView().getFromDate()) //
            || !derivationHelper.getToDate().equals(getView().getToDate())) {
          derivationHelper = new TemporalVariableDerivationHelper(originalVariable, getView().getGroupMethod(), getView().getFromDate(), getView().getToDate());
          getView().populateValues(derivationHelper.getValueMapEntries());
        }
      }
    }).build());

    return stepCtrls;
  }

  @Override
  public VariableDto getDerivedVariable() {
    return derivationHelper.getDerivedVariable();
  }

  //
  // Interfaces
  //

  public interface Display extends View {

    DefaultWizardStepController.Builder getMethodStepController();

    void setTimeType(String valueType);

    DefaultWizardStepController.Builder getMapStepController();

    String getGroupMethod();

    void populateValues(List<ValueMapEntry> valuesMap);

    Date getFromDate();

    Date getToDate();

  }

}
