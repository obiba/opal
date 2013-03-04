/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.CharacterSetDisplay;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class SpssFormatStepPresenter extends WidgetPresenter<SpssFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  @Inject
  private FileSelectionPresenter spssFileSelectionPresenter;

  @Inject
  public SpssFormatStepPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    spssFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE_OR_FOLDER);
    spssFileSelectionPresenter.bind();
    getDisplay().setSpssFileSelectorWidgetDisplay(spssFileSelectionPresenter.getDisplay());
    setDefaultCharset();
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
  public ImportConfig getImportConfig() {
    ImportConfig importData = new ImportConfig();
    importData.setFormat(ImportFormat.SPSS);
    importData.setSpssFile(getDisplay().getSelectedFile());
    importData.setCharacterSet(getDisplay().getCharsetText().getText());

    return importData;
  }

  @Override
  public boolean validate() {
    if(getDisplay().getSelectedFile().isEmpty() || !getDisplay().getSelectedFile().toLowerCase().endsWith(".sav")) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error("SpssFileRequired").build());
      return false;
    }
    return true;
  }

  public interface Display extends WidgetDisplay, WizardStepDisplay, CharacterSetDisplay {

    void setSpssFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    String getSelectedFile();

  }

  private void setDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/default").get()
        .withCallback(new ResourceCallback<JsArrayString>() {

          @Override
          public void onResource(Response response, JsArrayString resource) {
            String charset = resource.get(0);
            getDisplay().setDefaultCharset(charset);
          }
        }).send();
  }

}
