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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;

import com.google.inject.Inject;

public class XmlFormatStepPresenter extends WidgetPresenter<XmlFormatStepPresenter.Display> implements DataImportPresenter.DataImportFormatStepPresenter {

  @Inject
  private FileSelectionPresenter xmlFileSelectionPresenter;

  @Inject
  public XmlFormatStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    xmlFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE_OR_FOLDER);
    xmlFileSelectionPresenter.bind();
    getDisplay().setXmlFileSelectorWidgetDisplay(xmlFileSelectionPresenter.getDisplay());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public ImportData getImportData() {
    ImportData importData = new ImportData();
    importData.setFormat(ImportFormat.XML);
    importData.setXmlFile(getDisplay().getSelectedFile());

    return importData;
  }

  @Override
  public boolean validate() {
    if(getDisplay().getSelectedFile().isEmpty() || !getDisplay().getSelectedFile().toLowerCase().endsWith(".zip")) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error("ZipFileRequired").build());
      return false;
    }
    return true;
  }

  //
  // Interfaces
  //

  public interface Display extends WidgetDisplay, WizardStepDisplay {

    void setXmlFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    String getSelectedFile();

  }

}
