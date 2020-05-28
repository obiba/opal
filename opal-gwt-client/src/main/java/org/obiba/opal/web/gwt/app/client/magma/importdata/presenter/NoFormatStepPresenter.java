/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;

import com.google.web.bindery.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class NoFormatStepPresenter extends PresenterWidget<NoFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  private ImportFormat importFormat;

  @Inject
  public NoFormatStepPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  public void setImportFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importConfig = new ImportConfig();
    importConfig.setImportFormat(importFormat);
    return importConfig;
  }

  @Override
  public boolean validate() {
    return true;
  }

  public interface Display extends View, WizardStepDisplay {

  }
}
