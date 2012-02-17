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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.CsvOptionsDisplay;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class CsvFormatStepPresenter extends WidgetPresenter<CsvFormatStepPresenter.Display> implements DataImportPresenter.DataImportFormatStepPresenter {

  private static Translations translations = GWT.create(Translations.class);

  private List<String> availableCharsets = new ArrayList<String>();

  private String defaultCharset;

  @Inject
  private FileSelectionPresenter csvFileSelectionPresenter;

  @Inject
  public CsvFormatStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    getDefaultCharset();
    getAvailableCharsets();

    csvFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    csvFileSelectionPresenter.bind();
    getDisplay().setCsvFileSelectorWidgetDisplay(csvFileSelectionPresenter.getDisplay());
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

  private String getSelectedCharacterSet() {
    return getDisplay().getCharsetText().getText();
  }

  private List<String> validateCharacterSet(String charset) {
    List<String> errors = new ArrayList<String>();
    if(charset == null || charset.equals("")) {
      errors.add(translations.charsetMustNotBeNullMessage());

    } else if(!charsetExistsInAvailableCharsets(charset)) {
      errors.add(translations.charsetDoesNotExistMessage());
    }
    return errors;
  }

  private boolean charsetExistsInAvailableCharsets(String charset) {
    for(String availableCharset : availableCharsets) {
      if(charset.equals(availableCharset)) return true;
    }
    return false;
  }

  public void getDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/default").get().withCallback(new ResourceCallback<JsArrayString>() {

      @Override
      public void onResource(Response response, JsArrayString resource) {
        String charset = resource.get(0);
        getDisplay().setDefaultCharset(charset);
        CsvFormatStepPresenter.this.defaultCharset = charset;
      }
    }).send();

  }

  public void getAvailableCharsets() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/available").get().withCallback(new ResourceCallback<JsArrayString>() {
      @Override
      public void onResource(Response response, JsArrayString datasources) {
        for(int i = 0; i < datasources.length(); i++) {
          availableCharsets.add(datasources.get(i));
        }
      }
    }).send();
  }

  public void clear() {
    getDisplay().clear();
  }

  //
  // Display methods
  //

  @Override
  public ImportData getImportData() {
    ImportData importData = new ImportData();
    importData.setFormat(ImportFormat.CSV);
    importData.setCsvFile(csvFileSelectionPresenter.getSelectedFile());
    importData.setRow(Integer.parseInt(getDisplay().getRowText().getText()));
    importData.setField(getDisplay().getFieldSeparator());
    importData.setQuote(getDisplay().getQuote());
    importData.setCharacterSet(getSelectedCharacterSet());
    return importData;
  }

  @Override
  public boolean validate() {
    List<String> errors = new ArrayList<String>();

    if(csvFileSelectionPresenter.getSelectedFile().isEmpty()) {
      errors.add("CSVFileRequired");
    }

    errors.addAll(validateCharacterSet(getSelectedCharacterSet()));

    if(!errors.isEmpty()) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(errors).build());
    }

    return errors.size() == 0;
  }

  //
  // Inner classes
  //

  public interface Display extends CsvOptionsDisplay, WidgetDisplay, WizardStepDisplay {

  }

}
