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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.CategoricalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.inject.Inject;

/**
 *
 */
public class DeriveCategoricalVariableStepPresenter extends DerivationPresenter<DeriveCategoricalVariableStepPresenter.Display> {

  private CategoricalVariableDerivationHelper derivationHelper;

  @Inject
  public DeriveCategoricalVariableStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // DerivationPresenter methods
  //

  @Override
  void initialize(VariableDto variable) {
    super.initialize(variable);
    derivationHelper = new CategoricalVariableDerivationHelper(variable);
    getDisplay().populateValues(derivationHelper.getValueMapEntries());
  }

  @Override
  public VariableDto getDerivedVariable() {
    return derivationHelper.getDerivedVariable();
  }

  @Override
  public List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();

    stepCtrls.add(getDisplay().getMapStepController().onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        // TODO
        return true;
      }
    }).build());

    return stepCtrls;
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
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

  //
  // Interfaces
  //

  public interface Display extends WidgetDisplay {

    DefaultWizardStepController.Builder getMapStepController();

    void populateValues(List<ValueMapEntry> valuesMap);

  }

}
