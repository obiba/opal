/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.CsvOptionsDisplay;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.CharacterSetEncodingValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class CsvFormatStepPresenter extends PresenterWidget<CsvFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter, CsvFormatStepUiHandlers {

  private static final Translations translations = GWT.create(Translations.class);

  private final Collection<String> availableCharsets = new ArrayList<String>();

  private FileSelectionPresenter csvFileSelectionPresenter;

  private ViewValidator viewValidator;

  private String destination;

  private DatasourceDto datasource;

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
    getView().setCsvFileSelectorVisible(false);
    addHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {
      @Override
      public void onFileSelection(FileSelectionEvent event) {
        getView().setTable(createTableName(event.getSelectedFile().getSelectionPath()));
      }
    });
    getView().setEntityType("Participant");
    getView().setUiHandlers(this);
    viewValidator = new ViewValidator();
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

  private void refreshDatasource() {
    UriBuilder ub = UriBuilder.create().segment("datasource", destination);
    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<DatasourceDto>() {
          @Override
          public void onResource(Response response, DatasourceDto resource) {
            resource.setTableArray(JsArrays.toSafeArray(resource.getTableArray()));
            resource.setViewArray(JsArrays.toSafeArray(resource.getViewArray()));
            datasource = resource;
            getView().updateTables(datasource);
          }
        }).send();
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
    importConfig.setDestinationDatasourceName(destination);
    importConfig.setFormat(ImportFormat.CSV);
    importConfig.setDestinationTableName(getView().getSelectedTable().getText());
    importConfig.setDestinationEntityType(getView().getSelectedEntityType().getText());
    importConfig.setCsvFile(getSelectedFile());
    importConfig.setRow(Integer.parseInt(getView().getRowText().getText()));
    importConfig.setField(getView().getFieldSeparator().getText());
    importConfig.setQuote(getView().getQuote().getText());
    importConfig.setCharacterSet(getSelectedCharacterSet());
    return importConfig;
  }

  @Override
  public boolean validate() {
    return viewValidator.validate();
  }

  public Map<HasType<ControlGroupType>, String> getErrors() {
    return viewValidator.errors;
  }

  public void setDestination(String destination) {
    this.destination = destination;
    refreshDatasource();
  }

  @Override
  public void selectTable(HasText selectedTable) {
    UriBuilder ub = UriBuilder.create().segment("datasource", datasource.getName(), "table", selectedTable.getText())
        .query("counts", "false");
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()//
        .withCallback(new ResourceCallback<TableDto>() {

          @Override
          public void onResource(Response response, TableDto resource) {
            if(resource != null) {
              getView().setEntityType(resource.getEntityType());
            }
          }
        }).send();

  }

  private String createTableName(String selectedFile) {
    return selectedFile.substring(selectedFile.lastIndexOf('/') + 1, selectedFile.lastIndexOf('.'));
  }

  //
  // Inner classes
  //

  public interface Display extends CsvOptionsDisplay, View, WizardStepDisplay, HasUiHandlers<CsvFormatStepUiHandlers> {

    enum FormField {
      FILE,
      TABLE,
      ENTITY_TYPE
    }

    void setEntityType(String entityType);

    HasText getSelectedTable();

    HasText getSelectedEntityType();

    void updateTables(DatasourceDto datasourceDto);

    void setTable(String tableName);

  }

  private final class ViewValidator extends ViewValidationHandler {

    Map<HasType<ControlGroupType>, String> errors;

    @Override
    protected Set<FieldValidator> getValidators() {
      errors = new HashMap<HasType<ControlGroupType>, String>();

      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      addTableValidators(validators);
      addEntityTypeValidator(validators);
      addRowValidators(validators);
      addOptionsValidators(validators);
      addFileValidators(validators);
      addCharsetValidator(validators);

      return validators;
    }

    private void addRowValidators(Set<FieldValidator> validators) {
      validators.add(new RegExValidator(getView().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger",
          CsvOptionsDisplay.CsvFormField.ROW.name()));
    }

    private void addOptionsValidators(Set<FieldValidator> validators) {
      validators.add(new RequiredTextValidator(getView().getFieldSeparator(), "FieldSeparatorRequired",
          CsvOptionsDisplay.CsvFormField.FIELD.name()));
      validators.add(new RequiredTextValidator(getView().getQuote(), "QuoteSeparatorRequired",
          CsvOptionsDisplay.CsvFormField.QUOTE.name()));
    }

    private void addFileValidators(Set<FieldValidator> validators) {
      validators.add(new FileTypeValidator(csvFileSelectionPresenter.getSelectedFile(), Display.FormField.FILE.name()));
    }

    private void addCharsetValidator(Set<FieldValidator> validators) {
      String charset = getView().getCharsetText().getText();
      CharacterSetEncodingValidator charsetValidator = new CharacterSetEncodingValidator(charset,
          "InvalidCharacterSetName", CsvOptionsDisplay.CsvFormField.CHARSET.name());
      charsetValidator.setArgs(Arrays.asList(charset));
      validators.add(charsetValidator);
    }

    private void addTableValidators(Set<FieldValidator> validators) {
      validators.add(new RequiredTextValidator(getView().getSelectedTable(), "DestinationTableRequired",
          Display.FormField.TABLE.name()));
    }

    private void addEntityTypeValidator(Set<FieldValidator> validators) {
      validators.add(new RequiredTextValidator(getView().getSelectedEntityType(), "DestinationTableEntityTypeRequired",
          Display.FormField.ENTITY_TYPE.name()));
    }


      @Override
    protected void showMessage(String id, String message) {
      errors.put(getView().getGroupType(id), message);
    }
  }

  private final static class FileTypeValidator extends AbstractFieldValidator {
    private final String fileName;

    FileTypeValidator(String fileName, String id) {
      super("CSVFileRequired", id);
      this.fileName = fileName;
    }

    @Override
    protected boolean hasError() {
      return !fileName.toLowerCase().endsWith(".csv");
    }
  }

}
