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

import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.CsvFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.IdentityArchiveStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.XmlFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.ConclusionStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.CsvFormatStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.DataImportView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.DestinationSelectionStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.IdentityArchiveStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.XmlFormatStepView;

/**
 * Bind concrete implementations to interfaces within the import wizard.
 */
public class ImportModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(DataImportPresenter.class, DataImportPresenter.Display.class, DataImportView.class, DataImportPresenter.Wizard.class);

    bind(CsvFormatStepPresenter.Display.class).to(CsvFormatStepView.class);
    bind(DestinationSelectionStepPresenter.Display.class).to(DestinationSelectionStepView.class);
    bind(XmlFormatStepPresenter.Display.class).to(XmlFormatStepView.class);
    bind(IdentityArchiveStepPresenter.Display.class).to(IdentityArchiveStepView.class);
    bind(ConclusionStepPresenter.Display.class).to(ConclusionStepView.class);
  }
}
