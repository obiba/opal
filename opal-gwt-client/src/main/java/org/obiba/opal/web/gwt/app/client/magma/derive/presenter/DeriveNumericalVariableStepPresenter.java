/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.derive.presenter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.DerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.NumericalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryReceivedEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class DeriveNumericalVariableStepPresenter
    extends DerivationPresenter<DeriveNumericalVariableStepPresenter.Display> implements DerivationUiHandlers {

  private final SummaryTabPresenter summaryTabPresenter;

  private NumericalVariableDerivationHelper<? extends Number> derivationHelper;

  private NumberType numberType;

  private int scale = 2;

  @Inject
  public DeriveNumericalVariableStepPresenter(EventBus eventBus, Display view,
      SummaryTabPresenter summaryTabPresenter) {
    super(eventBus, view);
    this.summaryTabPresenter = summaryTabPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  void initialize(TableDto originalTable, TableDto destinationTable, VariableDto originalVariable,
      VariableDto derivedVariable) {
    super.initialize(originalTable, destinationTable, originalVariable, derivedVariable);
    getView().setNumberType(originalVariable.getValueType());
    summaryTabPresenter
        .setResourceUri(UriBuilders.DATASOURCE_TABLE_VARIABLE_SUMMARY.create(), originalTable.getValueSetCount(),
            originalTable.getDatasourceName(), originalTable.getName(), originalVariable.getName());
    summaryTabPresenter.forgetSummary();
    summaryTabPresenter.onReset();
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<>();
    stepBuilders.add(getView().getMethodStepBuilder() //
        .onStepIn(stepInHandler) //
        .onValidate(new MethodStepValidationHandler()));
    stepBuilders.add(getView().getMapStepBuilder() //
        .onStepIn(new MapStepInHandler()) //
        .onValidate(new MapStepValidationHandler() {

          @Override
          public List<String> getErrors() {
            return derivationHelper.getMapStepErrors();
          }

          @Override
          public List<String> getWarnings() {
            return derivationHelper.getMapStepWarnings();
          }
        }));
    return stepBuilders;
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private boolean addValueMapEntry(@Nullable String value, String newValue) {
    if(derivationHelper.hasValueMapEntryWithValue(value)) {
      fireEvent(NotificationEvent.newBuilder().error(translations.valueMapAlreadyAdded()).build());
      return false;
    }
    numberType.addValueMapEntry(derivationHelper, value, newValue);
    return true;
  }

  private boolean addValueMapEntry(@Nullable Number lower, @Nullable Number upper, String newValue) {
    if(!numberType.addValueMapEntry(derivationHelper, lower, upper, newValue)) {
      fireEvent(NotificationEvent.newBuilder().error(translations.rangeOverlap()).build());
      return false;
    }
    return true;
  }

  @Override
  public void generateDerivedVariable() {
    setDerivedVariable(derivationHelper.getDerivedVariable());
  }

  @Override
  protected void onBind() {
    getView().setSummaryTabWidget(summaryTabPresenter.getView());
    registerHandler(
        getEventBus().addHandler(SummaryReceivedEvent.getType(), new OriginalVariableSummaryReceivedHandler()));
    registerHandler(getView().addValueMapEntryHandler(new AddValueMapEntryHandler()));
  }

  @Override
  public void onMethodChange() {
    // We are in a "derived to" wizard
    if(getDestinationTable() == null) {
      // Reinitialize the destination variable
      setDerivedVariable(null);
    }
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  /**
   *
   */
  private final class MethodStepValidationHandler implements ValidationHandler {
    @Override
    public boolean validate() {
      List<String> errorMessages = new ArrayList<>();
      // validate that 1 radio is selected
      if(!getView().rangeSelected() && !getView().discreteSelected() && !getView().manualSelected()) {
        errorMessages.add(translations.selectDerivationMethod());
      }
      if(getView().rangeSelected()) {
        validateRangeForm(errorMessages);
      }
      if(!errorMessages.isEmpty()) {
        fireEvent(NotificationEvent.newBuilder().error(errorMessages).build());
      }
      return errorMessages.isEmpty();
    }

    private void validateRangeForm(Collection<String> errorMessages) {
      validateRangeLimitsForm(errorMessages);
      validateRangeDefinitionForm(errorMessages);
    }

    private void validateRangeLimitsForm(Collection<String> errorMessages) {
      getView().setLowerLimitError(false);
      getView().setUpperLimitError(false);

      Number lower = getView().getLowerLimit();
      Number upper = getView().getUpperLimit();

      if(lower == null) {
        errorMessages.add(translations.lowerValueLimitRequired());
        getView().setLowerLimitError(true);
      }
      if(upper == null) {
        errorMessages.add(translations.upperValueLimitRequired());
        getView().setUpperLimitError(true);
      }
      if(lower != null && upper != null && lower.doubleValue() > upper.doubleValue()) {
        errorMessages.add(translations.lowerLimitGreaterThanUpperLimit());
        getView().setLowerLimitError(true);
        getView().setUpperLimitError(true);
      }
    }

    private void validateRangeDefinitionForm(Collection<String> errorMessages) {
      getView().setRangeLengthError(false);
      getView().setRangeCountError(false);

      if(getView().rangeLengthSelected() && getView().getRangeLength() == null) {
        errorMessages.add(translations.rangesLengthRequired());
        getView().setRangeLengthError(true);
      } else if(!getView().rangeLengthSelected() && getView().getRangeCount() == null) {
        errorMessages.add(translations.rangesCountRequired());
        getView().setRangeCountError(true);
      }
    }
  }

  /**
   *
   */
  private final class MapStepInHandler implements StepInHandler {

    private MethodChoice lastChoice;

    private String lastChoiceSignature = "";

    @Override
    public void onStepIn() {
      if(!newMethodChoice()) return;

      List<String> derivedCategories = DerivationHelper.getDestinationCategories(getDerivedVariable());

      newDerivationHelper();
      if(getView().rangeSelected()) {
        // ranges
        if(getView().rangeLengthSelected()) {
          addRangesByLengthMapping();
        } else {
          addRangesByCountMapping();
        }
        getView().enableFrequency(false);
        getView().populateValues(derivationHelper.getValueMapEntries(), derivedCategories);
      } else if(getView().discreteSelected()) {
        addDistinctValuesMapping();
      } else {
        getView().enableFrequency(false);
        getView().populateValues(derivationHelper.getValueMapEntries(), derivedCategories);
      }
    }

    private void newDerivationHelper() {
      numberType = NumberType.valueOf(getOriginalVariable().getValueType().toUpperCase());
      derivationHelper = numberType.newDerivationHelper(getOriginalVariable(), getDerivedVariable());
    }

    private boolean newMethodChoice() {
      if(lastChoice != null && lastChoice.isCurrentChoice(getView()) &&
          lastChoice.sign(getView()).equals(lastChoiceSignature)) {
        return false;
      }
      for(MethodChoice method : MethodChoice.values()) {
        if(method.isCurrentChoice(getView())) {
          lastChoice = method;
          lastChoiceSignature = method.sign(getView());
          break;
        }
      }
      return true;
    }

    private void addDistinctValuesMapping() {

      final List<String> derivedCategories = DerivationHelper.getDestinationCategories(getDerivedVariable());
      getView().populateValues(new ArrayList<ValueMapEntry>(), derivedCategories);

      String uri = UriBuilder.create().fromPath(getOriginalVariable().getLink()).segment("summary")
          .query("nature", "categorical", "distinct", "true").build();
      ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder() //
          .forResource(uri) //
          .withCallback(new ResourceCallback<SummaryStatisticsDto>() {

            @Override
            public void onResource(Response response, SummaryStatisticsDto summaryStatisticsDto) {
              CategoricalSummaryDto categoricalSummaryDto = summaryStatisticsDto
                  .getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
              derivationHelper.addDistinctValues(categoricalSummaryDto);

              getView().enableFrequency(true);
              getView().populateValues(derivationHelper.getValueMapEntries(), derivedCategories);
            }
          }) //
          .get().send();
    }

    private void addRangesByCountMapping() {
      double lowerLimit = getView().getLowerLimit().doubleValue();
      double upperLimit = getView().getUpperLimit().doubleValue();
      long count = getView().getRangeCount();
      double length = (upperLimit - lowerLimit) / count;
      addRangesByLength(length);
    }

    private void addRangesByLengthMapping() {
      addRangesByLength(getView().getRangeLength().doubleValue());
    }

    @SuppressWarnings("UnusedAssignment")
    private void addRangesByLength(double length) {
      String lengthStr = String.valueOf(length);
      if(lengthStr.contains(".")) {
        // Adjust Scale precision
        scale = lengthStr.length() - lengthStr.indexOf(".");
      }

      double lowerLimit = getView().getLowerLimit().doubleValue();
      double upperLimit = getView().getUpperLimit().doubleValue();

      int newValue = 1;

      double lower = lowerLimit;
      double upper = lower + length;

      numberType.setScale(scale);
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
  }

  private enum MethodChoice {
    RANGE {
      @Override
      public boolean isCurrentChoice(Display display) {
        return display.rangeSelected();
      }

      @Override
      public String sign(Display display) {
        return super.sign(display) + ":" + display.getLowerLimit() + ":" + display.getUpperLimit() + ":" +
            (display.rangeLengthSelected() ? "length:" + display.getRangeLength() : "count" + display.getRangeCount());
      }

    },
    DISCRETE {
      @Override
      public boolean isCurrentChoice(Display display) {
        return display.discreteSelected();
      }

    },
    MANUAL {
      @Override
      public boolean isCurrentChoice(Display display) {
        return !display.rangeSelected() && !display.discreteSelected();
      }

    };

    public abstract boolean isCurrentChoice(Display display);

    public String sign(Display display) {
      return toString().toLowerCase();
    }
  }

  private enum NumberType {
    INTEGER() {
      @Override
      public NumericalVariableDerivationHelper<?> newDerivationHelper(VariableDto originalVariable,
          VariableDto destinationVariable) {
        return new NumericalVariableDerivationHelper<Long>(originalVariable, destinationVariable);
      }

      @SuppressWarnings("unchecked")
      @Override
      public void addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, String value,
          String newValue) {
        ((NumericalVariableDerivationHelper<Long>) helper).addValueMapEntry(new Long(value), newValue);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, Number lower,
          Number upper, String newValue) {
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
      @Nullable
      @Override
      public String formatNumber(Number nb) {
        if(nb == null) return null;
        String str = nb.toString();
        return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
      }

      @Override
      public NumericalVariableDerivationHelper<? extends Number> newDerivationHelper(VariableDto originalVariable,
          VariableDto destinationVariable) {
        return new NumericalVariableDerivationHelper<Double>(originalVariable, destinationVariable);
      }

      @SuppressWarnings("unchecked")
      @Override
      public void addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, String value,
          String newValue) {
        ((NumericalVariableDerivationHelper<Double>) helper).addValueMapEntry(new Double(value), newValue);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper, Number lower,
          Number upper, String newValue) {
        NumericalVariableDerivationHelper<Double> h = (NumericalVariableDerivationHelper<Double>) helper;

        Double l = lower == null ? null : asDouble(lower);
        Double u = upper == null ? null : asDouble(upper);
        if(h.isRangeOverlap(l, u)) {
          return false;
        }
        h.addValueMapEntry(l, u, newValue);
        return true;
      }

    };

    private int scale = 2;

    @Nullable
    public String formatNumber(Number nb) {
      return nb == null ? null : nb.toString();
    }

    public abstract NumericalVariableDerivationHelper<? extends Number> newDerivationHelper(
        VariableDto originalVariable, VariableDto destinationVariable);

    public abstract void addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper,
        @Nullable String value, String newValue);

    public abstract boolean addValueMapEntry(NumericalVariableDerivationHelper<? extends Number> helper,
        @Nullable Number lower, @Nullable Number upper, String newValue);

    public void setScale(int scale) {
      this.scale = scale;
    }

    public double asDouble(Number upper) {
      return new BigDecimal(upper.doubleValue()).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
  }

  /**
   *
   */
  private final class OriginalVariableSummaryReceivedHandler implements SummaryReceivedEvent.Handler {
    @Override
    public void onSummaryReceived(SummaryReceivedEvent event) {
      if(getOriginalVariable() != null && event.getResourceUri().equals(getOriginalVariable().getLink() + "/summary")) {
        SummaryStatisticsDto dto = event.getSummary();
        if(dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous) != null) {
          ContinuousSummaryDto continuous = dto
              .getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous).cast();
          double from = continuous.getSummary().getMin();
          double to = continuous.getSummary().getMax();
          getView().setValueLimits((long) from, (long) (to + 1));

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
      List<String> errorMessages = new ArrayList<>();
      validateValueMappingValues(errorMessages);

      if(errorMessages.isEmpty()) {
        boolean added = getView().addRangeSelected()
            ? addValueMapEntry(getView().getLowerValue(), getView().getUpperValue(), getView().getNewValue())
            : addValueMapEntry(numberType.formatNumber(getView().getDiscreteValue()), getView().getNewValue());
        if(added) {
          getView().refreshValuesMapDisplay();
        }
      } else {
        fireEvent(NotificationEvent.newBuilder().error(errorMessages).build());
      }
    }

    private void validateValueMappingValues(Collection<String> errorMessages) {
      if(getView().addRangeSelected()) {
        Number lower = getView().getLowerValue();
        Number upper = getView().getUpperValue();
        if(lower == null) {
          errorMessages.add(translations.lowerValueLimitRequired());
        }
        if(upper == null) {
          errorMessages.add(translations.upperValueLimitRequired());
        }
        if(lower != null && upper != null && lower.doubleValue() > upper.doubleValue()) {
          errorMessages.add(translations.lowerLimitGreaterThanUpperLimit());
        }
      } else if(getView().getDiscreteValue() == null) {
        errorMessages.add(translations.discreteValueRequired());
      }
    }
  }

  public interface Display extends View, HasUiHandlers<DerivationUiHandlers> {

    DefaultWizardStepController.Builder getMethodStepBuilder();

    DefaultWizardStepController.Builder getMapStepBuilder();

    void setRangeCountError(boolean error);

    void setRangeLengthError(boolean error);

    void setUpperLimitError(boolean error);

    void setLowerLimitError(boolean error);

    void populateValues(List<ValueMapEntry> valuesMap, List<String> derivedCategories);

    void refreshValuesMapDisplay();

    HandlerRegistration addValueMapEntryHandler(ClickHandler handler);

    boolean rangeSelected();

    boolean discreteSelected();

    boolean manualSelected();

    void enableFrequency(boolean enable);

    void setValueLimits(Number from, Number to);

    Number getLowerLimit();

    Number getUpperLimit();

    Number getRangeLength();

    Long getRangeCount();

    boolean rangeLengthSelected();

    Number getDiscreteValue();

    Number getLowerValue();

    Number getUpperValue();

    String getNewValue();

    boolean addRangeSelected();

    void setNumberType(String valueType);

    void setSummaryTabWidget(View widget);

  }

}
