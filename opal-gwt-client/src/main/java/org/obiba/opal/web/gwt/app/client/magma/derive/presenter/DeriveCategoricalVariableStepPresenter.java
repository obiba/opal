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

import org.obiba.opal.web.gwt.app.client.magma.derive.helper.CategoricalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class DeriveCategoricalVariableStepPresenter
    extends DerivationPresenter<DeriveCategoricalVariableStepPresenter.Display> {

  private CategoricalVariableDerivationHelper derivationHelper;

  @Inject
  public DeriveCategoricalVariableStepPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  void initialize(TableDto originalTable, TableDto destinationTable, final VariableDto originalVariable,
      final VariableDto derivedVariable) {
    super.initialize(originalTable, destinationTable, originalVariable, derivedVariable);

    String uri = UriBuilder.create().fromPath(getOriginalVariable().getLink()).segment("summary")
        .query("nature", "categorical", "distinct", "true").build();
    ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<SummaryStatisticsDto>() {
          @Override
          public void onResource(Response response, SummaryStatisticsDto statisticsDto) {
            derivationHelper = new CategoricalVariableDerivationHelper(originalVariable, derivedVariable,
                statisticsDto);
            derivationHelper.initializeValueMapEntries();

            getView().enableFrequencyColumn(true);
            getView()
                .populateValues(derivationHelper.getValueMapEntries(), derivationHelper.getDestinationCategories());
          }
        }).get().send();
  }

  @Override
  public void generateDerivedVariable() {
    setDerivedVariable(derivationHelper.getDerivedVariable());
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<>();
    stepBuilders.add(getView().getMapStepController() //
        .onStepIn(stepInHandler) //
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

  public interface Display extends View {

    DefaultWizardStepController.Builder getMapStepController();

    void enableFrequencyColumn(boolean enableFrequencyColumn);

    void populateValues(List<ValueMapEntry> valuesMap, @Nullable List<String> derivedCategories);

  }

}
