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

import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.view.IdentifiersImportView;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.presenter.IdentifiersMapPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.view.IdentifiersMapView;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * Bind concrete implementations to interfaces within the Identifier Import Variables wizard.
 */
public class ImportIdentifiersWizardModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(IdentifiersImportPresenter.Display.class).to(IdentifiersImportView.class);
    bind(IdentifiersMapPresenter.Display.class).to(IdentifiersMapView.class);
  }
}
