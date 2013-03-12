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

import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.view.IdentifiersImportView;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.presenter.IdentifiersMapPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.view.IdentifiersMapView;
import org.obiba.opal.web.gwt.app.client.wizard.syncidentifiers.presenter.IdentifiersSyncPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.syncidentifiers.view.IdentifiersSyncView;

/**
 * Bind concrete implementations to interfaces within the Identifier Import Variables wizard.
 */
public class ImportIdentifiersWizardModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(IdentifiersImportPresenter.class, IdentifiersImportPresenter.Display.class,
        IdentifiersImportView.class, IdentifiersImportPresenter.Wizard.class);
    bindWizardPresenterWidget(IdentifiersMapPresenter.class, IdentifiersMapPresenter.Display.class,
        IdentifiersMapView.class, IdentifiersMapPresenter.Wizard.class);
    bindWizardPresenterWidget(IdentifiersSyncPresenter.class, IdentifiersSyncPresenter.Display.class,
        IdentifiersSyncView.class, IdentifiersSyncPresenter.Wizard.class);
  }
}
