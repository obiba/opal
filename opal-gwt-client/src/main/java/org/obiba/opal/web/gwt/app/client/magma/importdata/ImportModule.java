/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;

/**
 * Bind concrete implementations to interfaces within the import wizard.
 */
@SuppressWarnings("OverlyCoupledClass")
public class ImportModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindWizardPresenterWidget(DataImportPresenter.class, DataImportPresenter.Display.class, DataImportView.class,
        DataImportPresenter.Wizard.class);
    bindPresenterWidget(IdentifiersMappingSelectionStepPresenter.class,
        IdentifiersMappingSelectionStepPresenter.Display.class, IdentifiersMappingSelectionStepView.class);
    bindPresenterWidget(DatasourceValuesStepPresenter.class, DatasourceValuesStepPresenter.Display.class,
        DatasourceValuesStepView.class);
    bindPresenterWidget(ArchiveStepPresenter.class, ArchiveStepPresenter.Display.class, ArchiveStepView.class);

    bind(CsvFormatStepPresenter.Display.class).to(CsvFormatStepView.class);
    bind(XmlFormatStepPresenter.Display.class).to(XmlFormatStepView.class);
    bind(RHavenStepPresenter.Display.class).to(RHavenStepView.class);
    bind(JdbcStepPresenter.Display.class).to(JdbcStepView.class);
    bind(RestStepPresenter.Display.class).to(RestStepView.class);
    bind(NoFormatStepPresenter.Display.class).to(NoFormatStepView.class);

    bind(DatasourcePluginFormatStepPresenter.Display.class).to(DatasourcePluginFormatStepView.class);
  }
}
