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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.OpenTextualVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.OpenTextualVariableDerivationHelper.Method;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.Builder;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapGrid;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

public class DeriveOpenTextualVariableStepPresenter extends DerivationPresenter<DeriveOpenTextualVariableStepPresenter.Display> {

  private OpenTextualVariableDerivationHelper derivationHelper;

  private CategoricalSummaryDto categoricalSummaryDto;

  public static final NumberFormat FREQ_FORMAT = NumberFormat.getFormat("#,##0");

  @Inject
  public DeriveOpenTextualVariableStepPresenter(final EventBus eventBus, final Display view) {
    super(eventBus, view);
  }

  @Override
  public VariableDto getDerivedVariable() {
    return derivationHelper.getDerivedVariable();
  }

  @Override
  List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();

    stepCtrls.add(getView().getMethodStepController().build());
    stepCtrls.add(getView().getMapStepController().onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        if(derivationHelper == null || derivationHelper.getMethod() != getView().getMethod()) {
          StringBuilder link = new StringBuilder(originalVariable.getLink())//
          .append("/summary")//
          .append("?nature=categorical")//
          .append("&distinct=true");

          getView().populateValues(new ArrayList<ValueMapEntry>());

          ResourceRequestBuilderFactory.<SummaryStatisticsDto> newBuilder()//
          .forResource(link.toString()).get()//
          .withCallback(new ResourceCallback<SummaryStatisticsDto>() {

            @Override
            public void onResource(Response response, SummaryStatisticsDto summaryStatisticsDto) {
              categoricalSummaryDto = summaryStatisticsDto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
              derivationHelper = new OpenTextualVariableDerivationHelper(originalVariable, summaryStatisticsDto, getView().getMethod());
              derivationHelper.initializeValueMapEntries();
              JsArray<FrequencyDto> frequenciesArray = categoricalSummaryDto.getFrequenciesArray();
              for(int i = 0; i < frequenciesArray.length(); i++) {
                FrequencyDto frequencyDto = frequenciesArray.get(i);
                getView().addValueSuggestion(frequencyDto.getValue(), FREQ_FORMAT.format(frequencyDto.getFreq()));
              }
              getView().getValueMapGrid().setMaxFrequency(getMaxFrequency());
              getView().populateValues(derivationHelper.getValueMapEntries());
            }
          }).send();
        }
      }
    }).build());
    return stepCtrls;
  }

  private Double getMaxFrequency() {
    if(categoricalSummaryDto.getFrequenciesArray() == null) return 0d;

    return Iterables.find(JsArrays.toList(categoricalSummaryDto.getFrequenciesArray()), new Predicate<FrequencyDto>() {

      @Override
      public boolean apply(FrequencyDto dto) {
        return dto.getValue().equals(categoricalSummaryDto.getMode());
      }
    }).getFreq();
  }

  @Override
  protected void onBind() {
    getView().getValueMapGrid().enableRowDeletion(true);
    getView().getValueMapGrid().enableFrequencyColumn(true);
    getView().getAddButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        String newValue = getView().getNewValue().getValue();
        String valueValue = getView().getValue().getValue();
        if(addEntry(valueValue, newValue, newValue)) {
          getView().emptyValueFields();
          getView().entryAdded();
        }
      }
    });
  }

  public boolean addEntry(String value, String newValue, String label) {
    if(value != null && !value.trim().equals("") && newValue != null && !newValue.trim().equals("")) {
      Double count = getFreqFromValue(value);
      Builder builder = ValueMapEntry.fromDistinct(value).newValue(newValue).label(label);
      if(count != null) {
        builder.count(count);
      }
      ValueMapEntry entry = builder.build();
      return derivationHelper.addEntry(entry);

    }
    return false;
  }

  private Double getFreqFromValue(String value) {
    JsArray<FrequencyDto> frequenciesArray = categoricalSummaryDto.getFrequenciesArray();
    for(int i = 0; i < frequenciesArray.length(); i++) {
      FrequencyDto frequencyDto = frequenciesArray.get(i);
      if(frequencyDto.getValue().equals(value)) {
        return frequencyDto.getFreq();
      }
    }
    return null;
  }

  @Override
  protected void onUnbind() {
  }

  public interface Display extends View {

    DefaultWizardStepController.Builder getMethodStepController();

    DefaultWizardStepController.Builder getMapStepController();

    Method getMethod();

    void populateValues(List<ValueMapEntry> valueMapEntries);

    HasClickHandlers getAddButton();

    ValueMapGrid getValueMapGrid();

    HasValue<String> getValue();

    HasValue<String> getNewValue();

    void emptyValueFields();

    void entryAdded();

    void addValueSuggestion(String value, String frequency);
  }
}
