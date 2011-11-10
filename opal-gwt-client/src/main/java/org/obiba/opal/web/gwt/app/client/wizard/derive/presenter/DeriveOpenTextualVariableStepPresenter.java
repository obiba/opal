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
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.OpenTextualVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.OpenTextualVariableDerivationHelper.Method;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapGrid;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

/**
 *
 */
public class DeriveOpenTextualVariableStepPresenter extends DerivationPresenter<DeriveOpenTextualVariableStepPresenter.Display> {

  private OpenTextualVariableDerivationHelper derivationHelper;

  @Inject
  public DeriveOpenTextualVariableStepPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  void initialize(VariableDto variable) {
    super.initialize(variable);
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public VariableDto getDerivedVariable() {
    return derivationHelper.getDerivedVariable();
  }

  @Override
  List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();

    stepCtrls.add(getDisplay().getMethodStepController().build());
    stepCtrls.add(getDisplay().getMapStepController().onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        if(derivationHelper == null || derivationHelper.getMethod() != getDisplay().getMethod()) {
          derivationHelper = new OpenTextualVariableDerivationHelper(originalVariable, getDisplay());
        }
      }
    }).build());
    return stepCtrls;
  }

  @Override
  protected void onBind() {
    getDisplay().getValueMapGrid().enableRowDeletion(true);
    getDisplay().getAddButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if(derivationHelper.addEntry(getDisplay().getValue(), getDisplay().getNewValue())) {
          getDisplay().populateValues(derivationHelper.getValueMapEntries());
          getDisplay().emptyValueFields();
        }
      }
    });
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

  public interface Display extends WidgetDisplay {

    DefaultWizardStepController.Builder getMethodStepController();

    DefaultWizardStepController.Builder getMapStepController();

    Method getMethod();

    void populateValues(List<ValueMapEntry> valueMapEntries);

    HasClickHandlers getAddButton();

    ValueMapGrid getValueMapGrid();

    String getValue();

    String getNewValue();

    void emptyValueFields();
  }
}
