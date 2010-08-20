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

import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.UploadVariablesStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.view.UploadVariablesStepView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * Bind concrete implementations to interfaces within the Import Variables wizard.
 */
public class ImportVariablesWizardModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(UploadVariablesStepPresenter.Display.class).to(UploadVariablesStepView.class).in(Singleton.class);
  }

}
