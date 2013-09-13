/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.CsvOptionsDisplay;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class CsvFormatStepPresenter extends PresenterWidget<CsvFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  private static final Translations translations = GWT.create(Translations.class);

  private final Collection<String> availableCharsets = new ArrayList<String>();

  private FileSelectionPresenter csvFileSelectionPresenter;

  @Inject
  public CsvFormatStepPresenter(EventBus eventBus, Display display, FileSelectionPresenter csvFileSelectionPresenter) {
    super(eventBus, display);
    this.csvFileSelectionPresenter = csvFileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    getDefaultCharset();
    getAvailableCharsets();

    csvFileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    csvFileSelectionPresenter.bind();
    getView().setCsvFileSelectorWidgetDisplay(csvFileSelectionPresenter.getView());
  }

  private String getSelectedCharacterSet() {
    return getView().getCharsetText().getText();
  }

  private Collection<String> validateCharacterSet(String charset) {
    Collection<String> errors = new ArrayList<String>();
    if(charset == null || "".equals(charset)) {
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
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/default").get()
        .withCallback(new ResourceCallback<JsArrayString>() {

          @Override
          public void onResource(Response response, JsArrayString resource) {
            String charset = resource.get(0);
            getView().setDefaultCharset(charset);
          }
        }).send();

  }

  public void getAvailableCharsets() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/available").get()
        .withCallback(new ResourceCallback<JsArrayString>() {
          @Override
          public void onResource(Response response, JsArrayString datasources) {
            for(int i = 0; i < datasources.length(); i++) {
              availableCharsets.add(datasources.get(i));
            }
          }
        }).send();
  }

  public void clear() {
    getView().clear();
  }

  public String getSelectedFile() {
    return csvFileSelectionPresenter.getSelectedFile();
  }

  //
  // Display methods
  //

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importConfig = new ImportConfig();
    importConfig.setFormat(ImportFormat.CSV);
    importConfig.setCsvFile(getSelectedFile());
    importConfig.setRow(Integer.parseInt(getView().getRowText().getText()));
    importConfig.setField(getView().getFieldSeparator());
    importConfig.setQuote(getView().getQuote());
    importConfig.setCharacterSet(getSelectedCharacterSet());
    return importConfig;
  }

  @Override
  public boolean validate() {
    List<String> errors = new ArrayList<String>();

    if(csvFileSelectionPresenter.getSelectedFile().isEmpty() ||
        !csvFileSelectionPresenter.getSelectedFile().toLowerCase().endsWith(".csv")) {
      errors.add("CSVFileRequired");
    }

    errors.addAll(validateCharacterSet(getSelectedCharacterSet()));

    if(!errors.isEmpty()) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(errors).build());
    }

    return errors.isEmpty();
  }

  //
  // Inner classes
  //

  public interface Display extends CsvOptionsDisplay, View, WizardStepDisplay {

  }

}
