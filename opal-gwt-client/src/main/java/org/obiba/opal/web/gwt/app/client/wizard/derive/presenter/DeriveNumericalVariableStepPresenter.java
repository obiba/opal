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
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;

import com.google.inject.Inject;

/**
 *
 */
public class DeriveNumericalVariableStepPresenter extends WidgetPresenter<DeriveNumericalVariableStepPresenter.Display> implements DeriveVariableStepPresenter {

  @Inject
  public DeriveNumericalVariableStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();

    stepCtrls.add(getDisplay().getMethodStepController().build());
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

    DefaultWizardStepController.Builder getMethodStepController();

    DefaultWizardStepController.Builder getMapStepController();

  }

}
