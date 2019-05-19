/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.view.VariablesToViewView;

/**
 * Bind concrete implementations to interfaces within the Create View wizard.
 */
public class CreateViewWizardModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindPresenterWidget(VariablesToViewPresenter.class, VariablesToViewPresenter.Display.class,
        VariablesToViewView.class);
  }
}
