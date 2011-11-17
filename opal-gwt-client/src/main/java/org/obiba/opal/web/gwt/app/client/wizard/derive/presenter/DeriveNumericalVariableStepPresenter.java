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

import org.obiba.opal.web.gwt.app.client.widgets.event.SummaryReceivedEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.NumericalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 *
 */
public class DeriveNumericalVariableStepPresenter extends DerivationPresenter<DeriveNumericalVariableStepPresenter.Display> {

  @Inject
  private SummaryTabPresenter summaryTabPresenter;

  private NumericalVariableDerivationHelper derivationHelper;

  @Inject
  public DeriveNumericalVariableStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);

    super.registerHandler(eventBus.addHandler(SummaryReceivedEvent.getType(), new OriginalVariableSummaryReceivedHandler()));
    super.registerHandler(getDisplay().addValueMapEntryHandler(new AddValueMapEntryHandler()));
  }

  @Override
  void initialize(VariableDto variable) {
    super.initialize(variable);
    getDisplay().setNumberType(variable.getValueType());
    summaryTabPresenter.setResourceUri(variable.getLink() + "/summary");
    summaryTabPresenter.forgetSummary();
    summaryTabPresenter.refreshDisplay();
  }

  @Override
  public List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();

    stepCtrls.add(getDisplay().getMethodStepController().build());
    stepCtrls.add(getDisplay().getMapStepController().onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        derivationHelper = new NumericalVariableDerivationHelper(originalVariable);
        if(getDisplay().rangeSelected()) {
          if(getDisplay().byRangeLengthSelected()) {
            addRangesByLength();
          } else {
            addRangesByCount();
          }
        }
        getDisplay().populateValues(derivationHelper.getValueMapEntries());
      }
    }).build());

    return stepCtrls;
  }

  private void addRangesByCount() {
    double lowerLimit = getDisplay().getLowerLimit().doubleValue();
    double upperLimit = getDisplay().getUpperLimit().doubleValue();
    long count = getDisplay().getRangeCount().longValue();
    long length = ((long) (upperLimit - lowerLimit)) / count;
    addRangesByLength(length);
  }

  private void addRangesByLength() {
    addRangesByLength(getDisplay().getRangeLength().longValue());
  }

  private void addRangesByLength(long length) {
    double lowerLimit = getDisplay().getLowerLimit().doubleValue();
    double upperLimit = getDisplay().getUpperLimit().doubleValue();

    int newValue = 1;

    double lower = lowerLimit;
    double upper = lower + length;

    addValueMapEntry(null, lower, "");
    while(upper <= upperLimit) {
      addValueMapEntry(lower, upper, String.valueOf(newValue++));
      lower = upper;
      upper += length;
    }
    addValueMapEntry(lower, null, "");
  }

  private void addValueMapEntry(Double lower, Double upper, String newValue) {
    if(originalVariable.getValueType().equals("integer")) {
      derivationHelper.addValueMapEntry(lower == null ? null : lower.longValue(), upper == null ? null : upper.longValue(), newValue);
    } else {
      derivationHelper.addValueMapEntry(lower, upper, newValue);
    }
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
    summaryTabPresenter.bind();
    getDisplay().setSummaryTabWidget(summaryTabPresenter.getDisplay());
  }

  @Override
  protected void onUnbind() {
    summaryTabPresenter.unbind();
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
  private final class OriginalVariableSummaryReceivedHandler implements SummaryReceivedEvent.Handler {
    @Override
    public void onSummaryReceived(SummaryReceivedEvent event) {
      if(event.getResourceUri().equals(originalVariable.getLink() + "/summary")) {
        SummaryStatisticsDto dto = event.getSummary();
        if(dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous) != null) {
          ContinuousSummaryDto continuous = dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous).cast();
          GWT.log("min=" + continuous.getSummary().getMin());
          GWT.log("max=" + continuous.getSummary().getMax());
          GWT.log("mean=" + continuous.getSummary().getMean());
          GWT.log("median=" + continuous.getSummary().getMedian());
          GWT.log("stddev" + continuous.getSummary().getStdDev());

          double from = new Double(continuous.getSummary().getMin());
          double to = new Double(continuous.getSummary().getMax());

          getDisplay().setValueLimits(new Long((long) from), new Long((long) to + 1));
        }
      }
    }
  }

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

    boolean rangeSelected();

    void setValueLimits(Number from, Number to);

    Number getLowerLimit();

    Number getUpperLimit();

    Long getRangeLength();

    Long getRangeCount();

    boolean byRangeLengthSelected();

    Number getDiscreteValue();

    Number getLowerValue();

    Number getUpperValue();

    String getNewValue();

    boolean addRangeSelected();

    void setNumberType(String valueType);

    void setSummaryTabWidget(WidgetDisplay widget);

  }

}
