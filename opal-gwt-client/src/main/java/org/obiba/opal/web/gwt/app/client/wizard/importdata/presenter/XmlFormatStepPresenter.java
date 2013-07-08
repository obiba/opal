/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig.ImportFormat;

public class XmlFormatStepPresenter extends PresenterWidget<XmlFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  private FileSelectionPresenter xmlFileSelectionPresenter;

  @Inject
  public XmlFormatStepPresenter(EventBus eventBus, Display display, FileSelectionPresenter xmlFileSelectionPresenter) {
    super(eventBus, display);
    this.xmlFileSelectionPresenter = xmlFileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    xmlFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE_OR_FOLDER);
    xmlFileSelectionPresenter.bind();
    getView().setXmlFileSelectorWidgetDisplay(xmlFileSelectionPresenter.getView());
  }

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importConfig = new ImportConfig();
    importConfig.setFormat(ImportFormat.XML);
    importConfig.setXmlFile(getView().getSelectedFile());

    return importConfig;
  }

  @Override
  public boolean validate() {
    if(getView().getSelectedFile().isEmpty() || !getView().getSelectedFile().toLowerCase().endsWith(".zip")) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("ZipFileRequired").build());
      return false;
    }
    return true;
  }

  //
  // Interfaces
  //

  public interface Display extends View, WizardStepDisplay {

    void setXmlFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    String getSelectedFile();

  }

}
