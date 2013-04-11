/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitSelectedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.util.DatasourceDtos;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;

import static org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig.ImportFormat;

public class IdentifiersImportPresenter extends WizardPresenterWidget<IdentifiersImportPresenter.Display> {

  private static final Translations translations = GWT.create(Translations.class);

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<IdentifiersImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<IdentifiersImportPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  public interface Display extends WizardView {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousClickHandler(ClickHandler handler);

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void setCsvOptionsFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    HasText getSelectedFile();

    boolean isIdentifiersOnly();

    boolean isIdentifiersPlusData();

    ImportFormat getImportFormat();

    /**
     * Display no format options in the Format Options Step. The format chosen has no options.
     */
    void setNoFormatOptions();

    /**
     * Display the CSV format options in the Format Options Step.
     */
    void setCsvFormatOptions();

    void renderPendingConclusion();

    void renderCompletedConclusion();

    void renderFailedConclusion();

    CsvOptionsView getCsvOptions();

    void setDefaultCharset(String defaultCharset);

  }

  private final FileSelectionPresenter fileSelectionPresenter;

  private final FileSelectionPresenter csvOptionsFileSelectionPresenter;

  private ImportConfig importConfig;

  private final List<String> availableCharsets = new ArrayList<String>();

  private FunctionalUnitDto functionalUnit;

  protected TableDto identifiersTable;

  @Inject
  public IdentifiersImportPresenter(Display display, EventBus eventBus,
      Provider<FileSelectionPresenter> fileSelectionPresenter) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenter.get();
    csvOptionsFileSelectionPresenter = fileSelectionPresenter.get();
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length > 0) {
      if(event.getEventParameters()[0] instanceof FunctionalUnitDto) {
        functionalUnit = (FunctionalUnitDto) event.getEventParameters()[0];
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected FunctionalUnitDto)");
      }

    } else {
      throw new IllegalArgumentException("missing event parameter: unit name");
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    getIdentifiersTable();
    getDefaultCharset();
    getAvailableCharsets();

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectorWidgetDisplay(fileSelectionPresenter.getDisplay());

    csvOptionsFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    csvOptionsFileSelectionPresenter.bind();
    getView().setCsvOptionsFileSelectorWidgetDisplay(csvOptionsFileSelectionPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    functionalUnit = null;
  }

  private void getIdentifiersTable() {
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource("/functional-units/entities/table").get()
        .withCallback(new ResourceCallback<TableDto>() {

          @Override
          public void onResource(Response response, TableDto resource) {
            if(resource != null) {
              identifiersTable = resource;
            }
          }
        }).send();
  }

  @Override
  protected void onClose() {
    super.onClose();
    getEventBus().fireEvent(new FunctionalUnitUpdatedEvent(functionalUnit));
  }

  @Override
  protected void onFinish() {
    super.onFinish();
    FileValidator validator = new FileValidator();
    if(validator.validate()) {
      getView().renderPendingConclusion();
      populateImportData();
      importIdentifiers();
    }
  }

  private void addEventHandlers() {
    registerHandler(getView().addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        update();
      }
    }));
  }

  private void update() {
    if(fileSelectionPresenter.getSelectedFile() != null && !"".equals(fileSelectionPresenter.getSelectedFile())) {
      csvOptionsFileSelectionPresenter.setSelectedFile(fileSelectionPresenter.getSelectedFile());
    }
    if(getView().getImportFormat() == ImportFormat.CSV) {
      getView().setCsvFormatOptions();
    } else {
      getView().setNoFormatOptions();
    }
  }

  class FileValidator extends AbstractValidationHandler {

    public FileValidator() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      if(getView().getImportFormat() == ImportFormat.CSV) {
        validators.add(new RegExValidator(getSelectedCsvFile(), ".csv$", "i", "CSVFileRequired"));
        validators
            .add(new RegExValidator(getView().getCsvOptions().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger"));
        validators.add(new RequiredTextValidator(getView().getCsvOptions().getCharsetText(), "CharsetNotAvailable"));
        validators.add(
            new RequiredTextValidator(getView().getCsvOptions().getFieldSeparatorText(), "FieldSeparatorRequired"));
        validators.add(new RequiredTextValidator(getView().getCsvOptions().getQuoteText(), "QuoteSeparatorRequired"));
      } else if(getView().getImportFormat() == ImportFormat.XML) {
        validators.add(new RegExValidator(getSelectedFile(), ".zip$", "i", "ZipFileRequired"));
      } else if(getView().getImportFormat() == ImportFormat.SPSS) {
        validators.add(new RegExValidator(getSelectedFile(), ".sav$", "i", "SpssFileRequired"));
      } else {
        validators.add(new RequiredTextValidator(getView().getSelectedFile(), "NoFileSelected"));
      }

      return validators;
    }
  }

  private void populateImportData() {
    importConfig = new ImportConfig();
    importConfig.setFormat(getView().getImportFormat());
    importConfig.setDestinationDatasourceName(null); // no ref table
    importConfig.setDestinationTableName(identifiersTable.getName());
    importConfig.setDestinationEntityType("Participant");
    importConfig.setCsvFile(csvOptionsFileSelectionPresenter.getSelectedFile());
    importConfig.setXmlFile(fileSelectionPresenter.getSelectedFile());
    importConfig.setUnit(functionalUnit.getName());
    importConfig.setCharacterSet(getView().getCsvOptions().getCharsetText().getText());
    importConfig.setRow(Integer.parseInt(getView().getCsvOptions().getRowText().getText()));
    importConfig.setQuote(getView().getCsvOptions().getQuote());
    importConfig.setField(getView().getCsvOptions().getFieldSeparator());
  }

  private void importIdentifiers() {
    DatasourceFactoryDto factory = DatasourceDtos.createDatasourceFactoryDto(importConfig);

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          getView().renderCompletedConclusion();
          getEventBus().fireEvent(new FunctionalUnitSelectedEvent(functionalUnit));
          getEventBus().fireEvent(
              NotificationEvent.newBuilder().info(translations.identifierImportCompletedConclusion()).build());
        } else {
          getView().renderFailedConclusion();
          getEventBus()
              .fireEvent(NotificationEvent.newBuilder().error(translations.identifierImportFailedConclusion()).build());
        }
      }
    };

    // import only the identifiers, ignore identifying variables if any.
    String path = "/functional-unit/" + functionalUnit.getName() + "/entities?select=false";

    ResourceRequestBuilderFactory.<DatasourceFactoryDto>newBuilder().forResource(path).post()//
        .withResourceBody(DatasourceFactoryDto.stringify(factory))//
        .withCallback(200, callbackHandler)//
        .withCallback(400, callbackHandler)//
        .withCallback(500, callbackHandler).send();
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

  private HasText getSelectedCsvFile() {
    HasText result = new HasText() {

      @Override
      public String getText() {
        return csvOptionsFileSelectionPresenter.getSelectedFile();
      }

      @Override
      public void setText(String text) {
        // do nothing
      }
    };
    return result;
  }

  private HasText getSelectedFile() {
    HasText result = new HasText() {

      @Override
      public String getText() {
        return fileSelectionPresenter.getSelectedFile();
      }

      @Override
      public void setText(String text) {
        // do nothing
      }
    };
    return result;
  }

}
