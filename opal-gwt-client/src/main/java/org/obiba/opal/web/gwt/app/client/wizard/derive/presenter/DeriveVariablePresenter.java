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

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 *
 */
public class DeriveVariablePresenter extends WidgetPresenter<DeriveVariablePresenter.Display> implements Wizard {

  @Inject
  private DeriveCategoricalVariableStepPresenter categoricalPresenter;

  @Inject
  private DeriveNumericalVariableStepPresenter numericalPresenter;

  @Inject
  private SummaryTabPresenter summaryTabPresenter;

  private VariableDto variable;

  //
  // Constructors
  //

  @Inject
  public DeriveVariablePresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // Wizard Methods
  //

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 1) {
      throw new IllegalArgumentException("Variable DTO is expected as first wizard argument.");
    }

    if(!(event.getEventParameters()[0] instanceof VariableDto)) {
      throw new IllegalArgumentException("unexpected event parameter type (expected VariableDto)");
    }

    variable = (VariableDto) event.getEventParameters()[0];
    if(event.getWizardType() == WizardType.DERIVE_CATEGORIZE_VARIABLE) {
      // TODO
      String valueType = variable.getValueType();
      if(valueType.equals("integer") || valueType.equals("decimal")) {
        getDisplay().appendWizardSteps(numericalPresenter.getWizardSteps());
      } else if(valueType.equals("text")) {
        getDisplay().appendWizardSteps(categoricalPresenter.getWizardSteps());
      }
    } else {
      // TODO
    }
  }

  private void requestSummary(final VariableDto selection) {
    GWT.log("requestSummary: " + selection.getLink() + "/summary");
    summaryTabPresenter.setResourceUri(selection.getLink() + "/summary");
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  @Override
  protected void onBind() {
    // TODO bind only the relevant one
    categoricalPresenter.bind();
    numericalPresenter.bind();

    summaryTabPresenter.bind();
    getDisplay().setSummaryTabWidget(summaryTabPresenter.getDisplay());
    getDisplay().setSummaryStepInHandler(new SummaryStepInHandler());
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    // TODO unbind only the relevant one
    categoricalPresenter.unbind();
    numericalPresenter.unbind();

    summaryTabPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addFinishClickHandler(new FinishClickHandler()));
  }

  //
  // Inner classes and Interfaces
  //

  final class SummaryStepInHandler implements StepInHandler {
    @Override
    public void onStepIn() {
      requestSummary(variable);
      summaryTabPresenter.refreshDisplay();
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
    }
  }

  class FinishClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
    }
  }

  public interface Display extends WidgetDisplay {

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addFinishClickHandler(ClickHandler handler);

    void showDialog();

    void hideDialog();

    void clear();

    void setSummaryTabWidget(WidgetDisplay widget);

    void setSummaryStepInHandler(StepInHandler handler);

    void appendWizardSteps(List<DefaultWizardStepController> stepCtrls);
  }

}
