/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.derive;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveBooleanVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveCategoricalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveConclusionPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveFromVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveOpenTextualVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveTemporalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveBooleanVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveCategoricalVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveConclusionView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveCustomVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveFromVariableView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveNumericalVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveOpenTextualVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveTemporalVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveVariableView;
import org.obiba.opal.web.gwt.app.client.magma.presenter.CodingViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.view.CodingViewModalView;

public class DeriveModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(DeriveVariablePresenter.class, DeriveVariablePresenter.Display.class,
        DeriveVariableView.class, DeriveVariablePresenter.CategorizeWizard.class);
    // tricky case: one wizard presenter for 3 different types
    bind(DeriveVariablePresenter.CustomWizard.class).asEagerSingleton();
    bind(DeriveVariablePresenter.FromWizard.class).asEagerSingleton();

    configureDeriveVariablePresenters();
  }

  private void configureDeriveVariablePresenters() {
    bindPresenterWidget(DeriveCategoricalVariableStepPresenter.class,
        DeriveCategoricalVariableStepPresenter.Display.class, DeriveCategoricalVariableStepView.class);
    bindPresenterWidget(DeriveNumericalVariableStepPresenter.class, DeriveNumericalVariableStepPresenter.Display.class,
        DeriveNumericalVariableStepView.class);
    bindPresenterWidget(DeriveBooleanVariableStepPresenter.class, DeriveBooleanVariableStepPresenter.Display.class,
        DeriveBooleanVariableStepView.class);
    bindPresenterWidget(DeriveTemporalVariableStepPresenter.class, DeriveTemporalVariableStepPresenter.Display.class,
        DeriveTemporalVariableStepView.class);
    bindPresenterWidget(DeriveOpenTextualVariableStepPresenter.class,
        DeriveOpenTextualVariableStepPresenter.Display.class, DeriveOpenTextualVariableStepView.class);
    bindPresenterWidget(DeriveCustomVariablePresenter.class, DeriveCustomVariablePresenter.Display.class,
        DeriveCustomVariableStepView.class);
    bindPresenterWidget(DeriveFromVariablePresenter.class, DeriveFromVariablePresenter.Display.class,
        DeriveFromVariableView.class);
    bindPresenterWidget(DeriveConclusionPresenter.class, DeriveConclusionPresenter.Display.class,
        DeriveConclusionView.class);
    bind(CodingViewModalPresenter.Display.class).to(CodingViewModalView.class);
  }

}
