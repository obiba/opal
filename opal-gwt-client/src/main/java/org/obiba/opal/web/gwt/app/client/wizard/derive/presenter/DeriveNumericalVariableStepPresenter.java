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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
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

  private static Translations translations = GWT.create(Translations.class);

  @Inject
  private SummaryTabPresenter summaryTabPresenter;

  private NumericalVariableDerivationHelper<? extends Number> derivationHelper;

  private NumberType numberType;

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

    stepCtrls.add(getDisplay().getMethodStepController().onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        List<String> errorMessages = new ArrayList<String>();
        if(getDisplay().rangeSelected()) {
          validateRangeForm(errorMessages);
        }
        if(!errorMessages.isEmpty()) {
          eventBus.fireEvent(NotificationEvent.newBuilder().error(errorMessages).build());
        }
        return errorMessages.isEmpty();
      }

      private void validateRangeForm(List<String> errorMessages) {
        validateRangeLimitsForm(errorMessages);
        validateRangeDefinitionForm(errorMessages);
      }

      private void validateRangeLimitsForm(List<String> errorMessages) {
        getDisplay().setLowerLimitError(false);
        getDisplay().setUpperLimitError(false);

        if(getDisplay().getLowerLimit() == null) {
          errorMessages.add(translations.lowerValueLimitRequired());
          getDisplay().setLowerLimitError(true);
        }
        if(getDisplay().getUpperLimit() == null) {
          errorMessages.add(translations.upperValueLimitRequired());
          getDisplay().setUpperLimitError(true);
        }
      }

      private void validateRangeDefinitionForm(List<String> errorMessages) {
        getDisplay().setRangeLengthError(false);
        getDisplay().setRangeCountError(false);

        if(getDisplay().rangeLengthSelected() && getDisplay().getRangeLength() == null) {
          errorMessages.add(translations.rangesLengthRequired());
          getDisplay().setRangeLengthError(true);
        } else if(!getDisplay().rangeLengthSelected() && getDisplay().getRangeCount() == null) {
          errorMessages.add(translations.rangesCountRequired());
          getDisplay().setRangeCountError(true);
        }
      }

    }).build());
    stepCtrls.add(getDisplay().getMapStepController().onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        newDerivationHelper();
        if(getDisplay().rangeSelected()) {
          // ranges
          if(getDisplay().rangeLengthSelected()) {
            addRangesByLengthMapping();
          } else {
            addRangesByCountMapping();
          }
        } else {
          // TODO query distinct values
        }

        getDisplay().populateValues(derivationHelper.getValueMapEntries());
      }
    }).build());

    return stepCtrls;
  }

  private void addRangesByCountMapping() {
    double lowerLimit = getDisplay().getLowerLimit().doubleValue();
    double upperLimit = getDisplay().getUpperLimit().doubleValue();
    long count = getDisplay().getRangeCount().longValue();
    long length = ((long) (upperLimit - lowerLimit)) / count;
    addRangesByLength(length);
  }

  private void addRangesByLengthMapping() {
    addRangesByLength(getDisplay().getRangeLength().longValue());
  }

  private void addRangesByLength(long length) {
    double lowerLimit = getDisplay().getLowerLimit().doubleValue();
    double upperLimit = getDisplay().getUpperLimit().doubleValue();

    int newValue = 1;

    double lower = lowerLimit;
    double upper = lower + length;

    addValueMapEntry(null, lower, String.valueOf(newValue++));
    if(length >= 0) {
      while(upper <= upperLimit) {
        addValueMapEntry(lower, upper, String.valueOf(newValue++));
        lower = upper;
        upper += length;
      }
    }
    addValueMapEntry(lower, null, String.valueOf(newValue++));
  }

  private void newDerivationHelper() {
    numberType = NumberType.valueOf(originalVariable.getValueType().toUpperCase());
    derivationHelper = numberType.newDerivationHelper(originalVariable);
  }

  private boolean addValueMapEntry(String value, String newValue) {
    if(derivationHelper.hasValueMapEntryWithValue(value)) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(translations.valueMapAlreadyAdded()).build());
      return false;
    }
    numberType.addValueMapEntry(derivationHelper, value, newValue);
    return true;
  }

  private boolean addValueMapEntry(Number lower, Number upper, String newValue) {
    if(!numberType.addValueMapEntry(derivationHelper, lower, upper, newValue)) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(translations.rangeOverlap()).build());
      return false;
    }
    return true;
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

  private enum NumberType {
    INTEGER() {

      @Override
      public NumericalVariableDerivationHelper<?> newDerivationHelper(VariableDto originalVariable) {
        return new NumericalVariableDerivationHelper<Long>(originalVariable);
      }

      @SuppressWarnings("unchecked")
      @Override
      public void addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, String value, String newValue) {
        ((NumericalVariableDerivationHelper<Long>) helper).addValueMapEntry(new Long(value), newValue);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, Number lower, Number upper, String newValue) {
        NumericalVariableDerivationHelper<Long> h = (NumericalVariableDerivationHelper<Long>) helper;
        Long l = lower == null ? null : lower.longValue();
        Long u = upper == null ? null : upper.longValue();
        if(h.isRangeOverlap(l, u)) {
          return false;
        }
        h.addValueMapEntry(l, u, newValue);
        return true;
      }

    },
    DECIMAL() {
      @Override
      public String formatNumber(Number nb) {
        if(nb == null) return null;
        String str = nb.toString();
        return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
      }

      @Override
      public NumericalVariableDerivationHelper<? extends Number> newDerivationHelper(VariableDto originalVariable) {
        return new NumericalVariableDerivationHelper<Double>(originalVariable);
      }

      @SuppressWarnings("unchecked")
      @Override
      public void addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, String value, String newValue) {
        ((NumericalVariableDerivationHelper<Double>) helper).addValueMapEntry(new Double(value), newValue);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, Number lower, Number upper, String newValue) {
        NumericalVariableDerivationHelper<Double> h = (NumericalVariableDerivationHelper<Double>) helper;
        Double l = lower == null ? null : lower.doubleValue();
        Double u = upper == null ? null : upper.doubleValue();
        if(h.isRangeOverlap(l, u)) {
          return false;
        }
        h.addValueMapEntry(l, u, newValue);
        return true;
      }
    };

    public String formatNumber(Number nb) {
      return nb == null ? null : nb.toString();
    }

    public abstract NumericalVariableDerivationHelper<? extends Number> newDerivationHelper(VariableDto originalVariable);

    public abstract void addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, String value, String newValue);

    public abstract boolean addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, Number lower, Number upper, String newValue);
  }

  /**
   *
   */
  private final class OriginalVariableSummaryReceivedHandler implements SummaryReceivedEvent.Handler {
    @Override
    public void onSummaryReceived(SummaryReceivedEvent event) {
      if(originalVariable != null && event.getResourceUri().equals(originalVariable.getLink() + "/summary")) {
        SummaryStatisticsDto dto = event.getSummary();
        if(dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous) != null) {
          ContinuousSummaryDto continuous = dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous).cast();
          double from = continuous.getSummary().getMin();
          double to = continuous.getSummary().getMax();
          getDisplay().setValueLimits(Long.valueOf((long) from), Long.valueOf((long) to + 1));
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
        added = addValueMapEntry(getDisplay().getLowerValue(), getDisplay().getUpperValue(), getDisplay().getNewValue());
      } else {
        added = addValueMapEntry(numberType.formatNumber(getDisplay().getDiscreteValue()), getDisplay().getNewValue());
      }
      if(added) {
        getDisplay().refreshValuesMapDisplay();
      }
    }
  }

  public interface Display extends WidgetDisplay {

    DefaultWizardStepController.Builder getMethodStepController();

    void setRangeCountError(boolean error);

    void setRangeLengthError(boolean error);

    void setUpperLimitError(boolean error);

    void setLowerLimitError(boolean error);

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

    boolean rangeLengthSelected();

    Number getDiscreteValue();

    Number getLowerValue();

    Number getUpperValue();

    String getNewValue();

    boolean addRangeSelected();

    void setNumberType(String valueType);

    void setSummaryTabWidget(WidgetDisplay widget);

  }

}
