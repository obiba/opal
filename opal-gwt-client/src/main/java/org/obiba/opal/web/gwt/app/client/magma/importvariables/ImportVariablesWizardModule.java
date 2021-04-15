/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importvariables;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;

/**
 * Bind concrete implementations to interfaces within the Import Variables wizard.
 */
public class ImportVariablesWizardModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(VariablesImportPresenter.class, VariablesImportPresenter.Display.class,
        VariablesImportView.class, VariablesImportPresenter.Wizard.class);
    bind(ComparedDatasourcesReportStepPresenter.Display.class).to(ComparedDatasourcesReportStepView.class);
    bind(ConclusionStepPresenter.Display.class).to(ConclusionStepView.class);
  }
}
