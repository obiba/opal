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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.CharacterSetDisplay;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class SpssFormatStepPresenter extends PresenterWidget<SpssFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  private FileSelectionPresenter spssFileSelectionPresenter;

  @Inject
  public SpssFormatStepPresenter(EventBus eventBus, Display display,
      FileSelectionPresenter spssFileSelectionPresenter) {
    super(eventBus, display);
    this.spssFileSelectionPresenter = spssFileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    spssFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE_OR_FOLDER);
    spssFileSelectionPresenter.bind();
    getView().setSpssFileSelectorWidgetDisplay(spssFileSelectionPresenter.getView());
    setDefaultCharset();
  }

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importData = new ImportConfig();
    importData.setFormat(ImportFormat.SPSS);
    importData.setSpssFile(getView().getSelectedFile());
    importData.setCharacterSet(getView().getCharsetText().getText());
    importData.setDestinationEntityType(getView().getEntityType().getText());
    importData.setLocale(getView().getLocale());

    return importData;
  }

  @Override
  public boolean validate() {
    if(getView().getSelectedFile().isEmpty() || !getView().getSelectedFile().toLowerCase().endsWith(".sav")) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("SpssFileRequired").build());
      return false;
    }

    String selectedLocale = getView().getLocale();
    if(!LanguageLocale.isValid(selectedLocale)) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("InvalidLocaleName").args(selectedLocale).build());
      return false;
    }

    return true;
  }

  public interface Display extends View, WizardStepDisplay, CharacterSetDisplay {

    void setSpssFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    String getSelectedFile();

    HasText getEntityType();

    String getLocale();
  }

  private void setDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/default").get()
        .withCallback(new ResourceCallback<JsArrayString>() {

          @Override
          public void onResource(Response response, JsArrayString resource) {
            String charset = resource.get(0);
            getView().setDefaultCharset(charset);
          }
        }).send();
  }

}
