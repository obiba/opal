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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public abstract class DerivationPresenter<V extends View> extends PresenterWidget<V> {

  protected static final Translations translations = GWT.create(Translations.class);

  private VariableDto originalVariable;
  private VariableDto derivedVariable;

  public DerivationPresenter(EventBus eventBus, V view) {
    super(eventBus, view);
  }

  void initialize(VariableDto originalVariable, VariableDto derivedVariable) {
    this.originalVariable = originalVariable;
    this.derivedVariable = derivedVariable;
  }

  public VariableDto getOriginalVariable() {
    return originalVariable;
  }

  public void setOriginalVariable(VariableDto originalVariable) {
    this.originalVariable = originalVariable;
  }

  public VariableDto getDerivedVariable() {
    return derivedVariable;
  }

  public void setDerivedVariable(VariableDto derivedVariable) {
    this.derivedVariable = derivedVariable;
  }

  public void generateDerivedVariable() {

  }

  void setTable(TableDto table) {

  }

  public void onClose() {

  }

  abstract List<DefaultWizardStepController.Builder> getWizardStepBuilders(
      WizardStepController.StepInHandler stepInHandler);

}