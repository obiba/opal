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

import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.CategoricalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.DerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
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

  //
  // DerivationPresenter methods
  //

  @Override
  void initialize(TableDto originalTable, TableDto destinationTable, final VariableDto originalVariable,
      final VariableDto derivedVariable) {
    super.initialize(originalTable, destinationTable, originalVariable, derivedVariable);
    // TODO use uribuilder
    ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder()
        .forResource(getOriginalVariable().getLink() + "/stats/summary?nature=categorical&distinct=true").get()
        .withCallback(new ResourceCallback<SummaryStatisticsDto>() {
          @Override
          public void onResource(Response response, SummaryStatisticsDto statisticsDto) {
            derivationHelper = new CategoricalVariableDerivationHelper(originalVariable, derivedVariable,
                statisticsDto);
            derivationHelper.initializeValueMapEntries();
            getView().enableFrequencyColumn(true);
            getView().setMaxFrequency(derivationHelper.getMaxFrequency());
            getView().populateValues(derivationHelper.getValueMapEntries(),
                DerivationHelper.getDestinationCategories(getDerivedVariable()));
          }
        }).send();
  }

  @Override
  public void generateDerivedVariable() {
    setDerivedVariable(derivationHelper.getDerivedVariable());
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<DefaultWizardStepController.Builder>();
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

  //
  // Interfaces
  //

  public interface Display extends View {

    DefaultWizardStepController.Builder getMapStepController();

    void setMaxFrequency(double maxFrequency);

    void enableFrequencyColumn(boolean enableFrequencyColumn);

    void populateValues(List<ValueMapEntry> valuesMap, List<String> derivedCategories);

  }

}
