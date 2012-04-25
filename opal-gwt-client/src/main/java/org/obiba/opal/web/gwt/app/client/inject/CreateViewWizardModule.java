/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.view.CreateViewStepView;

/**
 * Bind concrete implementations to interfaces within the Create View wizard.
 */
public class CreateViewWizardModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(CreateViewStepPresenter.class, CreateViewStepPresenter.Display.class, CreateViewStepView.class, CreateViewStepPresenter.Wizard.class);
  }
}
