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

import org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.copydata.view.DataCopyView;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.view.DataExportView;

/**
 * Bind concrete implementations to interfaces within the export wizard.
 */
public class ExportModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(DataExportPresenter.class, DataExportPresenter.Display.class, DataExportView.class, DataExportPresenter.Wizard.class);
    bindWizardPresenterWidget(DataCopyPresenter.class, DataCopyPresenter.Display.class, DataCopyView.class, DataCopyPresenter.Wizard.class);
  }
}
