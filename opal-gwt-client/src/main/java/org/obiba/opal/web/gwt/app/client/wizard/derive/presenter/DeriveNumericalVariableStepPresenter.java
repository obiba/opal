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

import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.NumericalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 *
 */
public class DeriveNumericalVariableStepPresenter extends DerivationPresenter<DeriveNumericalVariableStepPresenter.Display> {

  private NumericalVariableDerivationHelper derivationHelper;

  @Inject
  public DeriveNumericalVariableStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);

    super.registerHandler(getDisplay().addValueMapEntryHandler(new AddValueMapEntryHandler()));
  }

  @Override
  void initialize(VariableDto variable) {
    super.initialize(variable);
    getDisplay().setNumberType(variable.getValueType());
  }

  @Override
  public List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();

    stepCtrls.add(getDisplay().getMethodStepController().build());
    stepCtrls.add(getDisplay().getMapStepController().onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        derivationHelper = new NumericalVariableDerivationHelper(originalVariable);
        getDisplay().populateValues(derivationHelper.getValueMapEntries());
      }
    }).build());

    return stepCtrls;
  }

  @Override
  public VariableDto getDerivedVariable() {
    return derivationHelper.getDerivedVariable();
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

  /**
   *
   */
  private final class AddValueMapEntryHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      boolean added = false;
      if(getDisplay().addRangeSelected()) {
        added = derivationHelper.addValueMapEntry(getDisplay().getLowerValue(), getDisplay().getUpperValue(), getDisplay().getNewValue());
      } else {
        added = derivationHelper.addValueMapEntry(getDisplay().getDiscreteValue(), getDisplay().getNewValue());
      }
      if(added) {
        getDisplay().refreshValuesMapDisplay();
      }
    }
  }

  public interface Display extends WidgetDisplay {

    DefaultWizardStepController.Builder getMethodStepController();

    DefaultWizardStepController.Builder getMapStepController();

    void populateValues(List<ValueMapEntry> valuesMap);

    void refreshValuesMapDisplay();

    HandlerRegistration addValueMapEntryHandler(ClickHandler handler);

    String getNewValue();

    boolean addRangeSelected();

    Number getDiscreteValue();

    Number getLowerValue();

    Number getUpperValue();

    void setNumberType(String valueType);

  }

}
