/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.inject.client;

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SaveErrorsStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.ConfigureViewStepView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.SaveErrorsStepView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * Bind concrete implementations to interfaces within the Configure View wizard.
 */
public class ConfigureViewWizardModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ConfigureViewStepPresenter.Display.class).to(ConfigureViewStepView.class).in(Singleton.class);
    bind(SaveErrorsStepPresenter.Display.class).to(SaveErrorsStepView.class).in(Singleton.class);
  }
}
