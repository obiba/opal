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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.magma.derive.helper.DerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.OpenTextualVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.OpenTextualVariableDerivationHelper.Method;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.ValueMapEntry.Builder;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.ValueMapGrid;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;

public class DeriveOpenTextualVariableStepPresenter
    extends DerivationPresenter<DeriveOpenTextualVariableStepPresenter.Display> {

  public static final NumberFormat FREQ_FORMAT = NumberFormat.getFormat("#,##0");

  private OpenTextualVariableDerivationHelper derivationHelper;

  private CategoricalSummaryDto categoricalSummaryDto;

  @Inject
  public DeriveOpenTextualVariableStepPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  public void generateDerivedVariable() {
    setDerivedVariable(derivationHelper.getDerivedVariable());
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<>();
    stepBuilders.add(getView().getMethodStepController().onStepIn(stepInHandler));
    stepBuilders.add(getView().getMapStepController() //
        .onStepIn(new DeriveOpenTextualVariableMapStepInHandler()) //
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
    if(value != null && !"".equals(value.trim()) && newValue != null && !"".equals(newValue.trim())) {
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

  @Nullable
  private Double getFreqFromValue(String value) {
    JsArray<FrequencyDto> frequenciesArray = categoricalSummaryDto.getFrequenciesArray();
    if(frequenciesArray != null) {
      for(int i = 0; i < frequenciesArray.length(); i++) {
        FrequencyDto frequencyDto = frequenciesArray.get(i);
        if(frequencyDto.getValue().equals(value)) {
          return frequencyDto.getFreq();
        }
      }
    }
    return null;
  }

  @Override
  protected void onUnbind() {
  }

  private final class DeriveOpenTextualVariableMapStepInHandler implements StepInHandler {
    @Override
    public void onStepIn() {
      if(derivationHelper == null || derivationHelper.getMethod() != getView().getMethod()) {

        final List<String> destinationCategories = DerivationHelper.getDestinationCategories(getDerivedVariable());
        getView().populateValues(new ArrayList<ValueMapEntry>(), destinationCategories);

        String uri = UriBuilder.create().fromPath(getOriginalVariable().getLink()).segment("summary")
            .query("nature", "categorical", "distinct", "true").build();
        ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder() //
            .forResource(uri) //
            .withCallback(new ResourceCallback<SummaryStatisticsDto>() {

              @Override
              public void onResource(Response response, SummaryStatisticsDto summaryStatisticsDto) {
                categoricalSummaryDto = summaryStatisticsDto
                    .getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
                derivationHelper = new OpenTextualVariableDerivationHelper(getOriginalVariable(), getDerivedVariable(),
                    summaryStatisticsDto, getView().getMethod());
                derivationHelper.initializeValueMapEntries();
                JsArray<FrequencyDto> frequenciesArray = categoricalSummaryDto.getFrequenciesArray();
                if(frequenciesArray != null) {
                  for(int i = 0; i < frequenciesArray.length(); i++) {
                    FrequencyDto frequencyDto = frequenciesArray.get(i);
                    getView().addValueSuggestion(frequencyDto.getValue(), FREQ_FORMAT.format(frequencyDto.getFreq()));
                  }
                }
                getView().populateValues(derivationHelper.getValueMapEntries(), destinationCategories);
              }
            }) //
            .get().send();
      }
    }

  }

  public interface Display extends View {

    DefaultWizardStepController.Builder getMethodStepController();

    DefaultWizardStepController.Builder getMapStepController();

    Method getMethod();

    void populateValues(List<ValueMapEntry> valueMapEntries, @Nullable List<String> derivedCategories);

    HasClickHandlers getAddButton();

    ValueMapGrid getValueMapGrid();

    HasValue<String> getValue();

    HasValue<String> getNewValue();

    void emptyValueFields();

    void entryAdded();

    void addValueSuggestion(String value, String frequency);
  }
}
