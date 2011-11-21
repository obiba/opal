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
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class DeriveOpenTextualVariableStepPresenter extends DerivationPresenter<DeriveOpenTextualVariableStepPresenter.Display> {

  private OpenTextualVariableDerivationHelper derivationHelper;

  private CategoricalSummaryDto categoricalSummaryDto;

  private static final NumberFormat FREQ_FORMAT = NumberFormat.getFormat("#,##0");

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
          StringBuilder link = new StringBuilder(originalVariable.getLink())//
          .append("/summary")//
          .append("?nature=categorical")//
          .append("&distinct=true");

          ResourceRequestBuilderFactory.<SummaryStatisticsDto> newBuilder()//
          .forResource(link.toString()).get()//
          .withCallback(new ResourceCallback<SummaryStatisticsDto>() {

            @Override
            public void onResource(Response response, SummaryStatisticsDto dto) {
              categoricalSummaryDto = dto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
              derivationHelper = new OpenTextualVariableDerivationHelper(originalVariable, dto, getDisplay().getMethod());
              derivationHelper.initializeValueMapEntries();
              JsArray<FrequencyDto> frequenciesArray = categoricalSummaryDto.getFrequenciesArray();
              for(int i = 0; i < frequenciesArray.length(); i++) {
                FrequencyDto frequencyDto = frequenciesArray.get(i);
                display.addValueSuggestion(frequencyDto.getValue(), FREQ_FORMAT.format(frequencyDto.getFreq()) + "");
              }
              display.getValueMapGrid().setMaxFrequency(getMaxFrequency());
              display.populateValues(derivationHelper.getValueMapEntries());
            }
          }).send();
        }
      }
    }).build());
    return stepCtrls;
  }

  private Double getMaxFrequency() {
    return Iterables.find(JsArrays.toList(categoricalSummaryDto.getFrequenciesArray()), new Predicate<FrequencyDto>() {

      @Override
      public boolean apply(FrequencyDto dto) {
        return dto.getValue().equals(categoricalSummaryDto.getMode());
      }
    }).getFreq();
  }

  @Override
  protected void onBind() {
    getDisplay().getValueMapGrid().enableRowDeletion(true);
    getDisplay().getValueMapGrid().enableFrequencyColumn(true);
    getDisplay().getAddButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if(addEntry(getDisplay().getValue().getValue(), getDisplay().getNewValue().getValue(), getDisplay().getNewValue().getValue())) {
          getDisplay().emptyValueFields();
          display.entryAdded();
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

    HasValue<String> getValue();

    HasValue<String> getNewValue();

    void emptyValueFields();

    void entryAdded();

    void addValueSuggestion(String replacementString, String displayString);
  }
}
